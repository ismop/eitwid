package pl.ismop.web.client.internal;

import org.fusesource.restygwt.client.Dispatcher;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.DOM;

public class InternalExperimentDispatcher implements Dispatcher {
	public static final InternalExperimentDispatcher INSTANCE = new InternalExperimentDispatcher();
	
	@Override
	public Request send(Method method, RequestBuilder builder) throws RequestException {
		if(builder.getHTTPMethod().equalsIgnoreCase("put")) {
			String csrfHeaderName = DOM.getElementById("csrfHeaderName").getAttribute("content");
			String csrfToken = DOM.getElementById("csrfToken").getAttribute("content");
			builder.setHeader(csrfHeaderName, csrfToken);
		}
		
		return builder.send();
	}
}