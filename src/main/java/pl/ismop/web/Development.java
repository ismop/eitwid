package pl.ismop.web;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@EnableAutoConfiguration
@ComponentScan
public class Development extends Application {
	private static final Logger log = LoggerFactory.getLogger(Development.class);
	
    public static void main(String[] args) {
        new SpringApplicationBuilder(Development.class).run(args);
        log.info("ISMOP Web application successfully started in development mode");
    }
	
	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("classpath:messages");
		messageSource.setCacheSeconds(1);
		
		return messageSource;
	}
}