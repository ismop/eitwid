package pl.ismop.web.client.error;

import javax.inject.Singleton;

import org.fusesource.restygwt.client.Method;

@Singleton
public class ErrorUtil {
	public static class CommunicationException extends Exception {
		private static final long serialVersionUID = 1562686931404313175L;

		public CommunicationException(String message) {
			super(message);
		}
	}
	
	public ErrorDetails processErrors(Method method, Throwable exception) {
		return new ErrorDetails(exception.getMessage());
	}
	
	public CommunicationException processErrorsForException(Method method, Throwable exception) {
		return new CommunicationException(exception.getMessage());
	}
}