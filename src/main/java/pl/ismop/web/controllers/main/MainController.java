package pl.ismop.web.controllers.main;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

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
import org.springframework.web.servlet.ModelAndView;

import pl.ismop.web.domain.PasswordChangeToken;
import pl.ismop.web.domain.User;
import pl.ismop.web.repository.PasswordChangeTokenRepository;
import pl.ismop.web.repository.UserRepository;

@Controller
public class MainController {
	private static final Logger log = LoggerFactory.getLogger(MainController.class);
	
	@Autowired private UserRepository userReposiotory;
	@Autowired private PasswordChangeTokenRepository passwordChangeTokenRepository;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private MessageSource messages;
	
	@Value("${secret.token}") private String secretToken;
	@Value("${dap.token}") private String dapToken;
	@Value("${dap.endpoint}") private String dapEndpoint;
	@Value("${maps.google.key}") private String googleMapApiKey;
	@Value("${hypgen.user}") private String hypgenUser;
	@Value("${hypgen.pass}") private String hypgenPass;
	@Value("${hypgen.endpoint}") private String hypgenEndpoint;
	
	@RequestMapping("/")
	public String home(Model model, HttpServletRequest request) {
		model.addAttribute("googleMapApiKey", googleMapApiKey);
		model.addAttribute("dapToken", dapToken);
		model.addAttribute("dapEndpoint", "/dapproxy");
		model.addAttribute("hypgenEndpoint", "/hypgenproxy");
		model.addAttribute("hypgenToken", createHypgenToken());
		
		return "workspace";
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
			if(!secretToken.equals(registration.getSecretToken())) {
				errors.addError(new FieldError("registration", "secretToken", messages.getMessage("secret.token.mismatch", null, request.getLocale())));
				
				return "register";
			} else {
				User user = userReposiotory.findOneByEmail(registration.getEmail());
				
				if(user != null) {
					errors.addError(new FieldError("registration", "email", messages.getMessage("email.already.registered", null, request.getLocale())));
					
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
	
	@RequestMapping("/levees")
	public String levees(Model model) {
		model.addAttribute("googleMapApiKey", googleMapApiKey);
		model.addAttribute("dapToken", dapToken);
		model.addAttribute("dapEndpoint", dapEndpoint);
		model.addAttribute("hypgenEndpoint", hypgenEndpoint);
		model.addAttribute("hypgenToken", createHypgenToken());
		
		return "levees";
	}
	
	@RequestMapping("/changePassword/{token}")
	public ModelAndView changePassword(@PathVariable String token) {
		return new ModelAndView("changePassword", "passwords", new Passwords());
	}
	
	@RequestMapping(value = "/changePassword", method = RequestMethod.POST)
	@Transactional
	public String processChangePassword() {
		return "";
	}
	
	@RequestMapping("/forgotPassword")
	public String forgotPassword(Model model) {
		model.addAttribute("email", new Email());
		
		return "forgotPassword";
	}
	
	@RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
	@Transactional
	public String processForgotPassword(@Valid Email email, BindingResult errors, HttpServletRequest request) {
		if(errors.hasErrors()) {
			return "forgotPassword";
		} else {
			User user = userReposiotory.findOneByEmail(email.getEmail());
			
			if(user == null) {
				errors.addError(new FieldError("email", "email", messages.getMessage("email.missing.error", null, request.getLocale())));
			} else {
				PasswordChangeToken passwordChangeToken = new PasswordChangeToken();
				passwordChangeToken.setGenerationDate(new Date());
				passwordChangeToken.setToken(UUID.randomUUID().toString());
				passwordChangeTokenRepository.save(passwordChangeToken);
				
				String url = fromMethodCall(on(MainController.class).changePassword("{token}")).buildAndExpand(passwordChangeToken.getToken()).
						encode().toUri().toString();
				log.debug("Generated password change token for email {}", email.getEmail());
				
				//TODO
			}
			
			return "forgotPassword";
		}
	}
	
	private String createHypgenToken() {
		return Base64.getEncoder().encodeToString((hypgenUser + ":" + hypgenPass).getBytes());
	}
}