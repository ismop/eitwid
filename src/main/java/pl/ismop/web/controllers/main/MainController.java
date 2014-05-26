package pl.ismop.web.controllers.main;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.ismop.web.domain.User;
import pl.ismop.web.repository.UserRepository;

@Controller
public class MainController {
	private static final Logger log = LoggerFactory.getLogger(MainController.class);
	
	@Autowired private UserRepository userReposiotory;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private MessageSource messages;
	
	@Value("${secret.token}") private String secretToken;
	@Value("${dap.token}") private String dapToken;
	@Value("${dap.endpoint}") private String dapEndpoint;
	@Value("${maps.google.key}") private String googleMapApiKey;
	
	@RequestMapping("/")
	public String home(Model model, HttpServletRequest request) {
		model.addAttribute("dapEndpoint", dapEndpoint);
		
		return "summary";
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
	@Transactional
	public String processRegistration(@Valid Registration registration, BindingResult errors, HttpServletRequest request) {
		if (errors.hasErrors()) {
			return "register";
		} else {
			if (!registration.getPassword().equals(registration.getConfirmPassword())) {
				errors.addError(new FieldError("registration", "password", messages.getMessage("passwords.mismatch", null, request.getLocale())));
				
				return "register";
			} else if(!secretToken.equals(registration.getSecretToken())) {
				errors.addError(new FieldError("registration", "secretToken", messages.getMessage("secret.token.mismatch", null, request.getLocale())));
				
				return "register";
			} else {
				User user = userReposiotory.findOneByEmail(registration.getEmail());
				
				if(user != null) {
					errors.addError(new FieldError("registration", "email", "Email already registered"));
					
					return "register";
				}
				
				user = new User();
				user.setEmail(registration.getEmail());
				user.setPasswordHash(passwordEncoder.encode(registration.getPassword()));
				userReposiotory.save(user);
				
				return "redirect:/login?registrationSuccessful";
			}
		}
	}
	
	@RequestMapping("/retrieveDapToken")
	@ResponseBody
	public String retrieveDapToken() {
		return dapToken;
	}
	
	@RequestMapping("/levees")
	public String levees(Model model) {
		model.addAttribute("googleMapApiKey", googleMapApiKey);
		
		return "levees";
	}
}