package com.nee.game.service;

import com.nee.game.common.A0Json;
import com.nee.game.common.PokeData;
import com.nee.game.common.Result;
import com.nee.game.common.constant.CmdConstant;
import com.nee.game.common.constant.CommonConstant;
import com.nee.game.common.exception.BusinessException;
import com.nee.game.data.entities.Params;
import com.nee.game.data.entities.Table;
import com.nee.game.data.entities.User;
import com.nee.game.uitls.RevMsgUtils;
import io.vertx.core.net.NetSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
class DataService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private CardService cardService;

    private static Map<Integer, Table> tables = new HashMap<>();
    private static Map<Integer, User> users = new HashMap<>();
    private static Map<NetSocket, User> socketUserMap = new HashMap<>();


    public DataService() {
        tables.put(1, new Table(1, cardService));
        tables.put(2, new Table(2, cardService));
        tables.put(3, new Table(3, cardService));
        tables.put(4, new Table(4, cardService));
    }


    /*public static Table getTable() {
        Iterator<Integer> keys = tables.keySet().iterator();
        Table table;
        while (keys.hasNext()) {
            int key = keys.next();
            table = (Table) tables.get(key);
            if (!table.isFull()) {
                return table;
            }
        }
        table = new Table(tables.size() + 1);
        tables.put(tables.size() + 1, table);
        return table;
    }*/

    //登陆处理
    void loginHall(NetSocket netSocket, Params params) {

        int userId = params.getUserId();
        User u;
        if (users.get(userId) == null) {
            u = new User();
            u.setUserId(params.getUserId());
            u.setNick("测试用户" + userId);
            u.setMoney(1000);
            users.put(u.getUserId(), u);
        }

        u = users.get(userId);
        u.setNetSocket(netSocket);

        Map<String, Object> data = new HashMap<>();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", u.getUserId());

        data.put("user", userInfo);

        List<Map<String, Object>> tableMaps = new ArrayList<>();
        tables.values().forEach(table -> {
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
        });

        data.put("tables", tableMaps);

        Result result = new Result.Builder()
                .setCmd(CmdConstant.REV_HALL_INFO)
                .setData(data)
                .build();
        u.getNetSocket().write(A0Json.encode(result));

        socketUserMap.put(netSocket, u);
    }

    void sitDown(NetSocket netSocket, Params params) {

        User currentUser = socketUserMap.get(netSocket);
        if (currentUser == null) {
            throw new BusinessException("请重新登录");
        }
        AtomicReference<Table> currentTable = new AtomicReference<>();
        currentTable.set(null);
        Integer tableId = currentUser.getTableId();
        if (tableId == null) {
            tableId = params.getTableId();
            currentUser.offline = false;

            currentTable.set(tables.get(tableId));
            if (currentTable.get() == null) {
                throw new BusinessException("桌子不存在");
            }
            if (currentTable.get().isFull()) {
                throw new BusinessException("table is full");
            }

            for (int i = 0; i < 4; i++) {
                if (currentTable.get().getUsers().get(i) == null) {
                    currentUser.setSeatId(i);
                    currentUser.setTableId(currentTable.get().getTableId());
                    currentTable.get().getUsers().set(i, currentUser);
                    break;
                }
            }
        } else {
            currentTable.set(tables.get(tableId));
            if (currentTable.get() == null) {
                throw new BusinessException("桌子不存在");
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("tableId", currentTable.get().getTableId());
        data.put("tableNo", "测试房间" + currentTable.get().getTableId());
        data.put("maxGameRound", Table.getMaxGameRound());
        data.put("currentGameRound", currentTable.get().getGameRound());

        List<Map<String, Object>> userList = new ArrayList<>();
        currentTable.get().getUsers().stream().filter(Objects::nonNull)
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

        // return room info
        netSocket.write(A0Json.encode(new Result.Builder()
                .setCmd(CmdConstant.REV_ROOM_INFO)
                .setData(data)
                .build()));

        //广播消息
        Map<String, Object> map = new HashMap<>();
        map.put("userId", currentUser.getUserId());
        map.put("seatId", currentUser.getSeatId());
        Result result = new Result.Builder()
                .setCmd(CmdConstant.BROADCAST_JOIN_TABLE)
                .setData(map)
                .build();

        users.values().forEach(user -> {
            if (user.getNetSocket() != null) {
                user.getNetSocket().write(A0Json.encode(result));
            }
        });

    }

    //玩家准备
    void userReady(NetSocket netSocket) {

        User currentUser = socketUserMap.get(netSocket);
        Table currentTable = tables.get(currentUser.getTableId());
        currentUser.setStatus(CommonConstant.USER_STATUS.READY);


        currentTable.tache = CommonConstant.TABLE_TACHE.READY;


        Map<String, Object> data = new HashMap<>();
        data.put("userId", currentUser.getUserId());

        Result result = new Result.Builder()
                .setCmd(CmdConstant.BROADCAST_USER_READY)
                .setData(data)
                .build();

        final List<User> readyUsers = new ArrayList<>();
        currentTable.getUsers().stream()
                .filter(Objects::nonNull /*&& user != currentUser*/)
                .filter(user -> user.getNetSocket() != null)
                .forEach(user -> {
                    if (user.getStatus() == CommonConstant.USER_STATUS.READY) {
                        readyUsers.add(user);
                    }
                    user.getNetSocket().write(A0Json.encode(result));
                });

        if (readyUsers.size() == Table.getMaxGameRound()) {
            List<Byte> pokes = Arrays.asList(PokeData.GameLogic);

            Collections.shuffle(pokes);
            pokes.forEach(poke -> redisService.lpush("table-" + currentTable.getTableId(), poke));
        }
    }


    void playCard(NetSocket netSocket, Params params) {
        User currentUser = socketUserMap.get(netSocket);
        Table currentTable = tables.get(currentUser.getTableId());
        Byte poke = params.getPoke();
        currentTable.getUsers().stream().filter(Objects::nonNull)
                .forEach(user ->{

                });


        Map<String, Object> data = new HashMap<>();
        data.put("userId", currentUser.getUserId());
        data.put("seatId", currentUser.getSeatId());
        data.put("poke", poke);


        RevMsgUtils.revMsg(currentTable.getUsers(), CmdConstant.BROADCAST_PLAY_CARD, data);

         

    }

    void penCard(NetSocket netSocket, Params params) {

    }

    void gangCard(NetSocket netSocket, Params params) {

    }

    void chiCard(NetSocket netSocket, Params params) {

    }

    void huCard(NetSocket netSocket, Params params) {

    }

    /**
     * 离开房间
     *
     * @param netSocket socket
     */
    void standUp(NetSocket netSocket) {

    }

    void closeConnect(NetSocket netSocket) {
        User user = socketUserMap.get(netSocket);
        if (user != null) {
            user.setNetSocket(null);
            user.offline = true;
        }

        socketUserMap.remove(netSocket);
    }
}
