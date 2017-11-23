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
import com.nee.game.data.poke.PackerCompare;
import com.nee.game.data.poke.UserPacket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class DataService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private AsyncSQLClient mysqlClient;


    private static Map<Integer, Table> tables = new HashMap<>();
    private static Map<Integer, User> users = new HashMap<>();
    private static Map<String, User> userMap = new HashMap<>();//帐号保存
    private static Map<NetSocket, User> socketUserMap = new HashMap<>();

    private Random random = new Random();

    static {
        tables.put(1, new Table(1));
        tables.put(2, new Table(2));
        tables.put(3, new Table(3));
        tables.put(4, new Table(4));
        tables.put(5, new Table(5));
    }

    public static Table getTable() {
        Iterator<Integer> keys = tables.keySet().iterator();
        Table table;
        while (keys.hasNext()) {
            int key = keys.next();
            table = (Table) tables.get(key);
            if (table.isFull() == false) {
                return table;
            }
        }
        table = new Table(tables.size() + 1);
        tables.put(tables.size() + 1, table);
        return table;
    }

    //登陆处理
    void loginHall(NetSocket netSocket, Params params) {

        int userId = params.getUserId();
        User u;
        if (users.get(userId) == null) {
            u = new User();
            u.setUserId(params.getUserId());
            u.setNick("测试用户" + userId);
            u.setMoney(1000);
            userMap.put(u.getNick(), u);
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
            List<Map<String, Object>> userMaps = new ArrayList<Map<String, Object>>();
            table.getUsers().stream().filter(user -> user != null)
                    .forEach(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userId",user.getUserId());
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

    //加入游戏
    void joinTable(NetSocket netSocket, Params params) {

        User currentUser = socketUserMap.get(netSocket);
        if (currentUser == null) {
            throw new BusinessException("请重新登录");
        }
        Integer tableId = currentUser.getTableId();
        if (tableId == null) {
            tableId = params.getTableId();
            currentUser.offline = false;

            Table currentTable = tables.get(tableId);
            if (currentTable == null) {
                throw new BusinessException("桌子不存在");
            }

            Map<String, Object> data = new HashMap<>();


            data.put("tableId", currentTable.getTableId());
            data.put("tableNo", "测试房间"  + currentTable.getTableId());
            data.put("maxGameRound", Table.getMaxGameRound());
            data.put("currentGameRound", currentTable.getGameRound());

            List<Map<String, Object>> userList = new ArrayList<>();
            currentTable.getUsers().stream().filter(u -> u != null)
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

            // 返回加入房间
            netSocket.write(A0Json.encode(new Result.Builder()
                    .setCmd(CmdConstant.REV_JOIN_TABLE_4_OB)
                    .setData(data)
                    .build()));


        } else {
            Table currentTable = tables.get(tableId);
            if (currentTable == null) {
                throw new BusinessException("桌子不存在");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("table", currentTable);
            data.put("user", currentUser);

            // 返回加入房间
            netSocket.write(A0Json.encode(new Result.Builder()
                    .setCmd(CmdConstant.REV_JOIN_GAME_SUCCESS)
                    .setData(data)
                    .build()));


            //广播消息
            Map<String, Object> map = new HashMap<>();
            map.put("user", currentUser);
            Result result = new Result.Builder()
                    .setCmd(CmdConstant.REV_JOIN_GAME)
                    .setData(map)
                    .build();

            users.values().forEach(user -> {
                if (user.getNetSocket() != null) {
                    user.getNetSocket().write(A0Json.encode(result));
                }
            });
        }


        /*if (currentTable.getIsFull()) {
            throw new BusinessException("桌子已满");
        }*/

        // 不需要随机分配座位
        /*if (currentUser.getSeatId() == null) {
            for (int i = 0; i < 5; i++) {
                if (currentTable.getUsers().get(i) == null) {
                    currentUser.setSeatId(i);
                }
            }
        }*/


    }

    /**
     * sit down
     *
     * @param netSocket socket
     * @param params    params
     */
    void sitDown(NetSocket netSocket, Params params) {
        int seatId = params.getSeatId();
        int tableId = params.getTableId();
        User currentUser = socketUserMap.get(netSocket);
        Table currentTable = tables.get(tableId);

        currentUser.setSeatId(seatId);
        currentUser.setStatus(CommonConstant.USER_STATUS.NO_READY);
        currentUser.setTableId(tableId);
        if (currentTable.getUsers().get(seatId) != null) {
            throw new BusinessException("该座位已经有人");
        }

        currentTable.getUsers().set(seatId, currentUser);

        Map<String, Object> sitDownMap = new HashMap<>();
        sitDownMap.put("table", tables.get(currentUser.getTableId()));
        sitDownMap.put("user", currentUser);

        // 返回坐下成功
        Result result = new Result.Builder()
                .setCmd(CmdConstant.REV_SIT_DOWN_SUCCESS)
                .setData(sitDownMap)
                .build();
        netSocket.write(A0Json.encode(result));


        // 广播坐下成功
        Map<String, Object> map = new HashMap<>();
        map.put("user", currentUser);
        Result data = new Result.Builder()
                .setCmd(CmdConstant.REV_SIT_DOWN)
                .setData(map)
                .build();
        currentTable.getUsers().stream()
                .filter(user -> user != null)
                //.filter(user -> user != currentUser)
                .filter(user -> user.getNetSocket() != null)
                .forEach(user -> {
                    user.getNetSocket().write(A0Json.encode(data));
                });
        currentTable.getObUsers().stream()
                .filter(user -> user != null && user.getNetSocket() != null)
                .forEach(user -> {
                    user.getNetSocket().write(A0Json.encode(data));
                });
    }

    /**
     * 换座位
     *
     * @param netSocket socket
     * @param params    param
     */
    void changeSeat(NetSocket netSocket, Params params) {
        System.out.println("玩家换座位-> socketUserMap: " + A0Json.encode(socketUserMap));
        System.out.println("想找到空指针：" + netSocket);
        int seatId = params.getSeatId();
        User currentUser = socketUserMap.get(netSocket);

        Table currentTable = tables.get(currentUser.getTableId());
        currentTable.getUsers().set(currentUser.getSeatId(), null);
        currentTable.getUsers().set(seatId, currentUser);

        currentUser.setSeatId(seatId);

        Map<String, Object> changeSeatMap = new HashMap<>();
        changeSeatMap.put("table", currentTable);
        changeSeatMap.put("user", currentUser);

        // 返回换座位成功
        Result result = new Result.Builder()
                .setCmd(CmdConstant.REV_CHANGE_SEAT_SUCCESS)
                .setData(changeSeatMap)
                .build();
        netSocket.write(A0Json.encode(result));


        // 广播换座位
        Map<String, Object> map = new HashMap<>();
        map.put("user", currentUser);
        Result data = new Result.Builder()
                .setCmd(CmdConstant.CHANGE_SEAT)
                .setData(map)
                .build();
        currentTable.getUsers().stream()
                .filter(user -> user != null && user.getNetSocket() != null)
                .forEach(user -> {
                    user.getNetSocket().write(A0Json.encode(data));
                });
        currentTable.getObUsers().stream()
                .filter(user -> user != null && user.getNetSocket() != null)
                .forEach(user -> {
                    user.getNetSocket().write(A0Json.encode(data));
                });
    }

    //玩家准备
    void userReady(NetSocket netSocket, Params params) {


        User currentUser = socketUserMap.get(netSocket);
        Table currentTable = tables.get(currentUser.getTableId());
        currentUser.setStatus(CommonConstant.USER_STATUS.READY);


        currentTable.tache = CommonConstant.TABLE_TACHE.READY;


        Map<String, Object> data = new HashMap<>();
        data.put("user", currentUser);

        netSocket.write(A0Json.encode(new Result.Builder()
                .setCmd(CmdConstant.REV_USER_READY_SUCCESS)
                .build()));


        Result result = new Result.Builder()
                .setCmd(CmdConstant.REV_USER_READY)
                .setData(data)
                .build();

        currentTable.getObUsers().stream()
                .filter(u -> u != null && u.getNetSocket() != null)
                .forEach(u -> {
                    u.getNetSocket().write(A0Json.encode(result));
                });

        tables.values().forEach(table -> {
            table.getUsers().stream()
                    .filter(user -> user != null /*&& user != currentUser*/)
                    .filter(user -> user.getNetSocket() != null)
                    .forEach(user -> {
                        user.getNetSocket().write(A0Json.encode(result));
                    });

            if (table == currentTable) {
                final List<User> readyUsers = new ArrayList<User>();
                System.out.println("userReady --> " + Json.encode(table));
                table.getUsers().stream()
                        .filter(user -> user != null)
                        .forEach(user -> {
                            // 掉线的用户
                            if (user.offline &&
                                    user.getStatus() == CommonConstant.USER_STATUS.NO_READY) {
                                user.setStatus(CommonConstant.USER_STATUS.READY);

                                // 广播用户准备
                                data.put("user", user);
                                Result closedUserResult = new Result.Builder()
                                        .setCmd(CmdConstant.REV_USER_READY)
                                        .setData(data)
                                        .build();
                                users.values().stream()
                                        //.filter(closedUser -> closedUser != user)
                                        .filter(closedUser -> closedUser.getNetSocket() != null)
                                        .forEach(closedUser -> {
                                            closedUser.getNetSocket().write(A0Json.encode(closedUserResult));
                                        });
                            }
                            if (user.getStatus() == CommonConstant.USER_STATUS.READY) {
                                readyUsers.add(user);
                            }
                        });
                if (readyUsers.size() == currentTable.getRealCount()/* && readyUsers.size() > 1*/) {
                    List<Byte> pokes = Arrays.asList(PokeData.GameLogic);

                    Collections.shuffle(pokes);
                    pokes.forEach(poke -> {
                        redisService.lpush("table-" + currentTable.getTableId(), poke);
                    });

                    currentTable.addGameRound();

                    System.out.println("start deal card --> " + Json.encode(table));
                    currentTable.tache = CommonConstant.TABLE_TACHE.DEAL_4;
                    table.getUsers().stream()
                            .filter(user -> user != null
                                    && (user.getPokes() == null || user.getPokes().size() < 4))
                            .forEach(user -> {
                                List<Byte> userPokes = new ArrayList<>();
                                for (int i = 0; i < 4; i++) {
                                    userPokes.add(Byte.valueOf(redisService.lpop("table-" + currentTable.getTableId())));
                                }
                                user.setPokes(userPokes);
                                user.setStatus(CommonConstant.USER_STATUS.PLAY);
                                user.setGameCount(table.getGameRound());

                                Result pokeResult = new Result.Builder()
                                        .setCmd(CmdConstant.REV_DEAL)
                                        .setData(user)
                                        .build();
                                System.out.println("user socket --> " + user.getNetSocket());
                                System.out.println("user socket --> " + (user.getNetSocket() != null));

                                if (user.getNetSocket() != null) {
                                    user.getNetSocket().write(A0Json.encode(pokeResult));
                                }
                                user.gameCount++;
                            });

                    Result pokeResult = new Result.Builder()
                            .setCmd(CmdConstant.REV_DEAL_4_OB)
                            .setData(4)
                            .build();
                    table.getObUsers().stream().filter(user -> user != null && user.getNetSocket() != null)
                            .forEach(user -> {
                                user.getNetSocket().write(A0Json.encode(pokeResult));
                            });

                }
            }

        });
    }

    void loginOut(NetSocket netSocket) {
        User currentUser = socketUserMap.get(netSocket);

        netSocket.write(A0Json.encode(new Result.Builder()
                .setCmd(CmdConstant.REV_LOGOUT_HALL_SUCCESS)
                .build()));

        Map<String, Object> map = new HashMap<>();
        map.put("user", currentUser);

        Result result = new Result.Builder()
                .setCmd(CmdConstant.REV_LOGOUT_HALL)
                .setData(map)
                .build();

        users.values().stream()
                //.filter(user -> user != currentUser)
                .filter(user -> user.getNetSocket() != null)
                .forEach(user -> {
                    user.getNetSocket().write(A0Json.encode(result));
                });

        netSocket.close();

    }

    /**
     * 离开房间
     *
     * @param netSocket socket
     */
    public void leaveRoom(NetSocket netSocket) {

        User currentUser = socketUserMap.get(netSocket);
        Table currentTable = tables.get(currentUser.getTableId());
        currentTable.getUsers().set(currentUser.getSeatId(), null);
        currentUser.setTableId(null);

        netSocket.write(A0Json.encode(new Result.Builder()
                .setCmd(CmdConstant.REV_LEAVE_ROOM_SUCCESS)
                .build()));

        Map<String, Object> map = new HashMap<>();
        map.put("user", currentUser);

        Result result = new Result.Builder()
                .setCmd(CmdConstant.REV_LEAVE_ROOM)
                .setData(map)
                .build();

        users.values().stream()
                //.filter(user -> user != currentUser)
                .filter(user -> user.getNetSocket() != null)
                .forEach(user -> {
                    user.getNetSocket().write(A0Json.encode(result));
                });

    }

    public void startGame(NetSocket netSocket, Params params) {
    }

    void hog(NetSocket netSocket, Params params) {

        User currentUser = socketUserMap.get(netSocket);
        System.out.println("用户抢庄开始->" + currentUser.getUserId() + ", nick:" + currentUser.getNick() + "params:" + params.getHog());

        currentUser.setHog(params.getHog());
        Table currentTable = tables.get(currentUser.getTableId());

        currentTable.tache = CommonConstant.TABLE_TACHE.HOG;

        Map<String, Object> map = new HashMap<>();
        map.put("user", currentUser);
        map.put("hog", currentUser.getHog());
        Result revResult = new Result.Builder()
                .setCmd(CmdConstant.REV_HOG)
                .setData(map)
                .build();

        int count = 0;
        List<User> hogUsers = new ArrayList<>();
        for (User user : currentTable.getUsers()) {
            if (user == null) {
                continue;
            }
            // 掉线的用户
            if (user.offline && user.getHog() == 0) {
                System.out.println("抢庄时掉线的用户->" + user.getUserId() + ", " + user.getNick());
                user.setHog(2);
                Map<String, Object> offlineMap = new HashMap<>();
                offlineMap.put("user", user);
                offlineMap.put("hog", 2);
                Result result = new Result.Builder()
                        .setCmd(CmdConstant.REV_HOG)
                        .setData(offlineMap)
                        .build();
                currentTable.getUsers().stream()
                        .filter(u -> u != null && u != user && u.getNetSocket() != null)
                        .forEach(u -> {
                            u.getNetSocket().write(A0Json.encode(result));
                        });
                currentTable.getObUsers().stream()
                        .filter(u -> u != null && u != user && u.getNetSocket() != null)
                        .forEach(u -> {
                            u.getNetSocket().write(A0Json.encode(result));
                        });

            }
            if (user.getHog() > 0) {
                count++;
            }
            if (user.getHog() == 1) {
                hogUsers.add(user);
            }
            if (count == currentTable.getRealCount() && hogUsers.size() == 0) {
                currentTable.getUsers().stream()
                        .filter(u -> u != null)
                        .forEach(u -> {
                            u.setHog(1);
                            hogUsers.add(u);
                        });
            }
            //if (user = params.getUserId()) {

            if (user.getNetSocket() != null) {
                user.getNetSocket().write(A0Json.encode(revResult));
            }
            //}
        }
        currentTable.getObUsers().stream().filter(user -> user != null && user.getNetSocket() != null)
                .forEach(user -> {
                    user.getNetSocket().write(A0Json.encode(revResult));
                });
        if (count == currentTable.getRealCount()) {
            System.out.println("当前抢庄用户列表：" + Json.encode(hogUsers));
            User hogUser = hogUsers.get(random.nextInt(hogUsers.size()));

            System.out.println("抢庄成功的广播，当前庄家－>" + hogUser.getUserId() + ", " + hogUser.getNick());
            Result result = new Result.Builder()
                    .setCmd(CmdConstant.REV_HOG_SUCCESS)
                    .setData(hogUser)
                    .build();
            currentTable.getUsers().stream()
                    .filter(user -> user != null)
                    .forEach(user -> {
                        System.out.println("去除多余的庄家->" + (user != hogUser));
                        if (user != hogUser) {
                            user.setHog(2);
                        }
                        if (user.getNetSocket() != null) {
                            user.getNetSocket().write(A0Json.encode(result));
                        }
                    });
            currentTable.getObUsers().stream()
                    .filter(user -> user != null && user.getNetSocket() != null)
                    .forEach(user -> {
                        user.getNetSocket().write(A0Json.encode(result));
                    });
            System.out.println("抢庄成功的广播结束，当前庄家－>" + hogUser.getUserId() + ", " + hogUser.getNick() + ", hog = " + hogUser.getHog());

        }


    }


    void addMoney(NetSocket netSocket, Params params) {

        User currentUser = socketUserMap.get(netSocket);
        System.out.println("用户加倍开始->" + currentUser.getUserId() + ", nick:" + currentUser.getNick() + "params:" + params.getRate());

        Table currentTable = tables.get(currentUser.getTableId());

        currentTable.tache = CommonConstant.TABLE_TACHE.ADD_MONEY;


        currentUser.setRate(params.getRate());
        Map<String, Object> map = new HashMap<>();
        map.put("user", currentUser);
        map.put("rate", params.getRate());
        Result revResult = new Result.Builder()
                .setCmd(CmdConstant.REV_ADD_MONEY)
                .setData(map)
                .build();

        int count = 0;
        for (User user : currentTable.getUsers()) {
            if (user == null) {
                continue;
            }
            // 掉线的用户
            if (user.offline && user.getRate() == 0) {
                user.setRate(1);
                Map<String, Object> offlineMap = new HashMap<>();
                offlineMap.put("user", user);
                offlineMap.put("rate", 1);
                Result result = new Result.Builder()
                        .setCmd(CmdConstant.REV_ADD_MONEY)
                        .setData(offlineMap)
                        .build();

                currentTable.getUsers().stream()
                        .filter(u -> u != null && u != user && u.getNetSocket() != null)
                        .forEach(u -> {
                            u.getNetSocket().write(A0Json.encode(result));
                        });
                currentTable.getObUsers().stream()
                        .filter(u -> u != null && u != user && u.getNetSocket() != null)
                        .forEach(u -> {
                            u.getNetSocket().write(A0Json.encode(result));
                        });
            }
            if (user.getRate() > 0) {
                count++;
            }


            if (user.getNetSocket() != null) {
                user.getNetSocket().write(A0Json.encode(revResult));
            }
        }
        currentTable.getObUsers().stream().filter(u -> u != null && u.getNetSocket() != null)
                .forEach(u -> {
                    u.getNetSocket().write(A0Json.encode(revResult));
                });

        if (count == currentTable.getRealCount()) {

            currentTable.getUsers().stream()
                    .filter(user -> user != null && user.getPokes().size() == 4)
                    .forEach(user -> {

                        Byte poke = Byte.valueOf(redisService.lpop("table-" + currentTable.getTableId()));
                        user.getPokes().add(poke);
                        System.out.println("用户加倍成功开始发最后一张牌->" + user.getUserId() + ", " + user.getNick() + ", hog = " + user.getHog());
                        currentTable.tache = CommonConstant.TABLE_TACHE.DEAL_1;
                        UserPacket up = new UserPacket(user.packers());
                        up.isNiuNum();
                        user.setThreePokes(up.threePokes);

                        Result result = new Result.Builder()
                                .setCmd(CmdConstant.REV_DEAL)
                                .setData(user)
                                .build();
                        if (user.getNetSocket() != null) {
                            user.getNetSocket().write(A0Json.encode(result));
                        }
                    });
            Result result = new Result.Builder()
                    .setCmd(CmdConstant.REV_DEAL_4_OB)
                    .setData(1)
                    .build();
            currentTable.getObUsers().stream().filter(u -> u != null && u.getNetSocket() != null)
                    .forEach(u -> {
                        u.getNetSocket().write(A0Json.encode(result));
                    });

        }
    }

    public void compareCards(NetSocket netSocket, Params params) throws InterruptedException {
        User currentUser = socketUserMap.get(netSocket);
        System.out.println("用户比牌开始->" + currentUser.getUserId() + ", nick:" + currentUser.getNick() + ", hog:" + currentUser.getHog());

        Table currentTable = tables.get(currentUser.getTableId());

        currentTable.tache = CommonConstant.TABLE_TACHE.COMPARE_START;


        currentUser.setCompare(true);

        List<User> compares = new ArrayList<>();
        Result currentUserResult = new Result.Builder()
                .setCmd(CmdConstant.REV_START_COMPARE)
                .setData(currentUser)
                .build();
        currentTable.getUsers().stream()
                .filter(user -> user != null)
                .forEach(user -> {
                    // 掉线用户处理
                    if (user.offline && !user.isCompare()) {
                        user.setCompare(true);
                        Result result = new Result.Builder()
                                .setCmd(CmdConstant.REV_START_COMPARE)
                                .setData(user)
                                .build();
                        user.getNetSocket().write(A0Json.encode(result));
                        currentTable.getUsers().stream()
                                .filter(u -> u != user && u != null && u.getNetSocket() != null)
                                .forEach(u -> {
                                    u.getNetSocket().write(A0Json.encode(result));
                                });
                        currentTable.getObUsers().stream()
                                .filter(u -> u != null && u.getNetSocket() != null)
                                .forEach(u -> {
                                    u.getNetSocket().write(A0Json.encode(result));
                                });
                    }
                    if (user.isCompare()) {
                        compares.add(user);
                    }
                    if (user.getNetSocket() != null) {

                        user.getNetSocket().write(A0Json.encode(currentUserResult));
                    }
                });
        currentTable.getObUsers().stream().filter(u -> u != null && u.getNetSocket() != null)
                .forEach(u -> {
                    u.getNetSocket().write(A0Json.encode(currentUserResult));
                });


        if (compares.size() == currentTable.getRealCount()) {
            System.out.println("用户比牌正式开始");
            Integer hogUserId = null;
            for (User user : compares) {
                System.out.println("用户比牌开始时查找庄家" + user.getUserId() + ", nick:" + user.getNick() + ", hog:" + user.getHog());
                if (user != null && user.getHog() == 1) {
                    hogUserId = user.getUserId();
                }
            }
            if (hogUserId == null) {
                throw new BusinessException("没有找到庄家");
            }
            final User hogUser = users.get(hogUserId);
            hogUser.setCompare(false);

            System.out.println("当前的庄家是：" + hogUser.getUserId() + ", nick:" + hogUser.getNick());

            UserPacket banker = new UserPacket(hogUser.packers(), true);
            hogUser.calNiuInfo(banker);
            List<Map<String, Object>> datas = new ArrayList<>();
            int bankerMoney = hogUser.getMoney();

            compares.stream().filter(compare -> compare.getHog() != 1).forEach(compare -> {

                System.out.println("和其他玩家比牌：" + compare.getUserId() + ", nick:" + compare.getNick());
                UserPacket cp = new UserPacket(compare.packers());

                compare.calNiuInfo(cp);
                compare.setCompare(false);

                UserPacket win = PackerCompare.geWin(banker, cp);
                if (win == banker) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("user", compare);
                    result.put("type", cp.type);
                    int count = -cp.getRatio() * compare.getRate() * 30;
                    result.put("result", count);
                    hogUser.setMoney(hogUser.getMoney() - count);
                    compare.setMoney(compare.getMoney() + count);

                    datas.add(result);

                    hogUser.winNum++;
                    hogUser.bankerWinCount++;
                    hogUser.winCount++;
                } else {
                    Map<String, Object> result = new HashMap<>();
                    result.put("user", compare);
                    result.put("type", cp.type);
                    int count = cp.getRatio() * compare.getRate() * 30;
                    result.put("result", count);
                    hogUser.setMoney(hogUser.getMoney() - count);
                    compare.setMoney(compare.getMoney() + count);

                    datas.add(result);
                    compare.winNum++;
                    compare.winCount++;
                }
            });
            if (hogUser.bankerWinCount == compares.size()) {
                hogUser.allKill++;
            } else if (hogUser.bankerWinCount == 0) {
                hogUser.allCompensate++;
            }
            Map<String, Object> hogData = new HashMap<>();
            hogData.put("user", hogUser);
            hogData.put("type", banker.type);
            hogData.put("result", hogUser.getMoney() - bankerMoney);

            datas.add(hogData);

            Result result = new Result.Builder()
                    .setCmd(CmdConstant.REV_END_COMPARE)
                    .setData(datas)
                    .build();

            System.out.println("用户比牌结束 单局结算数据->:" + A0Json.encode(datas));
            System.out.println("用户比牌结束 单局结算数据-广播开始");

            currentTable.getUsers().stream()
                    .filter(user -> user != null)
                    .forEach(user -> {
                        if (user.getNetSocket() != null) {
                            user.getNetSocket().write(A0Json.encode(result));
                        }
                    });
            currentTable.getObUsers().stream()
                    .filter(user -> user != null && user.getNetSocket() != null)
                    .forEach(user -> {
                        user.getNetSocket().write(A0Json.encode(result));
                    });

            currentTable.tache = CommonConstant.TABLE_TACHE.COMPARE_END;
            System.out.println("用户比牌结束 单局结算数据-广播结束， 开始清除用户信息");

            currentTable.getUsers().stream()
                    .filter(user -> user != null)
                    .forEach(user -> {
                        user.setPokes(null);
                        user.setHog(0);
                        user.setRate(0);
                        user.setCompare(false);
                        user.setStatus(CommonConstant.USER_STATUS.NO_READY);
                    });


            if (currentTable.isEnd()) {

                currentTable.tache = CommonConstant.TABLE_TACHE.SETTLE;

                System.out.println("总结算数据-广播开始");
                TimeUnit.SECONDS.sleep(5);

                List<User> users = new ArrayList<>();
                currentTable.getUsers().stream()
                        .filter(u -> u != null)
                        .forEach(users::add);
                Collections.sort(users);
                List<Map<String, Object>> settleMaps = new ArrayList<>();
                currentTable.getUsers().stream()
                        .filter(u -> u != null)
                        .forEach(u -> {
                            Map<String, Object> settleMap = new HashMap<>();
                            settleMap.put("userId", u.getUserId());
                            settleMap.put("allKill", u.allKill);
                            settleMap.put("allCompensate", u.allCompensate);
                            settleMap.put("niuNiuNum", u.niuNiuNum);
                            settleMap.put("noNiuNum", u.noNiuNum);
                            settleMap.put("winNum", u.winNum);
                            settleMap.put("isWin", users.get(0) == u);
                            settleMap.put("winMoney", u.getMoney() - 1000);

                            if (u.getNetSocket() != null && u.offline) {
                                u.offline = false;
                            }
                            settleMaps.add(settleMap);
                        });

                Map<String, Object> map = new HashMap<>();
                map.put("datas", settleMaps);
                Result settleResult = new Result.Builder()
                        .setCmd(CmdConstant.REV_SETTLE_ACCOUNTS)
                        .setData(map)
                        .build();
                currentTable.getUsers().stream()
                        .filter(u -> u != null)
                        .forEach(u -> {

                            if (u.getNetSocket() != null)
                                u.getNetSocket().write(A0Json.encode(settleResult));

                            u.setOriginalMoney(u.getOriginalMoney() + u.getMoney());
                            u.clear();

                        });
                currentTable.getObUsers().stream()
                        .filter(u -> u != null && u.getNetSocket() != null)
                        .forEach(u -> {
                            u.getNetSocket().write(A0Json.encode(settleResult));
                        });
                currentTable.clearRound();
                currentTable.initUsers();
                currentTable.chargeType = 0;

            }

        }

    }

    public void closeConnect(NetSocket netSocket) {
        User user = socketUserMap.get(netSocket);
        if (user != null) {
            user.setNetSocket(null);
            user.offline = true;
        }

        socketUserMap.remove(netSocket);
    }

    /**
     * @param netSocket channel
     * @param params    param
     */
    public void createRoom(NetSocket netSocket, Params params) {
        User currentUser = socketUserMap.get(netSocket);
        int tableId = tables.keySet().size();
        Table currentTable = new Table(tableId, currentUser.getUserId(), params.getChargeType());
        tables.put(tableId, currentTable);
        currentUser.setTableId(tableId);
        currentUser.setSeatId(0);

        currentTable.addUser(currentUser);

        netSocket.write(A0Json.encode(new Result.Builder()
                .setCmd(CmdConstant.REV_CREATE_ROOM_SUCCESS)
                .build()));

        Map<String, Object> map = new HashMap<>();
        map.put("table", currentTable);

        Result result = new Result.Builder()
                .setCmd(CmdConstant.REV_CREATE_ROOM)
                .setData(map)
                .build();
        users.values().stream().filter(user -> user != currentUser)
                .filter(user -> user.getNetSocket() != null).forEach(user -> {
            user.getNetSocket().write(A0Json.encode(result));
        });
    }


    class LoadRobotTask extends TimerTask {
        private AsyncSQLClient mysqlClient;

        public LoadRobotTask(AsyncSQLClient mysqlClient) {
            this.mysqlClient = mysqlClient;
        }

        @Override
        public void run() {
            List<User> robots = new ArrayList<>();
            System.out.println(A0Json.encode(DataService.users));
            mysqlClient.getConnection(res -> {
                if (res.failed()) {
                    System.out.println(res.toString() + "");
                } else {
                    res.result().query("select id, nickname, headimgurl, sex, diamonds, admin, game_count, win_count from nee_users where role = 3", r -> {
                        if (r.succeeded()) {
                            List<JsonObject> objects = r.result().getRows();
                            objects.forEach(object -> {
                                User user = new User(object.getInteger("id"), object.getString("nickname"), object.getInteger("diamonds"), true);
                                robots.add(user);
                                if (DataService.users.get(object.getInteger("id")) == null) {
                                    DataService.users.put(object.getInteger("id"), user);
                                    DataService.userMap.put(object.getString("nickname"), user);
                                }

                            });

                        }
                    });
                }
            });

        }
    }

    class RobotTask extends TimerTask {

        private Random random = new Random();

        @Override
        public void run() {
            DataService.tables.values().stream()
                    .filter(table -> table.getRealCount() == 0)
                    .forEach(table -> {
                        DataService.users.values().stream()
                                .filter(user -> user.robot && user.getStatus() == 0)
                                .forEach(user -> {
                                    user.setSeatId(random.nextInt(5));
                                    user.setStatus(1);
                                    user.setMoney(1000);
                                    user.setOriginalMoney(user.getOriginalMoney() - 1000);
                                    user.setTableId(table.getTableId());
                                    table.addUser(user);
                                    return;
                                });
                    });
        }
    }
}
