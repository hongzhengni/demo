package com.nee.game.data.entities;


import java.util.List;

/**
 * @Author: heikki.
 * @Description:
 * @DATE: 下午9:23 17/10/30.
 */
public class Params {

    private Integer tableId;

    private Integer userId;

    private Integer seatId;

    private Byte poke;

    private List<Byte> pokes;

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
}
