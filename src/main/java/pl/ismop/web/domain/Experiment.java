package pl.ismop.web.domain;

import pl.ismop.web.client.dap.section.Section;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@Entity(name = "Experiments")
public class Experiment {
	@Id
	@GeneratedValue
	private Long id;
	
	private String nativeId;
	private List<Section> sections;

	public String getNativeId() {
		return nativeId;
	}

	public void setNativeId(String nativeId) {
		this.nativeId = nativeId;
	}
}