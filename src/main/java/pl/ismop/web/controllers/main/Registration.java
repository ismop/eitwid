package pl.ismop.web.controllers.main;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;


public class Registration extends Passwords {
	@NotEmpty
	@Email
	private String email;
	private String secretToken;
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getSecretToken() {
		return secretToken;
	}
	
	public void setSecretToken(String secretToken) {
		this.secretToken = secretToken;
	}
}