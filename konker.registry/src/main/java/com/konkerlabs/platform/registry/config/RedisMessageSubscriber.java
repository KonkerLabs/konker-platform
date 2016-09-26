package com.konkerlabs.platform.registry.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CountDownLatch;

/**
 * Created by andre on 26/09/16.
 */
public class RedisReceiver {


    @Autowired
    public RedisReceiver(CountDownLatch latch) {

    }

    public void handleMessage() {
        System.out.println("test");
    }
}


