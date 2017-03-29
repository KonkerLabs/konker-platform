package com.konkerlabs.platform.registry.test.base;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

import org.junit.After;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;

public class BusinessLayerTestSupport {

    @SuppressWarnings("unused")
    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("registry-test");

    @After
    public void tearDown() throws Exception {
        mongoDbRule.getDatabaseOperation().deleteAll();
    }

}
