package com.nee.game.data.entities;


import java.util.List;

public class Params {

    private Integer tableId;

    private Integer userId;

    private Integer seatId;

    private Byte poke;

    private List<Byte> pokes;

    private int ratio;

    private int maxGround;

    private int location;

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public Byte getPoke() {
        return poke;
    }

    public void setPoke(Byte poke) {
        this.poke = poke;
    }

    public List<Byte> getPokes() {
        return pokes;
    }

    public void setPokes(List<Byte> pokes) {
        this.pokes = pokes;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public int getRatio() {
        return ratio;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public void setMaxGround(int maxGround) {
        this.maxGround = maxGround;
    }

    public int getMaxGround() {
        return maxGround;
    }
}
