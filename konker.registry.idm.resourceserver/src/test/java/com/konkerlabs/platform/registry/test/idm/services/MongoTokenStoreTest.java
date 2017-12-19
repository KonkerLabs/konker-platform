package com.konkerlabs.platform.registry.test.idm.services;

import com.konkerlabs.platform.registry.idm.services.MongoTokenStore;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
public class MongoTokenStoreTest extends BusinessLayerTestSupport {

    private static final String TOKEN = "wqh3blfw2o";

    private static final String CLIENT_ID = "client-id-j2lcaw75as";

    private OAuth2Authentication authentication;

    private OAuth2AccessToken oAuth2AccessToken;

    @Autowired
    private MongoTokenStore mongoTokenStore;

    @Before
    public void setUp() {

        OAuth2AccessToken token = new DefaultOAuth2AccessToken(TOKEN);

        // check if token not exists
        oAuth2AccessToken = mongoTokenStore.readAccessToken(TOKEN);
        assertThat(oAuth2AccessToken, nullValue());

        // create token
        Set<String> scopes = new HashSet<>();
        scopes.add("read");

        OAuth2Request storedRequest = new OAuth2Request(
                new HashMap<>(),
                CLIENT_ID,
                new LinkedList<GrantedAuthority>(),
                true,
                scopes,
                new HashSet<String>(),
                null,
                null,
                null
        );

        Authentication userAuthentication = new PreAuthenticatedAuthenticationToken(null, null);
        authentication = new OAuth2Authentication(storedRequest, userAuthentication);

        mongoTokenStore.storeAccessToken(token, authentication);

    }

    @Test
    public void getAccessToken() {

        // get access token by authentication
        oAuth2AccessToken = mongoTokenStore.getAccessToken(authentication);
        assertThat(oAuth2AccessToken, notNullValue());
        assertThat(oAuth2AccessToken.getValue(), is(TOKEN));

    }

    @Test
    public void readAccessToken() {

        // check if token was created successfully
        oAuth2AccessToken = mongoTokenStore.readAccessToken(TOKEN);
        assertThat(oAuth2AccessToken, notNullValue());
        assertThat(oAuth2AccessToken.getValue(), is(TOKEN));

    }

    @Test
    public void readAuthentication() {

        oAuth2AccessToken = mongoTokenStore.readAccessToken(TOKEN);
        assertThat(oAuth2AccessToken, notNullValue());

        OAuth2Authentication oAuth2Authentication = mongoTokenStore.readAuthentication(oAuth2AccessToken);
        assertThat(oAuth2Authentication, notNullValue());
        assertTrue(oAuth2Authentication.getOAuth2Request().isApproved());
        assertThat(oAuth2Authentication.getOAuth2Request().getClientId(), is(CLIENT_ID));
        assertThat(oAuth2Authentication.getOAuth2Request().getScope(), hasItem("read"));
        assertThat(oAuth2Authentication.getOAuth2Request().getScope(), CoreMatchers.not(hasItem("trust")));

    }

    @Test
    public void removeAccessToken() {

        oAuth2AccessToken = mongoTokenStore.readAccessToken(TOKEN);
        assertThat(oAuth2AccessToken, notNullValue());

        // remove token
        mongoTokenStore.removeAccessToken(oAuth2AccessToken);

        // removed?
        oAuth2AccessToken = mongoTokenStore.readAccessToken(TOKEN);
        assertThat(oAuth2AccessToken, nullValue());

    }

    @Test
    public void findTokensByClientId() {

        Collection<OAuth2AccessToken> tokens =  mongoTokenStore.findTokensByClientId(CLIENT_ID);
        assertThat(tokens.size(), is(1));

    }


}
