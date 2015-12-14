package pl.ismop.web.controllers.main;

import static java.util.Calendar.HOUR;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
	@Autowired private JavaMailSender mailSender;
	
	@Value("${secret.token}") private String secretToken;
	@Value("${dap.token}") private String dapToken;
	@Value("${dap.endpoint}") private String dapEndpoint;
	@Value("${maps.google.key}") private String googleMapApiKey;
	@Value("${hypgen.user}") private String hypgenUser;
	@Value("${hypgen.pass}") private String hypgenPass;
	@Value("${hypgen.endpoint}") private String hypgenEndpoint;
	@Value("${change.password.link.expiry.hours}") private String changePasswordExpiryHours;
	
	@RequestMapping("/")
	public String home(Model model, HttpServletRequest request) {
		model.addAttribute("googleMapApiKey", googleMapApiKey);
		model.addAttribute("dapToken", dapToken);
		model.addAttribute("dapEndpoint", dapEndpoint);
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
	
	@RequestMapping("/changePassword/{token}")
	public ModelAndView changePassword(@PathVariable String token, HttpServletRequest request) {
		PasswordChangeToken changeToken = passwordChangeTokenRepository.findOneByToken(token);
		
		if(changeToken != null) {
			Calendar minusEpiryPeriodTime = Calendar.getInstance();
			minusEpiryPeriodTime.add(HOUR, -Integer.parseInt(changePasswordExpiryHours));
			
			if(changeToken.getGenerationDate().after(minusEpiryPeriodTime.getTime())) {
				Passwords passwords = new Passwords();
				passwords.setToken(token);
				
				return new ModelAndView("changePassword", "passwords", passwords);
			} else {
				return new ModelAndView("changePassword", "error", messages.getMessage("change.password.token.expired.error", null, request.getLocale()));
			}
		} else {
			return new ModelAndView("changePassword", "error", messages.getMessage("change.password.no.token.error", null, request.getLocale()));
		}
	}
	
	@RequestMapping(value = "/changePassword", method = RequestMethod.POST)
	@Transactional
	public ModelAndView processChangePassword(@Valid Passwords passwords, BindingResult errors, HttpServletRequest request) {
		if(errors.hasErrors()) {
			return new ModelAndView("changePassword");
		} else {
			PasswordChangeToken changeToken = passwordChangeTokenRepository.findOneByToken(passwords.getToken());
			
			if(changeToken != null) {
				User user = userReposiotory.findOneByEmail(changeToken.getEmail());
				
				if(user != null) {
					user.setPasswordHash(passwordEncoder.encode(passwords.getPassword()));
					userReposiotory.save(user);
					
					return new ModelAndView("changePassword", "success", messages.getMessage("change.password.success", null, request.getLocale()));
				} else {
					return new ModelAndView("changePassword", "error", messages.getMessage("change.password.no.token.error", null, request.getLocale()));
				}
				
			} else {
				return new ModelAndView("changePassword", "error", messages.getMessage("change.password.no.token.error", null, request.getLocale()));
			}
		}
	}
	
	@RequestMapping("/forgotPassword")
	public String forgotPassword(Model model) {
		model.addAttribute("email", new Email());
		
		return "forgotPassword";
	}
	
	@RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
	@Transactional
	public ModelAndView processForgotPassword(@Valid Email email, BindingResult errors, HttpServletRequest request) {
		if(errors.hasErrors()) {
			return new ModelAndView("forgotPassword");
		} else {
			User user = userReposiotory.findOneByEmail(email.getEmail());
			
			if(user == null) {
				errors.addError(new FieldError("email", "email", messages.getMessage("email.missing.error", null, request.getLocale())));
			} else {
				PasswordChangeToken passwordChangeToken = new PasswordChangeToken();
				passwordChangeToken.setGenerationDate(new Date());
				passwordChangeToken.setToken(UUID.randomUUID().toString());
				passwordChangeToken.setEmail(user.getEmail());
				passwordChangeTokenRepository.save(passwordChangeToken);
				
				String url = fromMethodCall(on(MainController.class).changePassword("{token}", null)).buildAndExpand(passwordChangeToken.getToken()).
						encode().toUri().toString();
				log.debug("Generated password change token for email {}", email.getEmail());
				
				SimpleMailMessage message = new SimpleMailMessage();
				message.setTo(email.getEmail());
				message.setSubject(messages.getMessage("password.change.message.subject", null, request.getLocale()));
				message.setText(messages.getMessage("password.change.message.body", new Object[] {url, changePasswordExpiryHours}, request.getLocale()));
				mailSender.send(message);
				email.setEmail("");
				
				return new ModelAndView("forgotPassword", "success", messages.getMessage("change.password.mail.sent", null, request.getLocale()));
			}
			
			return new ModelAndView("forgotPassword");
		}
	}
	
	private String createHypgenToken() {
		return Base64.getEncoder().encodeToString((hypgenUser + ":" + hypgenPass).getBytes());
	}
}