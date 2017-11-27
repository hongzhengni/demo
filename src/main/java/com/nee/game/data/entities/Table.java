package com.nee.game.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nee.game.common.constant.CmdConstant;
import com.nee.game.common.constant.CommonConstant;
import com.nee.game.service.CardService;
import com.nee.game.service.DataService;
import com.nee.game.uitls.RevMsgUtils;

import java.util.*;


public class Table {

    private static final int maxCount = 4;
    private int maxGameRound = 3;
    private int radio = 8;
    private int tableId;
    private List<User> users;

    private CardService cardService;

    @JsonIgnore
    public int tache = 0;

    private int gameRound = 0;

    public List<User> getUsers() {
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

    public void addVirtualUser(int num) {
       for (int i = 0; i < num; i++) {
           User u = new User(cardService);
            u.setUserId(DataService.users.size() + 1);
            u.setNick("测试用户" + u.getUserId());
            u.setMoney(1000);
            DataService.users.put(u.getUserId(), u);

            u.sitDown(tableId);
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

    public Table(int tableId, CardService cardService) {
        this.cardService = cardService;
        this.tableId = tableId;
        users = new ArrayList<>();
        for (int i = 0; i < maxCount; i++) {
            users.add(i, null);
        }
        Timer timer = new Timer();
        timer.schedule(new AutoExecuteTask(), 1000, 1000);
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

    public int getGameRound() {
        return gameRound;
    }

    public void clearRound() {
        this.gameRound = 0;
    }

    public void addGameRound() {
        this.gameRound++;
    }

    public void setMaxGameRound(int maxGameRound) {
        this.maxGameRound = maxGameRound;
    }

    public int getMaxGameRound() {
        return maxGameRound;
    }

    public void setRadio(int radio) {
        this.radio = radio;
    }

    public int getRadio() {
        return radio;
    }

    private class AutoExecuteTask extends TimerTask {

        @Override
        public void run() {
            // TODO all people offline

            if (!isFull() || isEnd()) {
                return;
            }
            if (tache == CommonConstant.TABLE_TACHE.READY) {
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

                // offline user auto ready
                users.stream().filter(user -> user.getNetSocket() == null).forEach(User::ready);
                // all people are ready
                if (readyCount() == maxCount) {

                    cardService.initCard(tableId);
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

                    System.out.println("find hog of user --> start " );
                    users.stream().filter(Objects::nonNull)
                            .forEach(user -> {
                                System.out.println("user id is: " + user.getUserId() + " , hog: " + user.getHog());
                                if (user.getHog() == 1) {
                                    user.catchCard();
                                }
                            });
                    tache = CommonConstant.TABLE_TACHE.PLAYING;
                }

            }

        }
    }

    void nextPeople(int userId) {
        User nextUser = getNextUser(userId);

        nextUser.catchCard();
    }

    private User getNextUser(int userId) {
        int nextActionSeatId = 0;
        if (userId < 3) {
            nextActionSeatId = userId + 1;
        }

        return users.get(nextActionSeatId);
    }

    public static void main(String args[]) {

        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            System.out.println(random.nextInt(4));
        }
    }

}
