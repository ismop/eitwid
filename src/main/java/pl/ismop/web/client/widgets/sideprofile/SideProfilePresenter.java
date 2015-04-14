package pl.ismop.web.client.widgets.sideprofile;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.MeasurementsCallback;
import pl.ismop.web.client.dap.DapController.SensorCallback;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.widgets.profile.IProfileView.ISideProfilePresenter;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = SideProfileView.class, multiple = true)
public class SideProfilePresenter extends BasePresenter<ISideProfileView, MainEventBus> implements ISideProfilePresenter {
	private DapController dapController;
	private String selectedSensorId;

	@Inject
	public SideProfilePresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void setProfileNameAndSensors(String profileName, List<Sensor> sensors) {
		List<String> sensorIds = new ArrayList<>();
		
		for(Sensor sensor : sensors) {
			sensorIds.add(sensor.getId());
		}
		
		view.clearSensors();
		view.setScene(profileName, sensorIds);
	}

	public void onSensorSelected(final String sensorId, boolean selected) {
		if(selected) {
			selectedSensorId = sensorId;
			dapController.getSensor(sensorId, new SensorCallback() {
				@Override
				public void onError(int code, String message) {
					Window.alert(message);
				}
				
				@Override
				public void processSensor(final Sensor sensor) {
					dapController.getMeasurements(sensorId, new MeasurementsCallback() {
						@Override
						public void onError(int code, String message) {
							Window.alert(message);
						}
						
						@Override
						public void processMeasurements(List<Measurement> measurements) {
							if(selectedSensorId != null && selectedSensorId.equals(sensorId)) {
								if(measurements != null && measurements.size() > 0) {
									sort(measurements, new Comparator<Measurement>() {
										@Override
										public int compare(Measurement m1, Measurement m2) {
											Date d1 = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).parse(m1.getTimestamp());
											Date d2 = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).parse(m2.getTimestamp());
											
											if(d1.after(d2)) {
												return -1;
											} else {
												return 1;
											}
										}
									});
									view.showMeasurement("" + measurements.get(0).getValue() + " " + sensor.getUnit());
								} else {
									view.showMeasurement(view.getNoMeasurementLabel());
								}
							}
						}
					});
				}
			});
		} else {
			view.removeMeasurement();
			selectedSensorId = null;
		}
	}
}