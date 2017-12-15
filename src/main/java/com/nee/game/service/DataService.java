package com.nee.game.service;

import com.nee.game.common.constant.CmdConstant;
import com.nee.game.common.constant.CommonConstant;
import com.nee.game.common.constant.ErrorCodeEnum;
import com.nee.game.common.exception.BusinessException;
import com.nee.game.data.entities.Params;
import com.nee.game.data.entities.Table;
import com.nee.game.data.entities.User;
import com.nee.game.uitls.RevMsgUtils;
import io.vertx.core.json.Json;
import io.vertx.core.net.NetSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private CardService cardService;

    public static Map<Integer, Table> tables = new HashMap<>();
    public static Map<Integer, User> users = new HashMap<>();
    private static Map<NetSocket, User> socketUserMap = new HashMap<>();

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

    private void initTable() {
        if (tables.size() == 0) {
            tables.put(1, new Table(1, cardService));
            tables.put(2, new Table(2, cardService));
            tables.put(3, new Table(3, cardService));
            tables.put(4, new Table(4, cardService));
        }
    }


    //登陆处理
    void loginHall(NetSocket netSocket, Params params) {

        initTable();

        int userId = params.getUserId();
        User u;
        if (users.get(userId) == null) {
            u = new User(cardService);
            u.setUserId(params.getUserId());
            u.setNick("测试用户" + userId);
            u.setMoney(1000);
            users.put(u.getUserId(), u);
        }

        u = users.get(userId);
        u.setNetSocket(netSocket);

        socketUserMap.put(netSocket, u);

        u.loginHall();

    }

    void createRoom(NetSocket netSocket, Params params) {
        User currentUser = socketUserMap.get(netSocket);
        if (currentUser == null) {
            throw new BusinessException("请重新登录");
        }

        int radio = params.getRatio();
        int maxGround = params.getMaxGround();

        int tableId = tables.size() + 1;
        Table currentTable = new Table(tableId, cardService);
        currentTable.setMaxGameRound(maxGround);
        currentTable.setRadio(radio);

        tables.put(tableId, currentTable);

        Map<String, Integer> data = new HashMap<>();
        data.put("tableId", tableId);
        data.put("userId", currentUser.getUserId());

        RevMsgUtils.revMsg(users.values(), CmdConstant.BROADCAST_CREATE_ROOM, data);

        currentTable.addVirtualUser(3);
    }

    void sitDown(NetSocket netSocket, Params params) {

        User currentUser = socketUserMap.get(netSocket);
        if (currentUser == null) {
            throw new BusinessException("请重新登录");
        }

        currentUser.sitDown(params.getTableId(), params.getSeatId());
    }

    //玩家准备
    void userReady(NetSocket netSocket) {

        User currentUser = socketUserMap.get(netSocket);
        Table currentTable = tables.get(currentUser.getTableId());

        System.out.println("user ready -------> currentUser: " + Json.encode(currentUser));
        currentTable.tache = CommonConstant.TABLE_TACHE.READY;
        currentUser.ready();
    }


    void playCard(NetSocket netSocket, Params params) {
        User currentUser = socketUserMap.get(netSocket);
        Byte poke = params.getPoke();

        currentUser.playCard(poke);
    }

    void penCard(NetSocket netSocket, Params params) {

        User currentUser = socketUserMap.get(netSocket);
        List<Byte> pokes = params.getPokes();
        if (pokes.size() != 3) {
            throw new BusinessException(ErrorCodeEnum.ERROR_PARAM);
        }

        currentUser.penCard(pokes);
    }

    void gangCard(NetSocket netSocket, Params params) {

        User currentUser = socketUserMap.get(netSocket);
        List<Byte> pokes = params.getPokes();
        if (pokes.size() != 4) {
            throw new BusinessException(ErrorCodeEnum.ERROR_PARAM);
        }

        currentUser.gangCard(pokes);
    }

    void chiCard(NetSocket netSocket, Params params) {

        User currentUser = socketUserMap.get(netSocket);
        List<Byte> pokes = params.getPokes();
        int location = params.getLocation();

        currentUser.chiCard(pokes, location);
    }

    void huCard(NetSocket netSocket, Params params) {

        User currentUser = socketUserMap.get(netSocket);
        if (params == null) {
            throw new BusinessException(ErrorCodeEnum.ERROR_PARAM);
        }
        Byte poke = params.getPoke();

        currentUser.huCard(poke);
    }

    void giveUpCard(NetSocket netSocket) {
        User currentUser = socketUserMap.get(netSocket);
        currentUser.giveUpPoke();
    }

    /**
     * 离开房间
     *
     * @param netSocket socket
     */
    void standUp(NetSocket netSocket) {

        User currentUser = socketUserMap.get(netSocket);

        currentUser.standUp();
    }

    void dismiss(NetSocket netSocket) {

        User currentUser = socketUserMap.get(netSocket);

        currentUser.dismiss();
    }

    void applySettle(NetSocket netSocket) {
        User currentUser = socketUserMap.get(netSocket);

        currentUser.applySettle();
    }

    void chat(NetSocket netSocket, Params params) {

        User currentUser = socketUserMap.get(netSocket);
        currentUser.chat(params.getContent());


    }

    void closeConnect(NetSocket netSocket) {
        User user = socketUserMap.get(netSocket);
        if (user != null) {
            user.setNetSocket(null);
           user.disConnect();
        }

        socketUserMap.remove(netSocket);

    }

}
