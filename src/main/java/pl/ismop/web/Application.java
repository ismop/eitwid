package pl.ismop.web;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.gwt.logging.server.RemoteLoggingServiceImpl;

@EnableAutoConfiguration
@EnableWebSecurity
@ComponentScan
public class Application extends WebMvcConfigurerAdapter {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	@Value("${dap.token}")
	private String dapToken;

	@Value("${dap.endpoint}")
	private String dapEndpoint;

	@Value("${public.maps.server.url}")
	private String mapsServerUrl;

	@Value("${hypgen.endpoint}")
	private String hypgenEndpoint;

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).run(args);
		log.info("ISMOP Web application successfully started");
	}

	@Configuration
	@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
	protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {
		@Autowired private UserDetailsService userDetailsService;
		@Autowired private PasswordEncoder passwordEncoder;

		@Autowired
		public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests()
				.antMatchers("/register", "/login**", "/forgotPassword", "/changePassword/**")
					.permitAll()
				.anyRequest()
					.authenticated()
					.and()
				.formLogin()
					.loginPage("/login")
					.failureUrl("/login?error")
					.permitAll();
		}
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public ServletRegistrationBean gwtLoggingServlet() {
		ServletRegistrationBean servletRegistration = new ServletRegistrationBean();
		servletRegistration.setServlet(new RemoteLoggingServiceImpl());
		servletRegistration.setUrlMappings(Arrays.asList("/ismopweb/remote_logging"));

		return servletRegistration;
	}
}
