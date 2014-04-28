package pl.ismop.web.client.dap.levee;

import org.fusesource.restygwt.client.Json;

public class ModeChangeRequest {
	@Json(name = "levee")
	private ModeChange modeChange;

	public ModeChange getModeChange() {
		return modeChange;
	}

	public void setModeChange(ModeChange modeChange) {
		this.modeChange = modeChange;
	}
}