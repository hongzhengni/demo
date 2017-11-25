package com.nee.game.data.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nee.game.common.A0Json;
import com.nee.game.common.constant.CmdConstant;
import com.nee.game.common.constant.CommonConstant;
import com.nee.game.service.DataService;
import com.nee.game.uitls.RevMsgUtils;
import io.vertx.core.json.Json;
import io.vertx.core.net.NetSocket;

import java.util.*;

public class User implements Comparable<User> {
    private Integer userId;
    private String nick;
    private int money = 1000;

    private String avatarUrl;

    private int originalMoney;
    private int status;//0未准备状态 1准备状态 2在玩状态 3旁观者

    private int seatId;
    private int tableId;

    private List<Byte> pokes;

    @JsonIgnore
    private NetSocket netSocket;

    private int hog = 0;


    /**
     * －－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－
     **/
    @JsonIgnore
    private int gameCount = 0;
    @JsonIgnore
    public int winCount = 0;
    @JsonIgnore
    public boolean offline = false;
    @JsonIgnore
    public List<Byte> chi_pokes;
    @JsonIgnore
    public List<Byte> pen_pokes;
    @JsonIgnore
    public List<Byte> gang_pokes;

    /**
     * －－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－
     **/

    public User() {

    }

    public User(int userId, String nick, int originalMoney, boolean robot) {
        this.userId = userId;
        this.nick = nick;
        this.setOriginalMoney(originalMoney);
    }

    public void clear() {
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

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public Integer getSeatId() {
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

    public int getOriginalMoney() {
        return originalMoney;
    }

    public int getHog() {
        return hog;
    }

    public void setHog(int hog) {
        this.hog = hog;
    }

    public void setOriginalMoney(int originalMoney) {
        this.originalMoney = originalMoney;
    }

    public void playCard(Byte poke) {

        Table currentTable = DataService.tables.get(tableId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);
        data.put("poke", poke);

        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_PLAY_CARD, data);

        currentTable.getUsers().stream().filter(Objects::nonNull)
                .forEach(user ->{

                });

        currentTable.nextPeople(userId);
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

    public void standUp() {

        Table currentTable = DataService.tables.get(tableId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("seatId", seatId);

        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_STAND_UP, data);

        clear();
    }


    void autoPlay() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (countPokes() == 14) {
                    Byte poke = pokes.remove(pokes.size() - 1);
                    playCard(poke);
                }
            }
        }, 5000);

    }

}

