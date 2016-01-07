package pl.ismop.web.client;

import java.util.Date;

import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.ServiceRoots;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
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
		
		GlobalMessages globalMessages = GWT.create(GlobalMessages.class);
		globalInitialization(globalMessages);
		
		Mvp4gModule module = (Mvp4gModule)GWT.create(Mvp4gModule.class);
		module.createAndStartModule();
		RootLayoutPanel.get().add((Widget) module.getStartView());
	}

	//this is the place to do global initialization once
	private void globalInitialization(GlobalMessages globalMessages) {
		//configuring highcharts exporting capabilities
		configureHighchartsGlobalSettings(globalMessages.exportPngLabel(), globalMessages.exportPdfLabel(), globalMessages.exportSvgLabel());
		
		//Bootbox setup
		Bootbox.createDefaults().setCloseButton(false).setDefaults();
	}
	
	private String getCurrentDate() {
		DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd_HH:mm:ss");
		
		return format.format(new Date());
	}

	private native void configureHighchartsGlobalSettings(String pngLabel, String pdfLabel, String svgLabel) /*-{
		var object = this;
		$wnd.Highcharts.setOptions({
			exporting: {
				buttons: {
					contextButton: {
						menuItems: [{
							text: pngLabel,
							onclick: function() {
								this.exportChart({
									filename: "ismop-data-export-" + object.@pl.ismop.web.client.IsmopWebEntryPoint::getCurrentDate()(),
									type: "image/png"
								})
							}
						}, {
							text: pdfLabel,
							onclick: function() {
								this.exportChart({
									filename: "ismop-data-export-" + object.@pl.ismop.web.client.IsmopWebEntryPoint::getCurrentDate()(),
									type: "application/pdf"
								})
							}
						}, {
							text: svgLabel,
							onclick: function() {
								this.exportChart({
									filename: "ismop-data-export-" + object.@pl.ismop.web.client.IsmopWebEntryPoint::getCurrentDate()(),
									type: "image/svg+xml"
								})
							}
						}]
					}
				}
			}
		});
	}-*/;
}