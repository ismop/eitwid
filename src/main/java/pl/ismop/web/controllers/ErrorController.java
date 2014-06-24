package pl.ismop.web.controllers;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ErrorController {
	private static final Logger log = LoggerFactory.getLogger(ErrorController.class);
	
	@ExceptionHandler(Throwable.class)
	public ModelAndView handleErrorsGlobally(Throwable t) {
		log.error("Global error occurred", t);
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("timestamp", new Date());
		modelAndView.addObject("messages", t.getMessage());
		modelAndView.setViewName("error");
		
		return modelAndView;
	}
}