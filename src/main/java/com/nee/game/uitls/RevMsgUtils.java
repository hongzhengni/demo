package com.nee.game.uitls;

import com.nee.game.common.A0Json;
import com.nee.game.common.Result;
import com.nee.game.data.entities.User;
import io.vertx.core.json.Json;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * return msg util
 */
public class RevMsgUtils {

    private static ExecutorService es = Executors.newFixedThreadPool(4);

    public static void revMsg(User user, Integer cmd, Object data) {
        if (user.getNetSocket() == null) {
            System.out.println("user (" + user.getNick() + ") socket is null ");
            return;
        }
        Result result = new Result.Builder()
                .setCmd(cmd)
                .setData(data)
                .build();

        System.out.println("send message: ----->  cmd " + cmd + ", data " + Json.encode(data));
        user.getNetSocket().write(A0Json.encode(result));
    }

    public static void revMsg(Collection<User> users, Integer cmd, Object data) {
        if (users == null || users.size() <= 0) {
            return;
        }
        System.out.println("send message: ----->  cmd " + cmd + ", data " + Json.encode(data) + ", users: " + Json.encode(users));
        Result result = new Result.Builder()
                .setCmd(cmd)
                .setData(data)
                .build();
        users.stream().filter(user -> user != null && user.getNetSocket() != null)
                .forEach(user -> user.getNetSocket().write(A0Json.encode(result)));
    }

    public static void revMsg(List<User> users, User currentUser, Integer cmd, Object data) {
        if (users == null || users.size() <= 0) {
            return;
        }

        Result result = new Result.Builder()
                .setCmd(cmd)
                .setData(data)
                .build();
        for (User user : users) {
            if (user != null && currentUser != user && user.getNetSocket() != null) {
                es.submit(new Thread(() -> user.getNetSocket().write(A0Json.encode(result))));
            }
        }
    }
/*
    public static void main(String args[]) {

        for (int i = 0; i < 10; i++) {
            es.execute(new Thread(() -> {
                System.out.println("xxxxxx");
            }));
        }

        System.out.println("xx");
    }*/
}
