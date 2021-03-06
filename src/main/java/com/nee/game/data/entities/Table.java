package com.nee.game.data.entities;

import com.nee.game.common.constant.CmdConstant;
import com.nee.game.common.constant.CommonConstant;
import com.nee.game.service.CardService;
import com.nee.game.service.DataService;
import com.nee.game.uitls.RevMsgUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.Inet4Address;
import java.util.*;


public class Table {

    private static final int maxCount = 4;
    private int maxGameRound = 3;
    private int radio = 8;
    private int invalidCount = 0;
    private int tableId;
    private boolean experience = false;
    private List<User> users;
    Integer adminUserId;

    private AsyncSQLClient mysqlClient;

    private List<Map<String, Object>> hu_tasks = new ArrayList<>();
    private List<Map<String, Object>> action_tasks = new ArrayList<>();
    private List<User> huUsers = new ArrayList<>();

    private User currentChiUser;
    User currentPlayUser;
    private User currentActionUser;
    Integer cid;
    private Byte currentPoke;
    private Timer timer = new Timer();

    private Map<Integer, Integer> pcMap = new HashMap<>();

    void setCurrentEle(User user, Byte poke) {
        this.currentActionUser = user;
        this.currentPoke = poke;
    }

    private CardService cardService;
    public int tache = 0;
    private int gameRound = 0;

    List<User> getUsers() {
        if (users == null) {
            initUsers();
        }
        return users;
    }

    private void initUsers() {
        users = new ArrayList<>();
        for (int i = 0; i < maxCount; i++) {
            users.add(i, null);
        }
    }

    int getCurrentActionSeatId() {
        return currentActionUser.getSeatId();
    }

    int getInvalidCount() {
        return invalidCount;
    }

    public int getTableId() {
        return tableId;
    }

    private Boolean isEnd() {
        return gameRound >= maxGameRound;
    }

    void addUser(User user) {
        if (users.contains(user)) {
            return;
        }
        for (int i = 0; i < maxCount; i++) {
            if (users.get(i) == null) {
                user.setSeatId(i);
                user.setTableId(tableId);
                users.set(i, user);

                //广播消息
                Map<String, Object> map = new HashMap<>();
                map.put("userId", user.getUserId());
                map.put("seatId", user.getSeatId());
                map.put("nick", user.getNick());
                map.put("avatarUrl", user.getAvatarUrl());
                map.put("tableId", tableId);

                RevMsgUtils.revMsg(DataService.users.values(), CmdConstant.BROADCAST_SIT_DOWN, map);
                break;
            }
        }
    }

    void configPcMap(Integer userId) {

        pcMap.putIfAbsent(userId, 0);
        if (getPcUserId().equals(userId)) {
            pcMap.put(userId, pcMap.get(userId) + 1);
        }
    }

    private boolean isPc() {
        return !pcMap.isEmpty();
    }

    Integer getPcCount(Integer userId) {
        if (pcMap.get(userId)== null)
            return 0;
        return pcMap.get(userId);
    }

    Integer getPcUserId() {
        if (isPc()) {
            for (Map.Entry<Integer, Integer> entry : pcMap.entrySet()) {
                if (entry != null)
                   return entry.getKey();
            }
        }
        return 0;
    }

    void clearPcMap() {
        pcMap.clear();
    }

    private void addVirtualUser(int num) {
        for (int i = 0; i < num; i++) {
            User u = new User(cardService, 2000);
            u.setUserId(DataService.users.size() + 1);
            u.setNick("测试用户" + u.getUserId());
            u.setMoney(0);
            DataService.users.put(u.getUserId(), u);

            u.sitDown(tableId, null);
        }
    }

    private int readyCount() {
        int i = 0;
        for (User user : users) {
            if (user != null && user.getStatus() == CommonConstant.USER_STATUS.READY)
                i++;
        }
        return i;
    }

    private boolean isFull() {
        return getRealCount() == maxCount;
    }

    Table(CardService cardService, AsyncSQLClient mysqlClient) {

        this.cardService = cardService;
        this.tableId = (int) ((Math.random() * 9 + 1) * 100000);
        users = new ArrayList<>();
        for (int i = 0; i < maxCount; i++) {
            users.add(i, null);
        }
        this.mysqlClient = mysqlClient;
        timer.schedule(new AutoExecuteTask(), 1000, 1000);
    }

    public Table(boolean experience, CardService cardService, AsyncSQLClient mysqlClient) {
        this(cardService, mysqlClient);
        this.experience = experience;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                addVirtualUser(3);
            }
        }, 300);
    }

    private boolean canDismiss() {
        int count = 0;
        int dismissCount = 0;
        for (User user : users) {
            if (user != null && user.getNetSocket() != null) {
                count++;
                if (user.isDismiss()) {
                    dismissCount++;
                }
            }

        }
        return dismissCount >= (count - 1);
    }

    private int getRealCount() {
        int count = 0;
        for (User user : users) {
            if (user != null) {
                count++;
            }
        }
        return count;
    }

    boolean isExperience() {
        return experience;
    }

    int getGameRound() {
        return gameRound;
    }

    public void clearRound() {
        this.gameRound = 0;
    }

    public void addGameRound() {
        this.gameRound++;
    }

    void setMaxGameRound(int maxGameRound) {
        this.maxGameRound = maxGameRound;
    }

    int getMaxGameRound() {
        return maxGameRound;
    }

    void setRadio(int radio) {
        this.radio = radio;
    }

    public int getRadio() {
        return radio;
    }

    List<User> getHuUsers() {
        return huUsers;
    }

    void calculateAction() {
        User nextUser = currentActionUser;
        for (int i = 0; i < 3; i++) {
            nextUser = getNextUser(nextUser);
            if (nextUser.canHU(currentPoke)) {
                //hu_tasks.add(nextUser.actionMap(CommonConstant.ACTION_TYPE.HU, currentPoke));
            }
            if (!isPc()) {
                if (nextUser.canGang(currentPoke, false)) {
                    action_tasks.add(nextUser.actionMap(CommonConstant.ACTION_TYPE.GANG, currentPoke));
                }
                if (nextUser.canPen(currentPoke)) {
                    action_tasks.add(nextUser.actionMap(CommonConstant.ACTION_TYPE.PEN, currentPoke));
                }
            }

        }
    }

    void nextStep() {

        if (hu_tasks.size() == 0) {
            huUsers.clear();
        }
        if (hu_tasks.size() > 0) {
            hu_tasks.forEach(single -> {
                Integer userId = (Integer) single.get("userId");
                User huUser = DataService.users.get(userId);
                huUsers.add(huUser);
                RevMsgUtils.revMsg(huUser, CmdConstant.REV_ACTION_CARD, single);
                huUser.autoGiveUpPoke();
            });
            hu_tasks.clear();
        } else if (action_tasks.size() > 0) {
            Map<String, Object> single = action_tasks.remove(0);
            Integer userId = (Integer) single.get("userId");
            User actionUser = DataService.users.get(userId);
            RevMsgUtils.revMsg(actionUser, CmdConstant.REV_ACTION_CARD, single);
            actionUser.autoGiveUpPoke();
        } else {
            User nextUser = getNextUser(currentActionUser);
            Map<String, Object> chi_poke_map = nextUser.canChi(currentPoke);
            if ((currentChiUser == null || nextUser != currentChiUser) && chi_poke_map != null && !isPc()) {
                currentChiUser = nextUser;
                RevMsgUtils.revMsg(nextUser, CmdConstant.REV_ACTION_CARD, chi_poke_map);
                nextUser.autoGiveUpPoke();
            } else {
                System.out.println("7");
                currentChiUser = null;
                nextUser.catchCard();
            }
        }
        System.out.println("8");
    }

    private User getNextUser(User user) {
        int nextActionSeatId = 0;
        if (user.getSeatId() < 3) {
            nextActionSeatId = user.getSeatId() + 1;
        }

        return users.get(nextActionSeatId);
    }

    private void startGame() {
        cardService.initCard(tableId);
        gameRound++;
        Map<String, Object> data = new HashMap<>();
        data.put("currentGameRound", gameRound);
        List<Map<String, Object>> userMaps = new ArrayList<>();
        users.stream().filter(Objects::nonNull)
                .forEach(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("userId", user.getUserId());
                    userMap.put("seatId", user.getSeatId());
                    userMap.put("hog", user.getHog());
                    userMaps.add(userMap);
                });

        data.put("users", userMaps);
        RevMsgUtils.revMsg(users, CmdConstant.BROADCAST_START_GAME, data);

        User admin = DataService.users.get(adminUserId);
        if (admin != null) {
            admin.money -= 10;
            JsonArray params = new JsonArray().add(admin.getUserId());
            mysqlClient.getConnection(connectionAsyncResult -> {
                if (connectionAsyncResult.succeeded()) {
                    connectionAsyncResult.result().queryWithParams("UPDATE nee_users SET diamonds = diamonds - 1 WHERE id = ?;",
                            params, resultSetAsyncResult -> {
                            connectionAsyncResult.result().close();
                    });
                }
            });

        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                users.stream().filter(Objects::nonNull)
                        .forEach(user -> {
                            cardService.dealCards(user, 13);
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("currentGameRound", gameRound);
                            userMap.put("userId", user.getUserId());
                            userMap.put("seatId", user.getSeatId());
                            userMap.put("hog", user.getHog());
                            userMap.put("pokes", user.getPokes());
                            RevMsgUtils.revMsg(user, CmdConstant.REV_START_GAME, userMap);
                            user.setStatus(CommonConstant.USER_STATUS.PLAYING);
                        });
            }
        }, 2000);


    }

    private void createHog() {
        Random random = new Random();
        int seatId = random.nextInt(4);
        User hogUser = users.get(seatId);
        hogUser.setHog(1);
        int d1 = 0, d2 = 0;
        while (d1 + d2 < 6) {
            d1 = random.nextInt(6) + 1;
            d2 = random.nextInt(6) + 1;
        }

        Map<String, Integer> diceMap = new HashMap<>();
        diceMap.put("userId", hogUser.getUserId());
        diceMap.put("dice1", d1);
        diceMap.put("dice2", d2);

        RevMsgUtils.revMsg(users, CmdConstant.BROADCAST_USER_DICE, diceMap);
    }

    private boolean hasHog() {
        for (User user : users) {
            if (user.getHog() == 1) {
                return true;
            }
        }
        return false;
    }

    void removeUser(User user) {

        users.set(user.getSeatId(), null);
    }

    void huCard() {

        users.forEach(u -> {
            u.setHog(0);
            if (huUsers.contains(u)) {

            }
            u.clearPokes();
        });
        //huUsers.get(0).setHog(1);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                tache = CommonConstant.TABLE_TACHE.READY;
            }
        }, 5000);

    }

    private class AutoExecuteTask extends TimerTask {

        @Override
        public void run() {
            if (isEnd()) {
                users.stream().filter(user -> user != null)
                        .forEach(User::standUp);

                return;
            }
            if (!isFull()) {
                return;
            }
            if (tache == CommonConstant.TABLE_TACHE.READY) {
                tache = CommonConstant.TABLE_TACHE.PLAYING;

                // offline user auto ready
                users.stream().filter(user -> user.getNetSocket() == null).forEach(User::ready);
                // all people are ready
                if (readyCount() == maxCount) {
                    if (!hasHog()) {
                        createHog();
                    }
                    startGame();

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("find hog of user --> start ");
                            users.stream().filter(Objects::nonNull)
                                    .forEach(user -> {
                                        System.out.println("user id is: " + user.getUserId() + " , hog: " + user.getHog());
                                        if (user.getHog() == 1) {
                                            user.catchCard();
                                        }
                                    });
                        }
                    }, 2200);

                }

            } else if (tache == CommonConstant.TABLE_TACHE.PLAYING) {

                if (canDismiss() && !isExperience()) {
                    users.stream().filter(user -> user != null)
                            .forEach(User::standUp);
                }
            }

        }
    }


    public static void main(String args[]) {

        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            System.out.println(random.nextInt(4));
        }
    }

}
