package com.nee.game.uitls;

import com.nee.game.common.A0Json;
import com.nee.game.common.Result;
import com.nee.game.data.entities.User;
import io.vertx.core.net.NetSocket;

import java.util.List;

/**
 * return msg util
 */
public class RevMsgUtils {

    public static void revMsg(User user, Integer cmd, Object data) {
        if (user.getNetSocket() == null)
            return;
        Result result = new Result.Builder()
                .setCmd(cmd)
                .setData(data)
                .build();
        user.getNetSocket().write(A0Json.encode(result));
    }

    public static void revMsg(List<User> users, Integer cmd, Object data) {
        Result result = new Result.Builder()
                .setCmd(cmd)
                .setData(data)
                .build();
        users.stream().filter(user -> user != null && user.getNetSocket() != null)
                .forEach(user -> {
                    user.getNetSocket().write(A0Json.encode(result));
                });
    }

    public static void revMsg(List<User> users, User currentUser, Integer cmd, Object data) {
        Result result = new Result.Builder()
                .setCmd(cmd)
                .setData(data)
                .build();
        users.stream().filter(user -> user != null && currentUser != user && user.getNetSocket() != null)
                .forEach(user -> {
                    user.getNetSocket().write(A0Json.encode(result));
                });
    }
}
