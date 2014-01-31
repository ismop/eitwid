package pl.ismop.web.controllers;

import java.util.Date;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ErrorController {
	@ExceptionHandler(Exception.class)
	public ModelAndView handleErrorsGlobally() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("timestamp", new Date());
		modelAndView.setViewName("error");
		
		return modelAndView;
	}
}