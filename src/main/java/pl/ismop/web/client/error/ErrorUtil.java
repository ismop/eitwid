package pl.ismop.web.client.error;

import javax.inject.Singleton;

import org.fusesource.restygwt.client.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ErrorUtil {

	private static final Logger log = LoggerFactory.getLogger(ErrorUtil.class);

	public static class CommunicationException extends Exception {

		private static final long serialVersionUID = 1562686931404313175L;

		public CommunicationException(String message) {
			super(message);
		}
	}

	public ErrorDetails processErrors(Method method, Throwable exception) {
		printStackTrace(exception);

		return new ErrorDetails(exception.getMessage());
	}

	public CommunicationException processErrorsForException(Method method, Throwable exception) {
		printStackTrace(exception);

		return new CommunicationException(exception.getMessage());
	}

	private void printStackTrace(Throwable exception) {
		log.error("Error detected: ", exception);
	}
}
