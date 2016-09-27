package com.konkerlabs.platform.registry.test.base;

import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.lordofthejars.nosqlunit.redis.*;
import com.lordofthejars.nosqlunit.redis.embedded.EmbeddedJedis;
import com.lordofthejars.nosqlunit.redis.embedded.EmbeddedRedisBuilder;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

public class BusinessLayerTestSupport {

    @SuppressWarnings("unused")
    @Autowired
    protected ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("registry-test");

    @After
    public void tearDown() throws Exception {
        mongoDbRule.getDatabaseOperation().deleteAll();

    }
}
