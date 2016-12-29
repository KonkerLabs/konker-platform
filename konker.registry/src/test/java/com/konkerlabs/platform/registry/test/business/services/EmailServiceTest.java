package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.EmailService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse.Status;
import com.konkerlabs.platform.registry.config.SpringMailConfig;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        RedisTestConfiguration.class,
        SpringMailConfig.class
})
@UsingDataSet(locations = {"/fixtures/users.json"})
public class EmailServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;
    
    private String sender = "no-reply@konkerlabs.com";
    private String subject = "Recover Password";
    private String body = "To recover your password access the link below";
    private List<User> receivers;
    private List<User> receiversCopied;
    
    @Before
    public void setUp() throws Exception {
    	receivers = userRepository.findAll();
    	receiversCopied = Arrays.asList(userRepository.findOne("admin@konkerlabs.com"));
    }

    @Test
    public void shouldRaiseAnExceptionIfSenderIsNull() throws Exception {
    	ServiceResponse<Status> response = emailService.send(null, receivers, receiversCopied, subject, body, Locale.ENGLISH);
    	
    	assertThat(response, hasErrorMessage(EmailService.Validations.SENDER_NULL.getCode()));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfSenderIsEmpty() throws Exception {
    	ServiceResponse<Status> response = emailService.send("", receivers, receiversCopied, subject, body, Locale.ENGLISH);
    	
    	assertThat(response, hasErrorMessage(EmailService.Validations.SENDER_NULL.getCode()));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfReceiversIsNull() throws Exception {
    	ServiceResponse<Status> response = emailService.send(sender, null, receiversCopied, subject, body, Locale.ENGLISH);
    	
    	assertThat(response, hasErrorMessage(EmailService.Validations.RECEIVERS_NULL.getCode()));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfReceiversIsEmpty() throws Exception {
    	ServiceResponse<Status> response = emailService.send(sender, new ArrayList<>(), receiversCopied, subject, body, Locale.ENGLISH);
    	
    	assertThat(response, hasErrorMessage(EmailService.Validations.RECEIVERS_NULL.getCode()));
    }
     
    @Test
    public void shouldRaiseAnExceptionIfSubjectIsNull() throws Exception {
    	ServiceResponse<Status> response = emailService.send(sender, receivers, receiversCopied, null, body, Locale.ENGLISH);
    	
    	assertThat(response, hasErrorMessage(EmailService.Validations.SUBJECT_EMPTY.getCode()));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfSubjectIsEmpty() throws Exception {
    	ServiceResponse<Status> response = emailService.send(sender, receivers, receiversCopied, "", body, Locale.ENGLISH);
    	
    	assertThat(response, hasErrorMessage(EmailService.Validations.SUBJECT_EMPTY.getCode()));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfBodyIsNull() throws Exception {
    	ServiceResponse<Status> response = emailService.send(sender, receivers, receiversCopied, subject, null, Locale.ENGLISH);
    	
    	assertThat(response, hasErrorMessage(EmailService.Validations.BODY_EMPTY.getCode()));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfBodyIsEmpty() throws Exception {
    	ServiceResponse<Status> response = emailService.send(sender, receivers, receiversCopied, subject, "", Locale.ENGLISH);
    	
    	assertThat(response, hasErrorMessage(EmailService.Validations.BODY_EMPTY.getCode()));
    }
    
    @Test
    public void shouldSendAnEmail() throws Exception {
    	ServiceResponse<Status> response = emailService.send(sender, receivers, receiversCopied, subject, body, Locale.ENGLISH);
    	
    	assertThat(response, isResponseOk());
    }

}