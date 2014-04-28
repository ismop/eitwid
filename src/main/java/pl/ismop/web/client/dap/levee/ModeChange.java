package pl.ismop.web.client.dap.levee;

import org.fusesource.restygwt.client.Json;

public class ModeChange {
	@Json(name = "emergency_level")
	private String mode;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
}