package pl.ismop.web.client.dap.sensor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

public interface SensorServiceSync {
	@GET
	@Path("sensors")
	@Produces("application/json")
	SensorsResponse getSensors();
}