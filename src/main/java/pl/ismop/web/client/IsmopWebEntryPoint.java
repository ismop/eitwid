package pl.ismop.web.client;

import org.fusesource.restygwt.client.Defaults;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.mvp4g.client.Mvp4gModule;

public class IsmopWebEntryPoint implements EntryPoint {
	public static IsmopProperties properties;

	@Override
	public void onModuleLoad() {
		properties = GWT.create(IsmopProperties.class);
		Defaults.setServiceRoot(properties.dapEndpoint());
		
		Mvp4gModule module = (Mvp4gModule)GWT.create(Mvp4gModule.class);
		module.createAndStartModule();
	}
}