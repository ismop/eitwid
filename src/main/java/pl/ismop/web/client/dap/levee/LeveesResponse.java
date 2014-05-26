package pl.ismop.web.client.dap.levee;

import java.util.List;

public class LeveesResponse {
	private List<Levee> levees;

	public List<Levee> getLevees() {
		return levees;
	}

	public void setLevees(List<Levee> levees) {
		this.levees = levees;
	}

	@Override
	public String toString() {
		return "LeveesResponse [levees=" + levees + "]";
	}
}