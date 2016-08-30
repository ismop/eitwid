package pl.ismop.web;

import org.apache.catalina.Context;
import org.apache.catalina.session.FileStore;
import org.apache.catalina.session.PersistentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
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

@EnableAutoConfiguration
@EnableWebSecurity
@ComponentScan
public class Application extends WebMvcConfigurerAdapter {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	@Value("${dap.token}") private String dapToken;
	@Value("${dap.endpoint}") private String dapEndpoint;
	@Value("${public.maps.server.url}") private String mapsServerUrl;
	@Value("${session.store.path}") private String sessionStorePath;
	@Value("${hypgen.endpoint}") private String hypgenEndpoint;

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
	public EmbeddedServletContainerFactory servletContainer() {
		TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
		factory.addContextCustomizers(new TomcatContextCustomizer() {
			@Override
			public void customize(Context context) {
				FileStore store = new FileStore();
				store.setDirectory(sessionStorePath);

				PersistentManager manager = new PersistentManager();
				manager.setStore(store);
				context.setManager(manager);
			}
		});

	    return factory;
	}
}
