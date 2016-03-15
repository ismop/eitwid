package pl.ismop.web.client.widgets.realtime.main;

import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.transform;
import static com.google.common.util.concurrent.Futures.transformAsync;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gwt.user.client.Window;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.widgets.realtime.main.IRealTimePanelView.IRealTimePanelPresenter;

@Presenter(view = RealTimePanelView.class, multiple = true)
public class RealTimePanelPresenter extends BasePresenter<IRealTimePanelView, MainEventBus>
		implements IRealTimePanelPresenter {
	private DapController dapController;

	@Inject
	public RealTimePanelPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	public void onRefreshRealTimePanel() {
		ListenableFuture<Void> weatherFuture = updateWeather();
		addCallback(weatherFuture, new FutureCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFailure(Throwable t) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private ListenableFuture<Void> updateWeather() {
		SettableFuture<Void> result = SettableFuture.create();
		ListenableFuture<List<String>> deviceIdsFuture = transform(
				dapController.getDevicesForType("weather_station"), devices ->
					Lists.transform(devices, device -> new String(device.getId())));
		ListenableFuture<List<Parameter>> parametersFuture = transformAsync(deviceIdsFuture,
				deviceIds -> dapController.getParameters(deviceIds));
		ListenableFuture<List<Context>> contextsFuture = dapController.getContext("measurements");
		ListenableFuture<List<List<? extends Object>>> contextsAndParameterIds =
				Futures.allAsList(contextsFuture, parametersFuture);
		ListenableFuture<List<Timeline>> timelinesFuture = transformAsync(contextsAndParameterIds,
				(List<List<? extends Object>> resultList) -> {
					@SuppressWarnings("unchecked")
					List<Context> contexts = (List<Context>) resultList.get(0);
					@SuppressWarnings("unchecked")
					List<Parameter> parameters = (List<Parameter>) resultList.get(1);
					
					if (contexts.size() > 0 && parameters.size() > 0) {
						return dapController.getTimelinesForParameterIds(
								contexts.get(0).getId(), Lists.transform(parameters,
										parameter -> parameter.getId()));
					} else {
						SettableFuture<List<Timeline>> resultFuture = SettableFuture.create();
						resultFuture.set(new ArrayList<>());
						
						return resultFuture;
					}
				});
		ListenableFuture<List<Measurement>> measurementsFuture = transformAsync(timelinesFuture,
				timelines -> dapController.getLastMeasurementsWith24HourMod(
					Lists.transform(timelines, timeline -> timeline.getId()), new Date()));
		addCallback(measurementsFuture, new FutureCallback<List<Measurement>>() {
			@Override
			public void onSuccess(List<Measurement> measurements) {
				Window.alert("Measurements: " + measurements);
				result.set(null);
			}

			@Override
			public void onFailure(Throwable t) {
				Window.alert("Failure: " + t);
				result.set(null);
			}
		});
		
		return result;
	}
}