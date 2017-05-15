
package com.konkerlabs.platform.registry.idm.oauth.mongo.service;


public interface ExtractTokenKeyDigester {


    String digest(String tokenValue);


}
