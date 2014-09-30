package pl.ismop.web.client.dap.sensor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class)
public interface SensorService extends RestService {
	@GET
	@Path("https://dap.moc.ismop.edu.pl/api/v1/sensors")
	void getSensors(MethodCallback<SensorsResponse> callback);
	
	@GET
	@Path("https://dap.moc.ismop.edu.pl/api/v1/sensors/{id}")
	void getSensor(@PathParam("id") String sensorId, MethodCallback<SensorResponse> callback);
}