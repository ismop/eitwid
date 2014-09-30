package pl.ismop.web.client.dap.measurement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class)
public interface MeasurementService  extends RestService {
	@GET
	@Path("https://dap.moc.ismop.edu.pl/api/v1/measurements?sensor_id={id}")
	void getMeasurements(@PathParam("id") String sensorId, MethodCallback<MeasurementsResponse> callback);
}