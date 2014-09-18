package pl.ismop.web.controllers.maps;

public class Geometry {
	private String type;
	
	public Geometry(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}