package com.nee.game.data.entities;


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
}
