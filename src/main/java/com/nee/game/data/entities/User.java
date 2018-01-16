package com.nee.game.data.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nee.game.common.A0Json;
import com.nee.game.common.PokeData;
import com.nee.game.common.constant.CmdConstant;
import com.nee.game.common.constant.CommonConstant;
import com.nee.game.common.constant.ErrorCodeEnum;
import com.nee.game.common.exception.BusinessException;
import com.nee.game.service.CardService;
import com.nee.game.service.DataService;
import com.nee.game.uitls.RevMsgUtils;
import io.vertx.core.json.Json;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;

public class User implements Comparable<User> {

    private CardService cardService;
    private Integer userId;
    private String nick;
    int money = 0;

    private int grade = 0;

    private String avatarUrl;

    private int status;//0未准备状态 1准备状态 2在玩状态 3旁观者

    private Integer seatId;
    private Integer tableId;

    private boolean dismiss = false;

    private int winCount = 0;

    private int serialHu = 0;

    private int pcCount = 0;

    private List<Byte> pokes;

    private Map<Byte, Integer> countMap = new HashMap<>();

    @JsonIgnore
    private NetSocket netSocket;

    private int hog = 0;

    private Timer timer = new Timer();

    private int huType;

    private int ratio = 1;

    private Map<Integer, Integer> chiCountMap = new HashMap<>();


    private boolean hasAction = false;

    private int gapTime = Integer.MAX_VALUE;


    /**
     * －－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－
     **/
    @JsonIgnore
    private int gameCount = 0;
    @JsonIgnore
    private List<Byte> chi_pokes;
    @JsonIgnore
    private List<Byte> pen_pokes;
    @JsonIgnore
    private List<Byte> gang_pokes;
    @JsonIgnore
    private List<Byte> play_pokes = new ArrayList<>();

    /**
     * －－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－
     **/

    private User(List<Byte> pokes) {
        this.pokes = pokes;
    }

    public User(CardService cardService) {
        this.cardService = cardService;
    }

    User(CardService cardService, int gapTime) {
        this(cardService);
        this.gapTime = gapTime;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /*  public User(int userId, String nick, int originalMoney, CardService cardService) {
        this.userId = userId;
        this.nick = nick;
        this.setOriginalMoney(originalMoney);
        this.cardService = cardService;
    }*/

    private void clear() {
        this.setMoney(0);
        this.setStatus(0);
        this.winCount = 0;
        this.serialHu = 0;
        this.grade = 0;
        this.tableId = null;
        this.seatId = null;
        this.dismiss = false;
        try {
            timer.cancel();
        } catch (Exception e) {
            System.out.println("It does not matter");
        }
        clearPokes();
    }

    private void clearGame() {
        this.setMoney(0);
        this.setStatus(0);
        this.dismiss = false;
        try {
            timer.cancel();
        } catch (Exception e) {
            System.out.println("It does not matter");
        }
        clearPokes();
        chiCountMap.clear();
    }

    void clearPokes() {
        if (play_pokes != null) {
            play_pokes.clear();
        }
        if (gang_pokes != null) {
            gang_pokes.clear();
        }
        if (pen_pokes != null) {
            pen_pokes.clear();
        }
        if (chi_pokes != null) {
            chi_pokes.clear();
        }
        if (pokes != null) {
            pokes.clear();
        }
    }

    private int countPokes() {
        int count = 0;
        if (chi_pokes != null) {
            count += chi_pokes.size();
        }
        if (pen_pokes != null) {
            count += pen_pokes.size();
        }
        if (gang_pokes != null) {
            count += gang_pokes.size();
        }
        if (pokes != null) {
            count += pokes.size();
        }
        return count;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    private int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    String getAvatarUrl() {
        return avatarUrl;
    }

    int getStatus() {
        return status;
    }

    void setStatus(int status) {
        this.status = status;
    }

    Integer getSeatId() {
        return seatId;
    }

    void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public Integer getTableId() {
        return tableId;
    }

    void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public NetSocket getNetSocket() {
        return netSocket;
    }

    public void setNetSocket(NetSocket netSocket) {
        this.netSocket = netSocket;
    }

    List<Byte> getPokes() {
        return pokes;
    }

    public void setPokes(List<Byte> pokes) {
        this.pokes = pokes;
    }

    @Override
    public int compareTo(User o) {
        if (this.grade > o.grade) {
            return -1;
        } else if (this.grade < o.grade) {
            return 1;
        }
        return 0;
    }

    boolean isDismiss() {
        return this.dismiss;
    }

    int getHog() {
        return hog;
    }

    void setHog(int hog) {
        this.hog = hog;
    }

    private boolean shouldCatch() {
        int p_c = countPokes();
        if (pokes.size() > 13) return false;

        if (gang_pokes == null || gang_pokes.size() == 0) {
            return (p_c < 14);
        } else {
            int g_num = gang_pokes.size() / 4;
            if (p_c > (13 + g_num)) {
                return false;
            }
        }
        return true;
    }

    boolean canHU(Byte poke) {

        countMap.clear();
        List<Byte> n_p = new ArrayList<>(pokes);
        n_p.add(poke);

        n_p.forEach(p -> {
            countMap.putIfAbsent(p, 0);
            countMap.put(p, countMap.get(p) + 1);
        });

        return combineYoriko(n_p);
    }

    private static boolean huPaiPanDin(List<Byte> pokes) {
        if (pokes.size() == 0) {
            return true;
        }
        if (pokes.size() < 3) {
            return false;
        }

        Byte f_poke = pokes.get(0);
        //组成克子
        if (pokes.get(0).equals(pokes.get(1)) && pokes.get(1).equals(pokes.get(2))) {
            pokes.remove(f_poke);
            pokes.remove(f_poke);
            pokes.remove(f_poke);
            return huPaiPanDin(pokes);
        } else { //组成顺子
            if (!(f_poke.equals(PokeData.EAST) || f_poke.equals(PokeData.SOUTH) || f_poke.equals(PokeData.WEST)
                    || f_poke.equals(PokeData.NORTH) || f_poke.equals(PokeData.CENTER))
                    && pokes.contains((byte) (f_poke + 1)) && pokes.contains((byte) (f_poke + 2))) {
                pokes.remove(pokes.indexOf(f_poke));
                pokes.remove(pokes.indexOf((byte) (f_poke + 1)));
                pokes.remove(pokes.indexOf((byte) (f_poke + 2)));

                return huPaiPanDin(pokes);
            }
            return false;
        }
    }

    private boolean cmnHu(List<Byte> pokes) {
        List<Byte> pairs = new ArrayList<>(pokes);
        //只有两张牌
        if (pairs.size() == 2) {
            return pairs.get(0).equals(pairs.get(1));
        }
        //先排序
        Collections.sort(pairs);

        Map<Byte, Integer> countMap = new HashMap<>();

        for (Byte pair : pairs) {
            countMap.putIfAbsent(pair, 0);
            countMap.put(pair, countMap.get(pair) + 1);
        }
        Set<Byte> keys = countMap.keySet();
        for (Byte key : keys) {
            if (countMap.get(key) > 1) {
                List<Byte> pairT = new ArrayList<>(pairs);
                pairT.remove(key);
                pairT.remove(key);

                if (huPaiPanDin(pairT)) {

                    huType = CommonConstant.HU_TYPE.COMMON_HU;
                    Integer pcCount = DataService.tables.get(tableId).getPcCount(this.userId);
                    if (pcCount > 0) {
                        huType += pcCount;
                        ratio *= Math.pow(2, pcCount);
                    }

                    return true;
                }
            }
        }
        return false;
    }

    private boolean combineYoriko(List<Byte> pokes) {

        if (sevenPairHu(pokes) || cmnHu(pokes)) {
            return true;
        }
        List<Byte> n_n = new ArrayList<>(pokes);
        if (n_n.indexOf(PokeData.BLANK) >= 0) {
            int idx = n_n.indexOf(PokeData.BLANK);
            for (int i = 0; i < PokeData._Mahjong.length - 1; i++) {
                n_n.set(idx, PokeData._Mahjong[i]);
                if (combineYoriko(n_n)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean sevenPairHu(List<Byte> pokes) {

        if (!CollectionUtils.isEmpty(pen_pokes) || !CollectionUtils.isEmpty(chi_pokes)) {
            return false;
        }

        List<Byte> n_p = new ArrayList<>(pokes);
        Collections.sort(n_p);

        int j;
        for (j = 0; j < n_p.size() - 1; j += 2) {
            if (!Objects.equals(n_p.get(j), n_p.get(j + 1))) {
                break;
            }
        }

        if (j >= (n_p.size() - 1)) {
            huType = CommonConstant.HU_TYPE.SEVEN_PAIRS;
            Collection<Integer> values = countMap.values();
            values.stream().filter(value -> value == 4)
                    .forEach(value -> {
                        huType = CommonConstant.HU_TYPE.SEVEN_PAIRS_S;
                        ratio *= 2;
                    });


            Integer pcCount = DataService.tables.get(tableId).getPcCount(this.userId);
            if (pcCount > 0) {
                huType += pcCount;
                ratio *= Math.pow(2, pcCount);
            }
            return true;
        }
        return false;
    }


    boolean canGang(Byte poke, Boolean isSelf) {
        if (isSelf) {
            return countMap.get(poke) == 4 || (pen_pokes != null && pen_pokes.contains(poke));
        } else {
            return countMap.get(poke) == 4;
        }

    }

    boolean canPen(Byte poke) {
        return !poke.equals((byte) 0x37) && countMap.get(poke) == 3;
    }

    Map<String, Object> actionMap(Integer actionType, Byte poke) {
        Map<String, Object> actionMap = new HashMap<>();
        List<Map<String, Object>> choices = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("type", actionType);
        Byte[] pokes = new Byte[1];
        pokes[0] = poke;
        map.put("pokes", pokes);
        choices.add(map);
        actionMap.put("userId", userId);
        actionMap.put("seatId", seatId);
        actionMap.put("choices", choices);

        return actionMap;
    }

    Map<String, Object> canChi(Byte poke) {

        if (poke < 0x38 && poke > 0x30) {
            return null;
        }

        Table currentTable = DataService.tables.get(tableId);
        if (currentTable.getRadio() == 8 && chiCountMap.get(currentTable.currentPlayUser.userId) != null
                && chiCountMap.get(currentTable.currentPlayUser.userId) > 1) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> choices = new ArrayList<>();
        if (this.pokes.indexOf((byte) (poke + 1)) > 0 && this.pokes.indexOf((byte) (poke + 2)) > 0) {
            Byte[] bytes = new Byte[]{poke, (byte) (poke + 1), (byte) (poke + 2)};

            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("type", CommonConstant.ACTION_TYPE.CHI);
            categoryMap.put("location", 1);
            categoryMap.put("pokes", bytes);
            choices.add(categoryMap);
        }
        if (this.pokes.indexOf((byte) (poke - 1)) > 0 && this.pokes.indexOf((byte) (poke + 1)) > 0) {
            Byte[] bytes = new Byte[]{(byte) (poke + 1), poke, (byte) (poke - 1)};
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("type", CommonConstant.ACTION_TYPE.CHI);
            categoryMap.put("location", 2);
            categoryMap.put("pokes", bytes);
            choices.add(categoryMap);
        }
        if (this.pokes.indexOf((byte) (poke - 1)) > 0 && this.pokes.indexOf((byte) (poke - 2)) > 0) {
            Byte[] bytes = new Byte[]{poke, (byte) (poke - 1), (byte) (poke - 2)};
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("type", CommonConstant.ACTION_TYPE.CHI);
            categoryMap.put("location", 3);
            categoryMap.put("pokes", bytes);
            choices.add(categoryMap);
        }
        if (choices.size() <= 0) {
            return null;
        }
        map.put("userId", userId);
        map.put("seatId", seatId);
        map.put("choices", choices);
        return map;
    }

    public void loginHall() {

        Map<String, Object> data = new HashMap<>();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("tableId", tableId);
        userInfo.put("seatId", seatId);
        data.put("user", userInfo);

        List<Map<String, Object>> tableMaps = new ArrayList<>();
        DataService.tables.values().forEach(table -> {
            if (table.isExperience()) {
                Map<String, Object> map = new HashMap<>();
                map.put("tableId", table.getTableId());
                List<Map<String, Object>> userMaps = new ArrayList<>();
                table.getUsers().stream().filter(Objects::nonNull)
                        .forEach(user -> {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("userId", user.getUserId());
                            userMap.put("nick", user.getNick());
                            userMaps.add(userMap);
                        });
                map.put("users", userMaps);
                tableMaps.add(map);
            }
        });

        data.put("tables", tableMaps);

        RevMsgUtils.revMsg(this, CmdConstant.REV_HALL_INFO, data);
    }


    public void ready() {
        Table currentTable = DataService.tables.get(tableId);

        System.out.println("current table: " + Json.encode(currentTable));
        if (currentTable == null) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);
        this.setStatus(CommonConstant.USER_STATUS.READY);
        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_USER_READY, data);
    }

    public void sitDown(Integer tableId, Integer seatId) {
        Table currentTable = DataService.tables.get(tableId);

        if (currentTable == null) {
            throw new BusinessException(ErrorCodeEnum.DATA_NOT_EXIST);
        }

        Map<String, Object> data = new HashMap<>();
        if (seatId == null || seatId == -1) {
            currentTable.addUser(this);

            data.put("tableId", currentTable.getTableId());
            data.put("tableNo", "房间" + currentTable.getTableId());
            data.put("maxGameRound", currentTable.getMaxGameRound());
            data.put("currentGameRound", currentTable.getGameRound());

            List<Map<String, Object>> userList = new ArrayList<>();
            currentTable.getUsers().stream().filter(Objects::nonNull)
                    .forEach(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userId", user.getUserId());
                        userMap.put("seatId", user.getSeatId());
                        userMap.put("avatarUrl", user.getAvatarUrl());
                        userMap.put("status", user.getStatus());
                        userMap.put("nick", user.getNick());
                        userMap.put("money", user.getMoney());

                        userList.add(userMap);
                    });
            data.put("users", userList);
        } else {
            data.put("tableId", currentTable.getTableId());
            data.put("tableNo", "房间" + currentTable.getTableId());
            data.put("maxGameRound", currentTable.getMaxGameRound());
            data.put("currentGameRound", currentTable.getGameRound());
            data.put("pokes", this.pokes);
            data.put("gangPokes", this.gang_pokes);
            data.put("pengPokes", this.pen_pokes);
            data.put("chiPokes", this.chi_pokes);
            data.put("playPokes", this.play_pokes);
            data.put("remainCardNum", cardService.remainCardNum(currentTable.getTableId()));
            data.put("currentActionSeatId", currentTable.cid);


            List<Map<String, Object>> userList = new ArrayList<>();
            currentTable.getUsers().stream().filter(Objects::nonNull)
                    .forEach(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userId", user.getUserId());
                        userMap.put("seatId", user.getSeatId());
                        userMap.put("avatarUrl", user.getAvatarUrl());
                        userMap.put("status", user.getStatus());
                        userMap.put("nick", user.getNick());
                        userMap.put("money", user.getMoney());
                        userMap.put("pokes", user.pokes == null ? 0 : user.pokes.size());
                        userMap.put("gangPokes", user.gang_pokes);
                        userMap.put("pengPokes", user.pen_pokes);
                        userMap.put("chiPokes", user.chi_pokes);
                        userMap.put("playPokes", user.play_pokes);
                        userList.add(userMap);

                        if (user.getHog() == 1) {
                            data.put("hog", this.seatId);
                        }
                    });
            data.put("users", userList);

            Map<String, Integer> data2 = new HashMap<>();
            data2.put("userId", this.userId);
            data2.put("seatId", this.seatId);

            RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_RECONNECT, data2);
        }


        RevMsgUtils.revMsg(this, CmdConstant.REV_ROOM_INFO, data);
    }

    void catchCard() {
        hasAction = false;

        if (shouldCatch()) {
            Table currentTable = DataService.tables.get(tableId);
            currentTable.cid = this.seatId;
            Byte poke = cardService.dealCard(tableId);

            Map<String, Object> broadcast_data = new HashMap<>();
            broadcast_data.put("userId", userId);
            broadcast_data.put("seatId", seatId);
            RevMsgUtils.revMsg(currentTable.getUsers(), this, CmdConstant.BROADCAST_CATCH_CARD, broadcast_data);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("seatId", seatId);
            Map<String, Object> choiceData = choiceData(poke, true);

            this.pokes.add(poke);
            data.put("poke", poke);

            RevMsgUtils.revMsg(this, CmdConstant.BROADCAST_CATCH_CARD, data);
            if (choiceData != null) {
                hasAction = true;
                RevMsgUtils.revMsg(this, CmdConstant.REV_ACTION_CARD, choiceData);
            } else {
                if (cardService.remainCardNum(currentTable.getTableId()) < 20) {
                    huCard(poke);
                    return;
                }
            }
        }
        autoPlay();
    }

    public void playCard(Byte poke) {
        if (poke == null) {
            poke = pokes.get(pokes.size() - 1);
        }
        hasAction = false;
        timer.cancel();
        Table currentTable = DataService.tables.get(tableId);

        currentTable.currentPlayUser = this;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);
        data.put("poke", poke);

        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_PLAY_CARD, data);

        System.out.println(" current user id is: " + userId + "play card: " + poke);
        System.out.println("before pokes is: " + pokes.toString());

        if (Objects.equals(poke, PokeData.BLANK)) {
            currentTable.configPcMap(this.userId);
        } else {
            if (currentTable.getPcUserId().equals(this.userId)) {
                currentTable.clearPcMap();
            }
        }

        int index = this.pokes.indexOf(poke);
        System.out.println("index: " + index);
        if (index >= 0) {
            this.pokes.remove(index);
            play_pokes.add(poke);
        }
        System.out.println("after pokes: " + pokes.toString());

        currentTable.setCurrentEle(this, poke);
        currentTable.cid = this.seatId;

        currentTable.calculateAction();

        currentTable.nextStep();

    }

    private Map<String, Object> choiceData(Byte poke, Boolean isSelf) {

        List<Map<String, Object>> choices = new ArrayList<>();

        if (canHU(poke)) {
            Map<String, Object> choiceMap = new HashMap<>();
            choiceMap.put("type", CommonConstant.ACTION_TYPE.HU);
            Byte[] pokes = new Byte[1];
            pokes[0] = poke;
            choiceMap.put("pokes", pokes);
            choices.add(choiceMap);
        }
        if (canGang(poke, isSelf)) {
            Map<String, Object> choiceMap = new HashMap<>();
            choiceMap.put("type", CommonConstant.ACTION_TYPE.GANG);
            Byte[] pokes = new Byte[1];
            pokes[0] = poke;
            choiceMap.put("pokes", pokes);
            choices.add(choiceMap);
        }
        if (choices.size() > 0) {
            Map<String, Object> choiceData = new HashMap<>();
            choiceData.put("userId", userId);
            choiceData.put("seatId", seatId);
            choiceData.put("choices", choices);
            return choiceData;
        }

        return null;
    }

    public void chiCard(List<Byte> pokes, int location) {
        timer.cancel();
        Table currentTable = DataService.tables.get(tableId);
        if (chi_pokes == null) {
            chi_pokes = pokes;
        } else {
            chi_pokes.addAll(pokes);
        }
        byte poke = pokes.remove(location - 1);
        this.pokes.removeAll(pokes);
        pokes.add(poke);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);
        Map<String, Object> actionMap = new HashMap<>();
        actionMap.put("type", CommonConstant.ACTION_TYPE.CHI);
        actionMap.put("pokes", pokes);
        data.put("action", actionMap);

        chiCountMap.putIfAbsent(currentTable.currentPlayUser.getUserId(), 0);
        chiCountMap.put(currentTable.currentPlayUser.getUserId(),
                chiCountMap.get(currentTable.currentPlayUser.getUserId() + 1));

        RevMsgUtils.revMsg(currentTable.getUsers(), this, CmdConstant.BROADCAST_ACTION_CARD, data);

        autoPlay();
    }

    public void penCard(List<Byte> pokes) {
        timer.cancel();
        Table currentTable = DataService.tables.get(tableId);
        if (pen_pokes == null) {
            pen_pokes = pokes;
        } else {
            pen_pokes.addAll(pokes);
        }
        this.pokes.remove(pokes.get(0));
        this.pokes.remove(pokes.get(0));

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);
        Map<String, Object> actionMap = new HashMap<>();
        actionMap.put("type", CommonConstant.ACTION_TYPE.PEN);
        actionMap.put("pokes", pokes);
        data.put("action", actionMap);

        RevMsgUtils.revMsg(currentTable.getUsers(), this, CmdConstant.BROADCAST_ACTION_CARD, data);

        autoPlay();

    }

    public void gangCard(List<Byte> pokes) {
        timer.cancel();
        Table currentTable = DataService.tables.get(tableId);
        if (gang_pokes == null) {
            gang_pokes = pokes;
        } else {
            gang_pokes.addAll(pokes);
        }
        if (this.pokes.contains(pokes.get(0))) {
            this.pokes.removeAll(pokes);
        } else {
            this.pen_pokes.removeAll(pokes);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);
        Map<String, Object> actionMap = new HashMap<>();
        actionMap.put("type", CommonConstant.ACTION_TYPE.GANG);
        actionMap.put("pokes", pokes);
        data.put("action", actionMap);

        RevMsgUtils.revMsg(currentTable.getUsers(), this, CmdConstant.BROADCAST_ACTION_CARD, data);

        catchCard();
    }

    public void huCard(Byte poke) {
        timer.cancel();
        Table currentTable = DataService.tables.get(tableId);
        currentTable.getUsers().forEach(user -> {
            if (this == user) {
                user.pokes.add(poke);
                user.serialHu++;
                user.winCount++;

                int grade = (int) Math.pow(2, 0) * user.ratio * currentTable.getRadio();
                if (user.getHog() == 1) {
                    grade *= 2;
                }
                user.grade += grade * 3;

                calculateGrade(currentTable, user, grade);
            }
        });

        System.out.println("********************* can hu users ************ \n: " + A0Json.encode(currentTable.getHuUsers()));

        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> userMaps = new ArrayList<>();
        int huType = 0;

        for (User user : currentTable.getUsers()) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("seatId", user.getSeatId());
            userMap.put("hu", false);
            if (this == user) {
                userMap.put("hu", true);
                huType = user.huType;
            } else {
                user.serialHu = 0;
            }
            List<Byte> userPokes = new ArrayList<>(user.pokes);
            /*if (user.chi_pokes != null) {
                userPokes.addAll(user.chi_pokes);
            }
            if (user.pen_pokes != null) {
                userPokes.addAll(user.pen_pokes);
            }
            if (user.gang_pokes != null) {
                userPokes.addAll(user.gang_pokes);
            }*/

            userMap.put("pokes", userPokes);
            userMap.put("grade", user.grade);
            userMaps.add(userMap);
        }


        System.out.println("当前huType：" + huType);
        if (huType < 10) {
            //huType = 50;
        }

        data.put("huType", huType);
        data.put("users", userMaps);

        currentTable.huCard();

        this.setHog(1);

        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_HU_CARD, data);
    }


    public void giveUpPoke() {
        timer.cancel();
        hasAction = false;
        Table currentTable = DataService.tables.get(tableId);
        currentTable.nextStep();
    }

    public void standUp() {

        Table currentTable = DataService.tables.get(tableId);
        if (currentTable == null)
            return;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);

        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_STAND_UP, data);


        currentTable.removeUser(this);
        clear();
    }

    public void dismiss() {

        Table currentTable = DataService.tables.get(tableId);
        this.dismiss = true;

        Map<String, Integer> data = new HashMap<>();
        data.put("userId", this.userId);

        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_DISMISS, data);
    }

    public void applySettle() {
        Table currentTable = DataService.tables.get(tableId);
        Map<String, Object> data = new HashMap<>();
        data.put("gameCount", currentTable.getGameRound());

        List<Map<String, Integer>> userMaps = new ArrayList<>();

        List<User> n_n = new ArrayList<>(currentTable.getUsers());
        Collections.sort(n_n);

        n_n.stream().filter(Objects::nonNull)
                .forEach(user -> {
                    Map<String, Integer> userMap = new HashMap<>();
                    userMap.put("userId", user.getUserId());
                    userMap.put("seatId", user.getSeatId());
                    userMap.put("winCount", winCount);
                    userMap.put("invalidCount", currentTable.getInvalidCount());
                    userMap.put("loseCount", currentTable.getGameRound() - winCount - currentTable.getInvalidCount());
                    userMap.put("grade", user.grade);
                    userMaps.add(userMap);
                });

        data.put("users", userMaps);
        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.REV_APPLY_SETTLE, data);
    }

    public void chat(String content) {

        Table currentTable = DataService.tables.get(tableId);
        if (currentTable == null) {
            throw new BusinessException(ErrorCodeEnum.DATA_NOT_EXIST);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);
        data.put("content", content);

        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_CHAT, data);

    }

    public void disConnect() {

        if (CollectionUtils.isEmpty(this.pokes)) {
            standUp();
        } else {
            if (hasAction) {
                giveUpPoke();
            }
            Table currentTable = DataService.tables.get(tableId);
            if (currentTable.isExperience()) {
                currentTable.clearRound();
                currentTable.getUsers().stream().filter(user -> user != this)
                        .forEach(User::clearGame);
                this.standUp();
            } else {
                Map<String, Integer> data = new HashMap<>();
                data.put("userId", this.userId);
                data.put("seatId", this.seatId);
                RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_DISCONNECT, data);
            }
        }
    }

    public void createRoom(int radio, int maxGround, AsyncSQLClient mysqlClient) {

        Table currentTable = new Table(cardService, mysqlClient);
        currentTable.setMaxGameRound(maxGround);
        currentTable.setRadio(radio);
        currentTable.adminUserId = userId;
        DataService.tables.put(currentTable.getTableId(), currentTable);

        Map<String, Integer> data = new HashMap<>();
        data.put("tableId", currentTable.getTableId());
        data.put("userId", userId);


        RevMsgUtils.revMsg(this, CmdConstant.BROADCAST_CREATE_ROOM, data);
    }

    private void calculateGrade(Table table, User user, double grade) {

        if (table == null)
            return;
        table.getUsers().stream().filter(u -> u != user && u != null)
                .forEach(u -> {
                    u.grade -= grade;
                });
    }

    private void autoPlay() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!shouldCatch()) {
                    playCard(null);
                }
            }
        }, gapTime);

    }

    void autoGiveUpPoke() {
        hasAction = true;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                giveUpPoke();
            }
        }, gapTime);
    }


    public static void main(String[] args) {

        List pokes = new ArrayList(Arrays.asList(new Byte[]{39, 55, 38, 36}));

        User user = new User(pokes);

        System.out.println(user.canHU((byte) 24));

        pokes.removeAll(Arrays.asList((byte) 39, (byte) 39));

        System.out.println(pokes.toString());

    }


}

