package pl.ismop.web.controllers.main;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

public class Registration {
	@NotEmpty
	@Email
	private String email;
	
	@Length(min = 5)
	private String password;
	private String confirmPassword;
	private String secretToken;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getConfirmPassword() {
		return confirmPassword;
	}
	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
	public String getSecretToken() {
		return secretToken;
	}
	public void setSecretToken(String secretToken) {
		this.secretToken = secretToken;
	}
}
