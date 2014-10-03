package pl.ismop.web.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "Experiments")
public class Experiment {
	@Id
	@GeneratedValue
	private Long id;
	
	private String nativeId;

	public String getNativeId() {
		return nativeId;
	}

	public void setNativeId(String nativeId) {
		this.nativeId = nativeId;
	}
}