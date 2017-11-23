package com.nee.game;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by heikki on 17/10/19.
 */
@EnableTransactionManagement(proxyTargetClass=true)
@EnableAspectJAutoProxy
@SpringBootApplication(scanBasePackages = "com.nee.game.data, com.nee.game.server, com.nee.game.service, com.nee.game.common.aop")
public class Application {

    private Vertx vertx;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }

    /**
     * A singleton instance of {@link Vertx} which is used throughout the application
     * @return An instance of {@link Vertx}
     */
    @Bean
    public Vertx getVertxInstance() {
        if (this.vertx==null) {

            this.vertx = Vertx.vertx();
        }
        return this.vertx;
    }

    /**
     * A singleton instance of {@link AsyncSQLClient} which is used throughout the application
     * @return An instance of {@link AsyncSQLClient}
     */
    @Bean
    public AsyncSQLClient getMysqlClientInstance() {
        JsonObject mySQLClientConfig = new JsonObject().put("host", "106.15.205.55").put("port", 3306)
                .put("database", "game").put("username", "root").put("password", "12345Aa");

        return MySQLClient.createShared(vertx, mySQLClientConfig);
    }


}
