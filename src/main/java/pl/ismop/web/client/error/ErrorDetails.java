package pl.ismop.web.client.error;

public class ErrorDetails {
	private String message;
	
	public ErrorDetails(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}