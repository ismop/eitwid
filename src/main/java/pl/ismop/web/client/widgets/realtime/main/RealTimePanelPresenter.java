package pl.ismop.web.client.widgets.realtime.main;

import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.transform;
import static com.google.common.util.concurrent.Futures.transformAsync;
import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gwt.i18n.client.NumberFormat;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.IsmopConverter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.realtime.main.IRealTimePanelView.IRealTimePanelPresenter;

@Presenter(view = RealTimePanelView.class, multiple = true)
public class RealTimePanelPresenter extends BasePresenter<IRealTimePanelView, MainEventBus>
		implements IRealTimePanelPresenter {
	private DapController dapController;
	
	private Map<String, Device> weatherDevices;
	
	private List<Parameter> weatherParameters;
	
	private List<Measurement> weatherMeasurements;

	private String currentWeatherDeviceId;

	private IsmopConverter ismopConverter;

	@Inject
	public RealTimePanelPresenter(DapController dapController, IsmopConverter ismopConverter) {
		this.dapController = dapController;
		this.ismopConverter = ismopConverter;
		weatherDevices = new HashMap<>();
	}
	
	public void init() {
		onRefreshRealTimePanel();
	}
	
	public void onRefreshRealTimePanel() {
		ListenableFuture<Void> weatherFuture = updateWeather();
		addCallback(weatherFuture, new FutureCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				eventBus.realDataContentLoaded();
			}

			@Override
			public void onFailure(Throwable t) {
				eventBus.showError(new ErrorDetails(t.getMessage()));
			}
		});
	}

	@Override
	public void onWeatherSourceChange() {
		Iterator<String> weatherDeviceIdIterator = weatherDevices.keySet().iterator();
		String weatherDeviceId = weatherDeviceIdIterator.next();
		
		while (weatherDeviceIdIterator.hasNext()
				&& weatherDeviceId.equals(currentWeatherDeviceId)) {
			weatherDeviceId = weatherDeviceIdIterator.next();
		}
		
		currentWeatherDeviceId = weatherDeviceId;
		renderWeatherData();
	}

	private ListenableFuture<Void> updateWeather() {
		weatherDevices.clear();
		
		SettableFuture<Void> result = SettableFuture.create();
		ListenableFuture<List<String>> deviceIdsFuture = transform(
				dapController.getDevicesForType("weather_station"), devices ->
					Lists.transform(devices, device -> {
						weatherDevices.put(device.getId(), device);
						
						return device.getId();
					}));
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
					weatherParameters = parameters;
					
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
				weatherMeasurements = measurements;
				renderWeatherData();
				result.set(null);
			}

			@Override
			public void onFailure(Throwable t) {
				result.set(null);
			}
		});
		
		return result;
	}

	private void renderWeatherData() {
		if (currentWeatherDeviceId == null) {
			List<String> sortedWeatherDeviceIds = new ArrayList<>(weatherDevices.keySet());
			sort(sortedWeatherDeviceIds, (d1, d2) -> weatherDevices.get(d1).getCustomId()
					.compareTo(weatherDevices.get(d2).getCustomId()));
			currentWeatherDeviceId = sortedWeatherDeviceIds.get(0);
		}
		
		view.setWeatherSectionTitle(weatherDevices.get(currentWeatherDeviceId).getCustomId());
		
		ListMultimap<Parameter, Measurement> readings = ArrayListMultimap.create();
		
		for (Parameter parameter : weatherParameters) {
			for (Measurement measurement : weatherMeasurements) {
				if (parameter.getTimelineIds().contains(measurement.getTimelineId())) {
					readings.put(parameter, measurement);
				}
			}
		}
		
		List<Parameter> sortedParameters = new ArrayList<>(readings.keys());
		
		for (Iterator<Parameter> i = sortedParameters.iterator(); i.hasNext();) {
			if (!i.next().getDeviceId().equals(currentWeatherDeviceId)) {
				i.remove();
			}
		}
		
		sort(sortedParameters, (p1, p2) -> p1.getMeasurementTypeName()
				.compareTo(p2.getMeasurementTypeName()));
		
		for (int i = 0; i < sortedParameters.size(); i++) {
			view.setWeatherParameter(i, sortedParameters.get(i).getMeasurementTypeName(),
					"" + NumberFormat.getFormat("0.00").format(
							readings.get(sortedParameters.get(i)).get(0).getValue())
					+ " " + sortedParameters.get(i).getMeasurementTypeUnit(),
					ismopConverter.format(readings.get(sortedParameters.get(i)).get(0)
							.getTimestamp()));
			
		}
	}
}