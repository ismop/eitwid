package pl.ismop.web.client;

import java.util.Date;
import java.util.List;

import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.options.DialogOptions;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.Dictionary;

import javaslang.concurrent.Future;
import javaslang.concurrent.Promise;

public class IsmopWebEntryPoint implements EntryPoint {
	private IsmopConverter ismopConverter;

	public static Dictionary properties;

	public IsmopWebEntryPoint() {
		ismopConverter = new IsmopConverter();
	}

	@Override
	public void onModuleLoad() {
//		properties = Dictionary.getDictionary("properties");
//		ServiceRoots.add("dap", properties.get("dapEndpoint"));
//		ServiceRoots.add("hypgen", properties.get("hypgenEndpoint"));
//		Defaults.ignoreJsonNulls();
//
//		GlobalMessages globalMessages = GWT.create(GlobalMessages.class);
//		globalInitialization(globalMessages);
//
//		Mvp4gModule module = (Mvp4gModule)GWT.create(Mvp4gModule.class);
//		module.createAndStartModule();
//		RootLayoutPanel.get().add((Widget) module.getStartView());
		testFuture();
	}

	private void testFuture() {
		Future.sequence(javaslang.collection.List.of(
				get("http://localhost:8080"),
				get("http://localhost:8080")))
		.onComplete(results -> {
			if (results.isSuccess()) {
				results.get().zipWithIndex((result, index) -> {
					GWT.log("Result " + index + ": " + result);

					return null;
				});
			}
		});


//		get("http://localhost:8080")
//		.flatMap(value -> {
//			GWT.log("Got 1: " + value);
//
//			return get("http://localhost:8080");
//		})
//		.onComplete(value -> GWT.log("Got 2: " + value))
//		.onFailure(exception -> GWT.log("Error: " + exception.getMessage()));
	}

	private Future<String> get(String url) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, url);
		Promise<String> promise = Promise.make();
		rb.setCallback(new RequestCallback() {

			@Override
			public void onResponseReceived(Request request, Response response) {
//				promise.success(response.getText());
				promise.failure(new IllegalArgumentException("Yo!"));
			}

			@Override
			public void onError(Request request, Throwable exception) {
				promise.failure(exception);
			}
		});
		try {
			rb.send();
		} catch (RequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return promise.future();
	}

	//this is the place to do global initialization once
	private void globalInitialization(GlobalMessages globalMessages) {
		//configuring highcharts exporting capabilities
		configureHighchartsGlobalSettings(globalMessages,
				toStringArray(ismopConverter.shortMonths()),
				toStringArray(ismopConverter.months()),
				toStringArray(ismopConverter.days()));

		//Bootbox setup
		DialogOptions dialogOptions = DialogOptions.newOptions("");
		dialogOptions.setCloseButton(false);
		Bootbox.setDefaults(dialogOptions);
	}

	private String getCurrentDate() {
		return ismopConverter.formatForFileSafeName(new Date());
	}

	private JavaScriptObject toStringArray(List<String> values) {
		JsArrayString result = (JsArrayString) JsArrayString.createArray();

		for (String value : values) {
			result.push(value);
		}

		return result;
	}

	private native void configureHighchartsGlobalSettings(GlobalMessages messages,
			JavaScriptObject shortMonths, JavaScriptObject months, JavaScriptObject days) /*-{
		var object = this;
		$wnd.Highcharts.setOptions({
			colors: ['#90ed7d', '#f7a35c', '#8085e9', '#f15c80',
   						'#e4d354', '#2b908f', '#f45b5b', '#91e8e1', '#7cb5ec', '#434348'],
			lang: {
				resetZoom: messages.@pl.ismop.web.client.GlobalMessages::resetZoomLabel()(),
				resetZoomTitle:
						messages.@pl.ismop.web.client.GlobalMessages::resetZoomTitleLabel()(),
				months: months,
				shortMonths: shortMonths,
				weekdays: days
			},
			global: {
				useUTC: false
			},
			exporting: {
				buttons: {
					contextButton: {
						menuItems: [{
							text: messages.@pl.ismop.web.client.GlobalMessages::exportPngLabel()(),
							onclick: function() {
								this.exportChart({
									filename: "ismop-data-export-"
										+ object.@pl.ismop.web.client.IsmopWebEntryPoint::getCurrentDate()(),
									type: "image/png"
								})
							}
						}, {
							text: messages.@pl.ismop.web.client.GlobalMessages::exportPdfLabel()(),
							onclick: function() {
								this.exportChart({
									filename: "ismop-data-export-"
										+ object.@pl.ismop.web.client.IsmopWebEntryPoint::getCurrentDate()(),
									type: "application/pdf"
								})
							}
						}, {
							text: messages.@pl.ismop.web.client.GlobalMessages::exportSvgLabel()(),
							onclick: function() {
								this.exportChart({
									filename: "ismop-data-export-"
										+ object.@pl.ismop.web.client.IsmopWebEntryPoint::getCurrentDate()(),
									type: "image/svg+xml"
								})
							}
						}]
					}
				}
			}
		});
	}-*/;

	private native JavaScriptObject toStringArray(String values) /*-{
		return values.split(",");
	}-*/;;
}
