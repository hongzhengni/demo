package com.nee.game.service;

import com.nee.game.common.constant.CommonConstant;
import com.nee.game.common.constant.ErrorCodeEnum;
import com.nee.game.common.exception.BusinessException;
import com.nee.game.data.entities.Params;
import com.nee.game.data.entities.Table;
import com.nee.game.data.entities.User;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class DataService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private CardService cardService;
    @Autowired
    private AsyncSQLClient mysqlClient;

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

    /*@PostConstruct
    private void test() {
        cardService.initCard(123456);

        System.out.println(cardService.remainCardNum(123456));


        for (int i = 0; i < 4; i++) {

            User user = new User(cardService);
            user.tableId = 123456;

            cardService.dealCards(user, 13);
        }

        System.out.println(cardService.remainCardNum(123456));

    }*/

    private void initTable() {
        /*if (tables.size() == 0) {
            Table table = new Table(true, cardService, mysqlClient);
            tables.put(table.getTableId(), table);
            table = new Table(true, cardService, mysqlClient);
            tables.put(table.getTableId(), table);
            table = new Table(true, cardService, mysqlClient);
            tables.put(table.getTableId(), table);
            table = new Table(true, cardService, mysqlClient);
            tables.put(table.getTableId(), table);
            table = new Table(true, cardService, mysqlClient);
            tables.put(table.getTableId(), table);
            table = new Table(true, cardService, mysqlClient);
            tables.put(table.getTableId(), table);
        }*/
    }


    //登陆处理
    void loginHall(NetSocket netSocket, Params params) {

        initTable();

        int userId = params.getUserId();
        CompletableFuture future = new CompletableFuture();
        if (users.get(userId) == null) {
            mysqlClient.getConnection(connectionAsyncResult -> {
                if (connectionAsyncResult.failed()) {
                    future.completeExceptionally(connectionAsyncResult.cause());
                }
                System.out.println("mahjong login connect success!");
                connectionAsyncResult.result().query("select * from nee_users where id = " + userId, resultSetAsyncResult -> {
                    if (resultSetAsyncResult.failed()) {
                        future.completeExceptionally(resultSetAsyncResult.cause());
                    }

                    System.out.println("mahjong login query success!");
                    List<JsonObject> result = (List<JsonObject>) resultSetAsyncResult.result().getRows();

                    User u = new User(cardService);
                    if (result.size() > 0) {
                        JsonObject object = result.get(0);
                        u.setUserId(object.getInteger("id"));
                        u.setNick(object.getString("nickname"));
                        u.setAvatarUrl(object.getString("headimgurl"));
                    } else {
                        u.setUserId(params.getUserId());
                        u.setNick("测试用户" + userId);
                        u.setMoney(0);
                    }

                    System.out.println("mahjong login user: " + Json.encode(u));
                    users.put(u.getUserId(), u);
                    connectionAsyncResult.result().close();
                    future.complete(1);
                });
            });

        } else {
            future.complete(1);
        }

        future.whenComplete((res, ex) -> {
            System.out.println("can exe here");
            if (ex != null) {
                System.out.println(ex.toString());
                throw new BusinessException(ErrorCodeEnum.ERROR_LOGIC);
            }
            User u = users.get(userId);
            u.setNetSocket(netSocket);

            socketUserMap.put(netSocket, u);

            u.loginHall();
        });
    }

    void createRoom(NetSocket netSocket, Params params) {
        User currentUser = socketUserMap.get(netSocket);
        if (currentUser == null) {
            throw new BusinessException("请重新登录");
        }

        int radio = params.getRatio();
        int maxGround = params.getMaxGround();

        currentUser.createRoom(radio, maxGround, mysqlClient);

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
