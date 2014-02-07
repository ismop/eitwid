package pl.ismop.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableAutoConfiguration
@ComponentScan
public class Application extends WebMvcConfigurerAdapter {
	private static final Logger log = LoggerFactory.getLogger(Application.class);
	
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).run(args);
        log.info("ISMOP Web application successfully started");
    }
    
    @Bean
    public ApplicationSecurity applicationSecurity() {
            return new ApplicationSecurity();
    }

    protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {
    	@Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
        }
    	
        @Override
        protected void configure(HttpSecurity http) throws Exception {
        	http
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .formLogin()
            	.loginPage("/login")
            	.failureUrl("/login?error")
            	.permitAll();
        }
    }
}