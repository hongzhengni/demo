package com.nee.game.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nee.game.common.A0Json;
import com.nee.game.common.Request;
import com.nee.game.common.Result;
import com.nee.game.common.constant.CmdConstant;
import com.nee.game.common.constant.ErrorCodeEnum;
import com.nee.game.common.exception.BusinessException;
import com.nee.game.data.entities.Params;
import com.nee.game.uitls.StringUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.web.RoutingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 */
@Component
public class GameService {

    @Autowired
    private DataService dataService;
    @Autowired
    private AsyncSQLClient mysqlClient;

    public void handle(NetSocket netSocket) {

        System.out.println("Mahjong incoming connection!");
        netSocket.handler(buffer -> {
            try {
                String reqStr = buffer.getString(0, buffer.length());
                System.out.println(reqStr);
                Request<Params> request = A0Json.decodeValue(reqStr,
                        new TypeReference<Request<Params>>() {
                        });
                int cmd = request.getCmd();

                switch (cmd) {
                    case CmdConstant.LOGIN_HALL:
                        dataService.loginHall(netSocket, request.getParams());
                        break;
                    case CmdConstant.CREATE_ROOM:
                        dataService.createRoom(netSocket, request.getParams());
                        break;
                    case CmdConstant.SIT_DOWN:
                        dataService.sitDown(netSocket, request.getParams());
                        break;
                    case CmdConstant.STAND_UP:
                        dataService.standUp(netSocket);
                        break;
                    case CmdConstant.READY:
                        dataService.userReady(netSocket);
                        break;
                    case CmdConstant.PLAY_CARD:
                        dataService.playCard(netSocket, request.getParams());
                        break;
                    case CmdConstant.PEN_CARD:
                        dataService.penCard(netSocket, request.getParams());
                        break;
                    case CmdConstant.GANG_CARD:
                        dataService.gangCard(netSocket, request.getParams());
                        break;
                    case CmdConstant.CHI_CARD:
                        dataService.chiCard(netSocket, request.getParams());
                        break;
                    case CmdConstant.HU_CARD:
                        dataService.huCard(netSocket, request.getParams());
                        break;
                    case CmdConstant.GIVE_UP_CARD:
                        dataService.giveUpCard(netSocket);
                        break;
                    case CmdConstant.DISMISS:
                        dataService.dismiss(netSocket);
                        break;
                    case CmdConstant.CHAT:
                        dataService.chat(netSocket, request.getParams());
                        break;
                    case CmdConstant.APPLY_SETTLE:
                        dataService.applySettle(netSocket);
                        break;
                    default:
                        System.out.println("bad request");
                }
            } catch (BusinessException e) {
                netSocket.write(A0Json.encode(new Result.Builder().setMessage(e.getMessage())
                        .setCode(e.getErrorCode()).build()));
            } catch (Exception e) {
                e.printStackTrace();
                netSocket.write(A0Json.encode(new Result.Builder()
                        .setMessage(ErrorCodeEnum.SYSTEM_ERROR.getMessage())
                        .setCode(ErrorCodeEnum.SYSTEM_ERROR.getCode() + "").build()));
            }

        }).closeHandler(aVoid -> {
            System.out.println("client closed mahjong");
            dataService.closeConnect(netSocket);
        });
    }

    public void execute(RoutingContext routingContext) {


        String method = routingContext.request().getParam("method");
        System.out.println("http ----> method: " + method);



        if (StringUtils.isBlank(method)) {
            throw new BusinessException(ErrorCodeEnum.REQUEST_ERROR);
        }

        if (StringUtils.equals(method, "get.info")) {
            getInfo(routingContext);
        } else if (StringUtils.equals(method, "sign.desk")) {
            signDesk(routingContext);
        } else {
            throw new BusinessException(ErrorCodeEnum.REQUEST_ERROR);
        }
    }

    private void signDesk(RoutingContext routingContext) {
        Integer userId = Integer.valueOf(routingContext.request().getParam("userId"));

        if (userId == null) {
            throw new BusinessException(ErrorCodeEnum.ERROR_PARAM);
        }
        CompletableFuture<List<JsonObject>> allFuture = new CompletableFuture<>();
        JsonArray queryParams = new JsonArray().add(userId);
        mysqlClient.getConnection(connection -> {
            if (connection.failed()) {
                allFuture.completeExceptionally(connection.cause());
            }
            if (connection.succeeded()) {
                connection.result().queryWithParams("SELECT id, diamonds, sign_count from nee_users where id = ?", queryParams, result -> {
                    if (result.failed()) allFuture.completeExceptionally(result.cause());
                    else {
                        allFuture.complete(result.result().getRows());
                    }
                    connection.result().close();
                });
            }
        });
        allFuture.thenCompose(res -> {
            CompletableFuture future = new CompletableFuture();
            if (res.size() > 0) {
                Integer signCount = res.get(0).getInteger("sign_count");
                if (signCount == null) signCount = 0;
                int diamonds = res.get(0).getInteger("diamonds");
                signCount += 1;
                final int addDiamonds = signCount % 30;
                diamonds += addDiamonds;

                JsonArray updateParams = new JsonArray().add(diamonds).add(signCount).add(userId);
                mysqlClient.getConnection(connection -> {
                    if (connection.failed()) {
                        future.completeExceptionally(connection.cause());
                    }
                    if (connection.succeeded()) {
                        connection.result().queryWithParams("update nee_users set diamonds = ?, sign_count = ?, " +
                                "last_sign_date = now() where id = ?", updateParams, result -> {
                            if (result.failed()) {
                                future.completeExceptionally(result.cause());
                            } else {
                                future.complete(addDiamonds);
                            }
                            connection.result().close();
                        });
                    }
                });
            } else {
                future.complete(0);
            }

            return future;
        }).whenComplete((res, ex) -> {
            if (ex != null) {
                System.out.println(ex.toString());
                routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                        .end(A0Json.encode(new Result.Builder().setCode(ErrorCodeEnum.SYSTEM_ERROR.getCode() + "")
                                .setMessage(ErrorCodeEnum.SYSTEM_ERROR.getMessage()).build()));
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("addDiamonds", res);
                routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                        .end(A0Json.encode(new Result.Builder().setData(data).build()));
            }
        });
    }


    private void getInfo(RoutingContext routingContext) {
        Integer userId = Integer.valueOf(routingContext.request().getParam("userId"));

        System.out.println("userId: " + userId);

        if (userId == null) {
            throw new BusinessException(ErrorCodeEnum.ERROR_PARAM);
        }
        CompletableFuture allFuture = new CompletableFuture();
        JsonArray queryParams = new JsonArray().add(userId);
        mysqlClient.getConnection(connection -> {
            if (connection.failed()) {
                allFuture.completeExceptionally(connection.cause());
            }
            if (connection.succeeded()) {
                connection.result().queryWithParams("SELECT id, nickname, headimgurl, diamonds, integral, game_count, " +
                        "win_count, sign_count, last_sign_date from nee_users where id = ?", queryParams, result -> {
                    if (result.failed()) allFuture.completeExceptionally(result.cause());
                    else {
                        allFuture.complete(result.result().getRows());
                    }
                    connection.result().close();
                });
            }
        });
        allFuture.whenComplete((res, ex) -> {
            if (ex != null) {
                routingContext.response()/*.putHeader("content-type", "application/json; charset=utf-8")*/
                        .end(A0Json.encode(new Result.Builder().setCode(ErrorCodeEnum.SYSTEM_ERROR.getCode() + "")
                                .setMessage(ErrorCodeEnum.SYSTEM_ERROR.getMessage()).build()));
            } else {
                routingContext.response()/*.putHeader("content-type", "application/json; charset=utf-8")*/
                        .end(A0Json.encode(new Result.Builder().setData(res).build()));
            }
        });


    }
}
