package pl.ismop.web.client.hypgen;

import org.fusesource.restygwt.client.Dispatcher;
import org.fusesource.restygwt.client.Method;

import pl.ismop.web.client.IsmopWebEntryPoint;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;

public class HypgenDispatcher implements Dispatcher {
	public static final HypgenDispatcher INSTANCE = new HypgenDispatcher();
	
	@Override
	public Request send(Method method, RequestBuilder builder) throws RequestException {
		builder.setHeader("Authorization", "Basic " + IsmopWebEntryPoint.properties.get("hypgenToken"));
		
		return builder.send();
	}
}