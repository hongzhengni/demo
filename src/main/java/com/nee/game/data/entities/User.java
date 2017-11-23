package com.nee.game.data.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nee.game.common.A0Json;
import com.nee.game.data.poke.Color;
import com.nee.game.data.poke.Num;
import com.nee.game.data.poke.Packer;
import io.vertx.core.json.Json;
import io.vertx.core.net.NetSocket;

import java.util.List;

public class User implements Comparable<User> {
    private Integer userId;
    private String nick;
    private int money = 1000;

    private String avatarUrl;

    private int originalMoney;
    private int status;//0未准备状态 1准备状态 2在玩状态 3旁观者

    private int seatId;
    private int tableId;

    @JsonIgnore
    public boolean robot = false;
    //@JsonIgnore
    private List<Byte> pokes;

    @JsonIgnore
    private NetSocket netSocket;

    private long joinTableTime;

    /**
     * －－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－
     **/
    @JsonIgnore
    public int gameCount = 0;
    @JsonIgnore
    public int winCount = 0;
    @JsonIgnore
    public boolean offline = false;

    /**
     * －－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－
     **/

    public User() {

    }

    public User(int userId, String nick, int originalMoney, boolean robot) {
        this.userId = userId;
        this.nick = nick;
        this.setOriginalMoney(originalMoney);
        this.robot = robot;
    }

    public void clear() {
        this.setMoney(0);
        this.setStatus(0);
        this.tableId = 0;
        this.seatId = 0;
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

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    void setJoinTableTime(long joinTableTime) {
        this.joinTableTime = joinTableTime;
    }

    public NetSocket getNetSocket() {
        return netSocket;
    }

    public void setNetSocket(NetSocket netSocket) {
        this.netSocket = netSocket;
    }

    public List<Byte> getPokes() {
        return pokes;
    }

    public void setPokes(List<Byte> pokes) {
        this.pokes = pokes;
    }

    public Packer[] packers() {
        Packer[] packers = new Packer[5];
        System.out.println("pokes->:" + A0Json.encode(pokes));
        for (int i = 0; i < pokes.size(); i++) {
            Byte poke = pokes.get(i);
            int num = poke % 16;
            int color = poke / 16;
            packers[i] = new Packer(Num.getEnumByNum(num), Color.getColorByValue(color));
        }

        return packers;
    }

    @Override
    public int compareTo(User o) {

        System.out.println("sort users by money --> this " + Json.encode(this));
        System.out.println("sort users by money -->  " + Json.encode(o));
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

    public void setOriginalMoney(int originalMoney) {
        this.originalMoney = originalMoney;
    }
}
