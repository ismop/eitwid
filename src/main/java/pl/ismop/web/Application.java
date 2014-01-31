package pl.ismop.web;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableAutoConfiguration
@ComponentScan
public class Application extends WebMvcConfigurerAdapter {
    public static void main(String[] args) {
    	// Set user password to "password" for demo purposes only
        new SpringApplicationBuilder(Application.class).properties(
                        "security.basic.enabled=false", "security.user.password=password").run(args);
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
    	registry.addViewController("/login").setViewName("login");
    }
    
    @Bean
    public ApplicationSecurity applicationSecurity() {
            return new ApplicationSecurity();
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 8)
    protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                    http.authorizeRequests().anyRequest().fullyAuthenticated().and().formLogin()
                                    .loginPage("/login").failureUrl("/login?error").permitAll();
            }
    }
}