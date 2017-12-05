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
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.web.RoutingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 */
@Component
public class GameService {

    @Autowired
    private DataService dataService;

    public void handle(NetSocket netSocket) {

        System.out.println("Mahjong incoming connection!");
        netSocket.handler(buffer -> {
            try {
                Request<Params> request = A0Json.decodeValue(buffer.getString(0, buffer.length()),
                        new TypeReference<Request<Params>>() {
                        });
                int cmd = request.getCmd();
                System.out.println(Json.encode(request));

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

        JsonObject object = routingContext.getBodyAsJson();


        String method = object.getString("method");
        String version = object.getString("version");
        if (StringUtils.isBlank(method)) {
            throw new BusinessException(ErrorCodeEnum.REQUEST_ERROR);
        }
        if (StringUtils.isBlank(version)) {
            throw new BusinessException(ErrorCodeEnum.VERSION_ERROR);
        }

       /* if (StringUtils.equals(method, "register")) {

        } else if (StringUtils.equals(method, "login")) {

        } else if (StringUtils.equals(method, "logout")) {

        } else if (StringUtils.equals(method, "forget.password")) {

        } else if (StringUtils.equals(method, "change.password")) {

        } else {
            throw new BusinessException(ErrorCodeEnum.REQUEST_ERROR);
        }*/
    }
}
