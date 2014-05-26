package pl.ismop.web;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import pl.ismop.web.client.dap.levee.LeveeServiceSync;
import pl.ismop.web.services.DapService;

@EnableAutoConfiguration
@ComponentScan
public class Application extends WebMvcConfigurerAdapter {
	private static final Logger log = LoggerFactory.getLogger(Application.class);
	
	@Value("${dap.token}") private String dapToken;
	@Value("${dap.endpoint}") private String dapEndpoint;
	
	class AuthFilter implements ClientRequestFilter {
		@Override
		public void filter(ClientRequestContext requestContext) throws IOException {
			requestContext.getHeaders().add("PRIVATE-TOKEN", dapToken);
		}
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).run(args);
		log.info("ISMOP Web application successfully started");
	}

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
				.antMatchers("/register", "/login**")
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
	public ApplicationSecurity applicationSecurity() {
		return new ApplicationSecurity();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DapService dapService(LeveeServiceSync leveeService) {
		return new DapService(leveeService);
	}
	
	@Bean
	public LeveeServiceSync leveeServiceSync() throws KeyManagementException, NoSuchAlgorithmException {
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} }, new SecureRandom());
		Client client = ClientBuilder.newBuilder()
				.sslContext(sslContext)
				.hostnameVerifier((String hostname, SSLSession session) -> {
					return true;
				})
				.build();
		client.register(new AuthFilter());
		client.register(JacksonFeature.class);
		
		WebTarget target = client.target(dapEndpoint);
		LeveeServiceSync leveeService = WebResourceFactory.newResource(LeveeServiceSync.class, target);
		
		return leveeService;
	}
}