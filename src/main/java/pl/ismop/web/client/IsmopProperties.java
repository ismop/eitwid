package pl.ismop.web.client;

import com.google.gwt.i18n.client.Constants;

public interface IsmopProperties extends Constants {
	@DefaultStringValue("https://atmo.moc.ismop.edu.pl/api/v1")
	String dapEndpoint();
	
	@DefaultStringValue("asd324de3")
	String dapToken();


	@DefaultStringValue("#f5ea6f")
	String selectionColor();
}