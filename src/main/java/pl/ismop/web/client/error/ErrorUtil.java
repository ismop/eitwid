package pl.ismop.web.client.error;

import javax.inject.Singleton;

import org.fusesource.restygwt.client.Method;

@Singleton
public class ErrorUtil {
	public ErrorDetails processErrors(Method method, Throwable exception) {
		ErrorDetails details = new ErrorDetails();
		details.setMessage(exception.getMessage());
		
		return details;
	}
}