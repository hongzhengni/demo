package com.nee.game.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;


public class Table {

    private static final int maxCount = 4;
    private static final int maxGameRound = 3;
    private int tableId;
    private List<User> users;

    @JsonIgnore
    public int tache = 0;

    private int gameRound = 0;

    public List<User> getUsers() {
        if (users == null) {
            initUsers();
        }
        return users;
    }

    public void initUsers() {
        users = new ArrayList<>();
        for (int i = 0; i < maxCount; i++) {
            users.add(i, null);
        }
    }

    public int getTableId() {
        return tableId;
    }

    public Boolean isEnd() {
        return gameRound >= maxGameRound;
    };

    public boolean isFull() {
        return getRealCount() == maxCount;
    }

    public Table(int tableId) {
        this.tableId = tableId;
        users = new ArrayList<>();
        for (int i = 0; i < maxCount; i++) {
            users.add(i, null);
        }
    }

    public Boolean addUser(User user) {
        user.setJoinTableTime(0);
        for (int i = 0; i < maxCount; i++) {
            if (users.get(i) == null) {
                user.setSeatId(i);
                users.set(i, user);
                return true;
            }
        }
        return false;
    }

    public int getRealCount() {
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

    public static int getMaxGameRound() {
        return maxGameRound;
    }
}
