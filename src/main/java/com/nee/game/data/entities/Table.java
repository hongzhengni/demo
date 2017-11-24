package com.nee.game.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nee.game.common.constant.CmdConstant;
import com.nee.game.common.constant.CommonConstant;
import com.nee.game.service.CardService;
import com.nee.game.uitls.RevMsgUtils;

import java.util.*;


public class Table {

    private static final int maxCount = 4;
    private static final int maxGameRound = 3;
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

    ;

    private int readyCount() {
        int i = 0;
        for (User user : users) {
            if (user != null && user.getStatus() == CommonConstant.USER_STATUS.READY)
                i++;
        }
        return i;
    }

    public boolean isFull() {
        return getRealCount() == maxCount;
    }

    public Table(int tableId, CardService cardService) {
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

    public static int getMaxGameRound() {
        return maxGameRound;
    }

    private class AutoExecuteTask extends TimerTask {

        @Override
        public void run() {
            // TODO all people offline

            if (!isFull() || isEnd()) {
                return;
            }
            if (tache == CommonConstant.TABLE_TACHE.READY) {

                // offline user auto ready
                users.stream().filter(user -> user.getNetSocket() == null).forEach(user -> {
                    user.setStatus(CommonConstant.USER_STATUS.READY);
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", user.getUserId());
                    RevMsgUtils.revMsg(users, CmdConstant.BROADCAST_USER_READY, map);
                });
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
                                RevMsgUtils.revMsg(user.getNetSocket(), CmdConstant.REV_START_GAME, userMap);
                            });
                }

            }

        }
    }


}
