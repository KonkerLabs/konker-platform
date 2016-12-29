package com.konkerlabs.platform.registry.business.services;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.EmailService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse.Status;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EmailServiceImpl implements EmailService {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private SpringTemplateEngine templateEngine;

	@Override
	public ServiceResponse<Status> send(String sender, 
									List<User> recipients, 
									List<User> recipientsCopied, 
									String subject,
									String body,
									Locale locale) throws MessagingException {
		
		if (!Optional.ofNullable(sender).isPresent() || sender.isEmpty()) {
			return ServiceResponseBuilder.<Status>error().withMessage(Validations.SENDER_NULL.getCode()).build();
		}
		if (!Optional.ofNullable(recipients).isPresent() || recipients.isEmpty()) {
			return ServiceResponseBuilder.<Status>error().withMessage(Validations.RECEIVERS_NULL.getCode()).build();
		}
		if (!Optional.ofNullable(subject).isPresent() || subject.isEmpty()) {
			return ServiceResponseBuilder.<Status>error().withMessage(Validations.SUBJECT_EMPTY.getCode()).build();
		}
		if (!Optional.ofNullable(body).isPresent() || body.isEmpty()) {
			return ServiceResponseBuilder.<Status>error().withMessage(Validations.BODY_EMPTY.getCode()).build();
		}
		
		final Context ctx = new Context(locale);
		ctx.setVariable("name", "Douglas");
		ctx.setVariable("subscriptionDate", new Date());
		ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports"));
		
		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
		message.setSubject(subject);
		message.setFrom(sender);
		message.setTo(recipients.stream().map(r -> r.getEmail()).collect(Collectors.toList()).toArray(new String[0]));
		
		if (Optional.ofNullable(recipientsCopied).isPresent()) {
			message.setCc(recipientsCopied.stream().map(r -> r.getEmail()).collect(Collectors.toList()).toArray(new String[0]));
		}
		
		final String htmlContent = this.templateEngine.process("html/email-recover-pass", ctx);
		message.setText(htmlContent, true);
		
		this.mailSender.send(mimeMessage);
		
		return ServiceResponseBuilder.<Status>ok().build();
	}
	
}
