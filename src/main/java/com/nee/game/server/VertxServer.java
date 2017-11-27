package com.nee.game.server;

import com.nee.game.service.GameService;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by heikki on 17/8/19.
 */

@Component
public class VertxServer {

    @Autowired
    private Vertx vertx;
    @Autowired
    private GameService gameService;

    @PostConstruct
    public void start() throws Exception {

        NetServer server = vertx.createNetServer();
        server.connectHandler(gameService::handle);

        server.listen(8200);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/user/v1/:id")
                .produces("application/json")
                .blockingHandler(gameService::execute);


        vertx.createHttpServer().requestHandler(router::accept).listen(8201);
    }


}
