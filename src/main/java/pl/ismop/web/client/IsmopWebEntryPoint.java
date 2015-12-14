package pl.ismop.web.client;

import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.ServiceRoots;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.Mvp4gModule;

public class IsmopWebEntryPoint implements EntryPoint {
	public static Dictionary properties;

	@Override
	public void onModuleLoad() {
		properties = Dictionary.getDictionary("properties");
		ServiceRoots.add("dap", properties.get("dapEndpoint"));
		ServiceRoots.add("hypgen", properties.get("hypgenEndpoint"));
		Defaults.ignoreJsonNulls();
		
		Mvp4gModule module = (Mvp4gModule)GWT.create(Mvp4gModule.class);
		module.createAndStartModule();
		RootLayoutPanel.get().add((Widget) module.getStartView());
	}
}