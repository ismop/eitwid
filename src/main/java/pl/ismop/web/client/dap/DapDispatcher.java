package pl.ismop.web.client.dap;

import org.fusesource.restygwt.client.Dispatcher;
import org.fusesource.restygwt.client.Method;

import pl.ismop.web.client.IsmopWebEntryPoint;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;

public class DapDispatcher implements Dispatcher {
	public static final DapDispatcher INSTANCE = new DapDispatcher();
	
	@Override
	public Request send(Method method, RequestBuilder builder) throws RequestException {
		builder.setHeader("PRIVATE-TOKEN", IsmopWebEntryPoint.properties.get("dapToken"));
		
		return builder.send();
	}
}