package com.nee.game.data.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nee.game.common.constant.CmdConstant;
import com.nee.game.common.constant.CommonConstant;
import com.nee.game.common.constant.ErrorCodeEnum;
import com.nee.game.common.exception.BusinessException;
import com.nee.game.service.CardService;
import com.nee.game.service.DataService;
import com.nee.game.uitls.RevMsgUtils;
import io.vertx.core.net.NetSocket;

import java.util.*;

public class User implements Comparable<User> {

    private CardService cardService;
    private Integer userId;
    private String nick;
    private int money = 1000;

    private String avatarUrl;

    private int originalMoney;
    private int status;//0未准备状态 1准备状态 2在玩状态 3旁观者

    private int seatId;
    private int tableId;

    private List<Byte> pokes;

    private Map<Byte, Integer> countMap = new HashMap<>();

    @JsonIgnore
    private NetSocket netSocket;

    private int hog = 0;


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

    /**
     * －－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－
     **/

    private User(List<Byte> pokes) {
        this.pokes = pokes;
    }

    public User(CardService cardService) {
        this.cardService = cardService;
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
        this.tableId = 0;
        this.seatId = 0;
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
        if (this.money > o.money) {
            return -1;
        } else if (this.money < o.money) {
            return 1;
        }
        return 0;
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
            int g_num = gang_pokes.size() % 4;
            if (p_c > 13 + g_num) {
                return false;
            }
        }
        return true;
    }

    boolean canHU(Byte poke) {

        return sevenPairHu(poke) || commonHu(poke);

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
            if (pokes.contains((byte) (f_poke + 1)) && pokes.contains((byte) (f_poke + 2))) {
                pokes.remove(pokes.indexOf(f_poke));
                pokes.remove(pokes.indexOf((byte) (f_poke + 1)));
                pokes.remove(pokes.indexOf((byte) (f_poke + 2)));

                return huPaiPanDin(pokes);
            }
            return false;
        }
    }

    private boolean commonHu(byte poke) {
        List<Byte> pairs = new ArrayList<>(this.pokes);

        pairs.add(poke);
        //只有两张牌
        if (pairs.size() == 2) {
            return pairs.get(0).equals(pairs.get(1));
        }
        //先排序
        Collections.sort(pairs);

        Set<Byte> keys = countMap.keySet();
        for (Byte key : keys) {
            if (countMap.get(key) > 1) {
                List<Byte> pairT = new ArrayList<>(pairs);
                pairT.remove(key);
                pairT.remove(key);

                if (huPaiPanDin(pairT)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean sevenPairHu(Byte poke) {
        List<Byte> n_p = new ArrayList<>();
        n_p.addAll(pokes);
        n_p.add(poke);
        Collections.sort(n_p);

        n_p.forEach(p -> {
            countMap.putIfAbsent(p, 0);
            countMap.put(p, countMap.get(p) + 1);
        });
        if (n_p.size() == 14) {
            for (int i = 0; i < n_p.size() - 1; i += 2) {
                if (!Objects.equals(n_p.get(i), n_p.get(i + 1))) {
                    return false;
                }
            }

        }
        return true;
    }


    boolean canGang(Byte poke) {
        return countMap.get(poke) == 4;
    }

    boolean canPen(Byte poke) {
        return countMap.get(poke) == 3;
    }

    Map<String, Object> canChi(Byte poke) {

        Map<String, Object> map = new HashMap<>();
        map.put("type", CommonConstant.ACTION_TYPE.CHI);

        List<Map<String, Object>> categories = new ArrayList<>();
        if (this.pokes.indexOf((byte) (poke + 1)) > 0 && this.pokes.indexOf((byte) (poke + 2)) > 0) {
            Byte[] bytes = new Byte[]{poke, (byte) (poke + 1), (byte) (poke + 2)};

            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("location", 1);
            categoryMap.put("pokes", bytes);
            categories.add(categoryMap);
        }
        if (this.pokes.indexOf((byte) (poke - 1)) > 0 && this.pokes.indexOf((byte) (poke + 1)) > 0) {
            Byte[] bytes = new Byte[]{(byte) (poke + 1), poke, (byte) (poke - 1)};
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("location", 2);
            categoryMap.put("pokes", bytes);
            categories.add(categoryMap);
        }
        if (this.pokes.indexOf((byte) (poke - 1)) > 0 && this.pokes.indexOf((byte) (poke - 2)) > 0) {
            Byte[] bytes = new Byte[]{poke, (byte) (poke - 1), (byte) (poke - 2)};
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("location", 3);
            categoryMap.put("pokes", bytes);
            categories.add(categoryMap);
        }
        if (categories.size() <= 0) {
            return null;
        }
        map.put("userId", userId);
        map.put("detail", categories);
        return map;
    }


    public void ready() {
        Table currentTable = DataService.tables.get(tableId);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);
        this.setStatus(CommonConstant.USER_STATUS.READY);
        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_USER_READY, data);
    }

    public void sitDown(Integer tableId) {
        Table currentTable = DataService.tables.get(tableId);

        if (currentTable == null) {
            throw new BusinessException(ErrorCodeEnum.DATA_NOT_EXIST);
        }

        currentTable.addUser(this);

        Map<String, Object> data = new HashMap<>();
        data.put("tableId", currentTable.getTableId());
        data.put("tableNo", "测试房间" + currentTable.getTableId());
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

        RevMsgUtils.revMsg(this, CmdConstant.REV_ROOM_INFO, data);
    }

    void catchCard() {
        if (!shouldCatch()) {
            return;
        }
        Table currentTable = DataService.tables.get(tableId);
        Byte poke = cardService.dealCard(tableId);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);
        System.out.println("start deal card -> step: 1");
        RevMsgUtils.revMsg(currentTable.getUsers(), this, CmdConstant.BROADCAST_CATCH_CARD, data);

        System.out.println("start deal card -> step: 2");
        List<Map<String, Object>> choices = new ArrayList<>();

        System.out.println("start deal card -> step: 3");
        this.pokes.add(poke);
        data.put("poke", poke);

        RevMsgUtils.revMsg(this, CmdConstant.BROADCAST_CATCH_CARD, data);

        if (choices.size() > 0) {
            data.put("choice", choices);
            RevMsgUtils.revMsg(this, CmdConstant.REV_ACTION_CARD, data);
        }
        autoPlay();
    }

    public void playCard(Byte poke) {

        Table currentTable = DataService.tables.get(tableId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);
        data.put("poke", poke);

        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_PLAY_CARD, data);

        currentTable.nextStep(this, poke);
    }

    private Map<String, Object> choiceData(Byte poke, boolean self) {

        List<Map<String, Object>> choices = new ArrayList<>();

        if (canHU(poke)) {
            Map<String, Object> choiceMap = new HashMap<>();
            choiceMap.put("type", CommonConstant.ACTION_TYPE.HU);
            Byte[] pokes = new Byte[1];
            pokes[0] = poke;
            choiceMap.put("pokes", pokes);
            choices.add(choiceMap);
        }
        if (canGang(poke)) {
            Map<String, Object> choiceMap = new HashMap<>();
            choiceMap.put("type", CommonConstant.ACTION_TYPE.GANG);
            Byte[] pokes = new Byte[1];
            pokes[0] = poke;
            choiceMap.put("pokes", pokes);
            choices.add(choiceMap);
        }
        if (!self) {
            if (canPen(poke)) {
                Map<String, Object> choiceMap = new HashMap<>();
                choiceMap.put("type", CommonConstant.ACTION_TYPE.PEN);
                Byte[] pokes = new Byte[1];
                pokes[0] = poke;
                choiceMap.put("pokes", pokes);
                choices.add(choiceMap);
            }
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

        RevMsgUtils.revMsg(currentTable.getUsers(), this, CmdConstant.BROADCAST_ACTION_CARD, data);

        autoPlay();
    }

    public void penCard(List<Byte> pokes) {
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
        Table currentTable = DataService.tables.get(tableId);
        if (gang_pokes == null) {
            gang_pokes = pokes;
        } else {
            gang_pokes.addAll(pokes);
        }
        this.pokes.removeAll(pokes);

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

    }

    public void standUp() {

        Table currentTable = DataService.tables.get(tableId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);

        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_STAND_UP, data);

        clear();
    }


    private void autoPlay() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!shouldCatch()) {
                    Byte poke = pokes.remove(pokes.size() - 1);
                    playCard(poke);
                }
            }
        }, 5000);

    }

    public static void main(String[] args) {

        Byte[] bytes = {0x01, 0x01, 0x1, 0x01, 0x04, 0x04, 0x07, 0x07, 0x08, 0x09, 0x09, 0x09, 0x09};

        List<Byte> bs = Arrays.asList(bytes);

        System.out.println(bs.indexOf((byte) 1));

        User user = new User(Arrays.asList(bytes));

        System.out.println(user.shouldCatch());

        System.out.println(user.canHU((byte) 0x08));

    }
}

