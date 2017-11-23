package com.nee.game.service;

import io.vertx.ext.asyncsql.AsyncSQLClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @Author: heikki.
 * @Description:
 * @DATE: 上午11:45 17/11/2.
 */
@Component
public class InstantiationTracingBeanPostProcessor implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private AsyncSQLClient mysqlClient;

    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("============spring 初始化完成===========");

        TimerTask timerTask = new DataService().new LoadRobotTask(mysqlClient);
        Timer timer = new Timer();
        //timer.schedule(timerTask, 1000, 10000);

        System.out.println("============spring 初始化完成===========");
    }

}
