package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.billing.model.TenantDailyUsage;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.services.api.IuguService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.IuguConfig;
import com.konkerlabs.platform.registry.web.forms.IuguCustomerForm;
import com.konkerlabs.platform.registry.web.forms.UserForm;
import com.konkerlabs.platform.registry.web.services.api.AvatarService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


@Controller()
@Scope("request")
@RequestMapping("/me")
public class UserController implements ApplicationContextAware {

    @Autowired
    private UserService userService;
    @Autowired
    private AvatarService avatarService;
    @Autowired
    private TenantService tenantService;
    @Autowired
	private IuguService iuguService;

    private User user;
    private IuguConfig iuguConfig = new IuguConfig();

    public enum Messages {
        USER_UPDATED_SUCCESSFULLY("controller.user.updated.success");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public UserController(UserService userService, User user) {
        this.user = user;
    }
    
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ModelAndView userPage(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        ServiceResponse<List<TenantDailyUsage>> serviceResponse = tenantService.findTenantDailyUsage(user.getTenant());
    	List<TenantDailyUsage> usages = serviceResponse.getResult();

        ModelAndView mv = new ModelAndView("users/form")
                .addObject("user", new UserForm().fillFrom(user))
                .addObject("tenantPlan", user.getTenant().getPlan() != null ? user.getTenant().getPlan().getValue() : null)
                .addObject("action", "/me")
                .addObject("dateformats", DateFormat.values())
                .addObject("languages", Language.values())
                .addObject("timezones", TimeZone.values())
                .addObject("loglevels", LogLevel.values())
                .addObject("usedSpace", formatSize(usages.stream().mapToInt(u -> u.getIncomingPayloadSize() + u.getOutgoingPayloadSize()).sum()));

		if(user.getTenant().isChargeable()) {
			ServiceResponse<KonkerIuguCharge> chargeServiceResponse = iuguService.findNextCharge(user.getTenant());

			if(chargeServiceResponse.isOk()) {
				mv.addObject("nextCharge", chargeServiceResponse.getResult());
			}
		}

        return mv;
    }

	@RequestMapping(value = "", method = RequestMethod.POST)
	public ModelAndView save(UserForm userForm, RedirectAttributes redirectAttributes, Locale locale) {

		User fromForm = userForm.toModel();
		fromForm.setEmail(this.user.getEmail());
		
		// update avatar
		ServiceResponse<User> avatarServiceResponse = avatarService.updateAvatar(fromForm);
		if (!avatarServiceResponse.isOk()) {
			return redirectErrorMessages(redirectAttributes, locale, avatarServiceResponse);
		}
		
		// update user
		ServiceResponse<User> serviceResponse = userService.save(fromForm, userForm.getOldPassword(),
				userForm.getNewPassword(), userForm.getNewPasswordConfirmation());

		if (!serviceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
			return redirectErrorMessages(redirectAttributes, locale, serviceResponse);
		}

		// update tenant
		LogLevel newLogLevel = userForm.getLogLevel();
		ServiceResponse<Tenant> tenServiceResponse = tenantService.updateLogLevel(this.user.getTenant(),
				newLogLevel);

		if (!tenServiceResponse.getStatus().equals(ServiceResponse.Status.OK)) {
			return redirectErrorMessages(redirectAttributes, locale, tenServiceResponse);
		} else {
			user.getTenant().setLogLevel(newLogLevel);
		}

		// success
		redirectAttributes.addFlashAttribute("message", applicationContext
				.getMessage(Messages.USER_UPDATED_SUCCESSFULLY.getCode(), null, locale));

		return new ModelAndView("redirect:/me");
	}

	@RequestMapping(value = "/plans", method = RequestMethod.GET)
	public ModelAndView plans() {
    	ModelAndView mv = new ModelAndView("payment/plans");
    	mv.addObject("user", user);
    	mv.addObject("action", "/me/plans/checkout");
        mv.addObject("plans", Tenant.PlanEnum.values());

        return mv;
	}

	@RequestMapping(value = "/plans/checkout", method = RequestMethod.GET)
	public ModelAndView checkoutView(@ModelAttribute("iuguCustomerForm")IuguCustomerForm iuguForm,
									 RedirectAttributes redirectAttributes,
									 Locale locale) {
		ModelAndView mv = getModelAndViewCheckout(iuguForm);

		return mv;
	}

	@RequestMapping(value = "/plans/checkout", method = RequestMethod.POST)
	public ModelAndView checkout(@ModelAttribute("iuguCustomerForm")IuguCustomerForm iuguForm,
								 RedirectAttributes redirectAttributes,
								 Locale locale) {
    	iuguForm.setEmail(user.getEmail());

		ServiceResponse<IuguCustomer> serviceIuguCustomer = iuguService.createIuguCustomer(iuguForm.toModel());
		if (!serviceIuguCustomer.getStatus().equals(ServiceResponse.Status.OK)) {
			return errorCheckoutMessage(iuguForm,
					locale,
					serviceIuguCustomer.getResponseMessages());
		}

		IuguCustomer iuguCustomer = serviceIuguCustomer.getResult();
		IuguPaymentWay iuguPaymentWay = IuguPaymentWay.builder()
				.customerId(iuguCustomer.getId())
				.description("Konker Payment Way")
				.token(iuguForm.getCardToken())
				.setAsDefault(true)
				.build();

		ServiceResponse<IuguPaymentWay> servicePaymentWay = iuguService.createPaymentWay(iuguPaymentWay);
		if (!servicePaymentWay.getStatus().equals(ServiceResponse.Status.OK)) {
			return errorCheckoutMessage(iuguForm,
					locale,
					servicePaymentWay.getResponseMessages());
		}

        Tenant.PlanEnum konkerPlanEnum = Tenant.PlanEnum.valueOf(iuguForm.getPlan().toUpperCase());
		LocalDate now = LocalDate.now();
		LocalDate expiresAt = LocalDate.of(now.getYear(), now.getMonthValue() + 1, 5);
		IuguSubscription iuguSubscription = IuguSubscription.builder()
				.customerId(iuguCustomer.getId())
				.planIdentifier(konkerPlanEnum.name())
				.expiresAt(expiresAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
				.onlyOnChargeSuccess("false")
				.ignoreDueEmail("false")
				.payableWith("credit_card")
				.creditsBased("false")
				.twoStep("false")
				.suspendOnInvoiceExpired("false")
				.build();
		ServiceResponse<IuguSubscription> subscriptionResponse = iuguService.createSubscription(iuguSubscription);
		if (!subscriptionResponse.getStatus().equals(ServiceResponse.Status.OK)) {
			return errorCheckoutMessage(iuguForm,
					locale,
					subscriptionResponse.getResponseMessages());
		}

        KonkerIuguPlan konkerIuguPlan = KonkerIuguPlan.builder()
                .tenantName(user.getTenant().getName())
                .tenantDomain(user.getTenant().getDomainName())
                .iuguCustomerId(iuguCustomer.getId())
                .iuguPlanIdentifier(konkerPlanEnum.name())
				.iuguSubscriptionId(subscriptionResponse.getResult().getId())
				.name(user.getName())
				.email(user.getEmail())
				.zipCode(iuguForm.getZipCode())
				.street(iuguForm.getStreet())
				.city(iuguForm.getCity())
				.state(iuguForm.getState())
				.country(iuguForm.getCountry())
                .build();
		iuguService.createKonkerIuguPlan(konkerIuguPlan);

		user.getTenant().setChargeable(false);
		user.getTenant().setPlan(konkerPlanEnum);
		tenantService.save(user.getTenant());

		if (iuguForm.isKit()) {
			iuguService.payForKit(konkerIuguPlan);
		}

		return new ModelAndView("redirect:/me/plans/checkout/success");
	}

	@RequestMapping(value = "/plans/checkout/success", method = RequestMethod.GET)
	public ModelAndView checkoutSuccess() {
		ModelAndView mv = new ModelAndView("payment/checkout-success");
		mv.addObject("action", "/me");

		return mv;
	}

	private String formatSize(Integer size) {
		if (size.equals(0))
			return "0 B";
		
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		
		int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	private ModelAndView errorCheckoutMessage(IuguCustomerForm iuguForm, Locale locale, Map<String, Object[]> responseMessages) {
		List<String> messages = responseMessages.entrySet().stream()
				.map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
				.collect(Collectors.toList());

		ModelAndView mv = getModelAndViewCheckout(iuguForm);
		mv.addObject("errors", messages);

        return mv;
	}

	private ModelAndView getModelAndViewCheckout(IuguCustomerForm iuguForm) {
		ModelAndView mv = new ModelAndView("payment/checkout");
		mv.addObject("user", user);
		mv.addObject("action", "/me/plans/checkout");
		mv.addObject("plan", Tenant.PlanEnum.valueOf(iuguForm.getPlan().toUpperCase()));
		mv.addObject("kit", iuguForm.isKit());
		mv.addObject("iuguAccountId", iuguConfig.getAccountId());
		mv.addObject("iuguTestMode", iuguConfig.isTestMode());
		return mv;
	}

	private ModelAndView redirectErrorMessages(RedirectAttributes redirectAttributes, Locale locale,
			ServiceResponse<?> serviceResponse) {
		List<String> messages = serviceResponse.getResponseMessages().entrySet().stream()
				.map(message -> applicationContext.getMessage(message.getKey(), message.getValue(), locale))
				.collect(Collectors.toList());
		redirectAttributes.addFlashAttribute("errors", messages);

		return new ModelAndView("redirect:/me");
	}

}
