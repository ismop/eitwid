package pl.ismop.web.client.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.MeasurementsCallback;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter.DataCallback;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter.ZoomDataCallback;

public class TimelineZoomDataCallbackHelper implements ZoomDataCallback {
	private DapController dapController;
	
	private MainEventBus eventBus;

	private ChartPresenter chartPresenter;

	public TimelineZoomDataCallbackHelper(DapController dapController, MainEventBus eventBus,
			ChartPresenter chartPresenter) {
		this.dapController = dapController;
		this.eventBus = eventBus;
		this.chartPresenter = chartPresenter;
	}
	
	@Override
	public void onZoom(Date startDate, Date endDate, List<String> timelineIds,
			final DataCallback callback) {
		dapController.getMeasurementsWithQuantityAndTime(timelineIds, startDate,
				endDate, 1000, new MeasurementsCallback() {
					@Override
					public void onError(ErrorDetails errorDetails) {
						eventBus.showError(errorDetails);
						chartPresenter.setLoadingState(false);
					}
					
					@Override
					public void processMeasurements(List<Measurement> measurements) {
						Map<String, List<Measurement>> measurementMap = new HashMap<>();
						
						for (Measurement measurement : measurements) {
							if (measurementMap.get(
									measurement.getTimelineId()) == null) {
								measurementMap.put(measurement.getTimelineId(),
										new ArrayList<Measurement>());
							}
							
							measurementMap.get(measurement.getTimelineId())
									.add(measurement);
						}
						
						Map<String, Number[][]> data = new HashMap<>();
						
						for (String timelineId : measurementMap.keySet()) {
							if (data.get(timelineId) == null) {
								data.put(timelineId, new Number[measurementMap.get(timelineId).size()][2]);
							}
							
							int index = 0;
							
							for(Measurement measurement : measurementMap.get(timelineId)) {
								data.get(timelineId)[index][0] = measurement.getTimestamp().getTime();
								data.get(timelineId)[index][1] = measurement.getValue();
								index++;
							}
						}
						
						callback.updateData(data);
					}
				});
	}
}