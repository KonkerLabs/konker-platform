package com.konkerlabs.platform.registry.idm.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalControllerExceptionHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

	@ExceptionHandler(value = Throwable.class)
	public ModelAndView defaultErrorHandler(HttpServletRequest req, HttpServletResponse res, Throwable ex) {
		LOGGER.error("GlobalControllerExceptionHandler: ", ex);
		return new ModelAndView("error/index");
	}
	
	@ExceptionHandler(value = NoHandlerFoundException.class)
	public ModelAndView handleNotFound(HttpServletRequest req, HttpServletResponse res, NoHandlerFoundException ex) {
		LOGGER.error("GlobalControllerExceptionHandler: ", ex);
		return new ModelAndView("error/index");
	}

}

