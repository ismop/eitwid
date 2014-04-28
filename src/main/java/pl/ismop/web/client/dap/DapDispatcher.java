package pl.ismop.web.client.dap;

import org.fusesource.restygwt.client.Dispatcher;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;

public class DapDispatcher implements Dispatcher {
	public static final DapDispatcher INSTANCE = new DapDispatcher();
	
	@Override
	public Request send(Method method, RequestBuilder builder) throws RequestException {
		builder.setHeader("PRIVATE-TOKEN", "asd324de3");
		
		return builder.send();
	}
}