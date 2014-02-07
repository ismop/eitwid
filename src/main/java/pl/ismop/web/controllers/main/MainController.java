package pl.ismop.web.controllers.main;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainController {
	private static final Logger log = LoggerFactory.getLogger(MainController.class);
	
	@RequestMapping("/")
	public String home() {
		return "home";
	}
	
	@RequestMapping("/login")
	public String login() {
		return "login";
	}
	
	@RequestMapping("/register")
	public String register(Model model) {
		model.addAttribute("registration", new Registration());
		
		return "register";
	}
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String processRegistration(@Valid Registration registration, BindingResult errors) {
		if (errors.hasErrors()) {
			return "register";
		} else {
			if (!registration.getPassword().equals(registration.getConfirmPassword())) {
				errors.addError(new FieldError("registration", "password", "Passwords do not match"));
				
				return "register";
			} else {
				return "redirect:/login?registrationSuccessful";
			}
		}
	}
}