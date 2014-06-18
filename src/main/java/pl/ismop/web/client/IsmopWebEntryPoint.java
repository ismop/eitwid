package pl.ismop.web.client;

import org.fusesource.restygwt.client.Defaults;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.mvp4g.client.Mvp4gModule;

public class IsmopWebEntryPoint implements EntryPoint {
	public static Dictionary properties;

	@Override
	public void onModuleLoad() {
		properties = Dictionary.getDictionary("properties");
		Defaults.setServiceRoot(properties.get("dapEndpoint"));
		
		Mvp4gModule module = (Mvp4gModule)GWT.create(Mvp4gModule.class);
		module.createAndStartModule();
	}
}