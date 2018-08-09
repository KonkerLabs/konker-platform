package com.konkerlabs.platform.registry.test.web.i18n;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.MessageSourceConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class, SecurityTestConfiguration.class, WebConfig.class, EmailConfig.class, MessageSourceConfig.class })
public class EnsureTranslationForAllIdiomsTest extends WebLayerTestContext {
    @Autowired
    MessageSource messageSource;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void shouldReturnANonEmptySetOfBaseFilesForTranslation() {
        assertThat(((AbstractResourceBasedMessageSource) messageSource).getBasenameSet(), not(empty()));
    }

    @Test
    public void shouldHaveTheSameTranslationKeysInEnglishAndDefaultTranslation()
            throws IOException {
        checkIfTranslationForLocaleMatchesDefaultTranslation(Locale.ENGLISH);
    }

    @Test
    public void shouldHaveTheSameTranslationKeysInBrazilianPortugueseAndDefaultTranslation() throws Exception {
        checkIfTranslationForLocaleMatchesDefaultTranslation(
                new Locale.Builder().setLanguage("pt").setRegion("BR").build());
    }

    // utility methods
    private void checkIfTranslationForLocaleMatchesDefaultTranslation(Locale locale)
            throws IOException {
        Set<String> baseTranslationResources = ((AbstractResourceBasedMessageSource) messageSource).getBasenameSet();

        for (String s : baseTranslationResources) {
            Resource baseResource = applicationContext.getResource(s + ".properties");
            assertThat("Resource " + baseResource + " is not readable.", baseResource.isReadable(), equalTo(true));

            Properties defaultTranslation = new Properties();
            defaultTranslation.load(new InputStreamReader(baseResource.getInputStream(), "UTF-8"));

            Resource localizedResource = applicationContext.getResource(s + "_" + locale.toString() + ".properties");
            assertThat("Resource " + localizedResource + " is not readable.", localizedResource.isReadable(),
                    equalTo(true));

            Properties localizedTranslation = new Properties();
            localizedTranslation.load(new InputStreamReader(localizedResource.getInputStream(), "UTF-8"));

            Set<Object> union = new HashSet<Object>();
            union.addAll(defaultTranslation.keySet());
            union.addAll(localizedTranslation.keySet());

            Set<Object> missingKeysOnDefault = new HashSet<Object>(union);
            missingKeysOnDefault.removeAll(defaultTranslation.keySet());
            assertThat("There are keys on " + locale.toString() + " that does not exist on default translation.",
                    missingKeysOnDefault, empty());

            Set<Object> missingKeysOnLocalized = new HashSet<Object>(union);
            missingKeysOnLocalized.removeAll(localizedTranslation.keySet());
            assertThat("There are keys on the default translation do do not exist for" + locale.toString() + ".",
                    missingKeysOnLocalized, empty());
        }
    }

}
