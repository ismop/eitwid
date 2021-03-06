package pl.ismop.web.client.widgets.analysis.chart;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.IsmopProperties;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.chart.wizard.ChartWizardPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

@Presenter(view = ChartView.class, multiple = true)
public class ChartPresenter extends BasePresenter<IChartView, MainEventBus>
        implements IPanelContent<IChartView, MainEventBus> {
    private final DapController dapController;

    private final IsmopProperties properties;

    private Experiment selectedExperiment;

    private ISelectionManager selectionManager;

    private ChartWizardPresenter wizard;

    private pl.ismop.web.client.widgets.common.chart.ChartPresenter chartPresenter;

    private Map<String, Device> idToDevice = new HashMap<>();

    private List<Timeline> timelines;

    private boolean changeTrends;

    private Map<String, String> colors = new HashMap<>();

    private Date start;
    private Date end;

    @Inject
    public ChartPresenter(DapController dapController, IsmopProperties properties) {
        this.dapController = dapController;
        this.properties = properties;
    }

    @Override
    public void setSelectedExperiment(Experiment experiment) {
        selectedExperiment = experiment;
        updateChartInterval();
    }

    private void updateChartInterval() {
    	if ((start == null
    			|| end == null
    			|| !start.equals(selectedExperiment.getStart())
    			|| !end.equals(selectedExperiment.getEnd()))
    			&& !getChart().isZoomed()) {
    		GWT.log("Updating chart intervals: " + selectedExperiment.getStart()
    				+ " - " + selectedExperiment.getEnd());
			getChart().setInterval(selectedExperiment.getStart(), selectedExperiment.getEnd());
    	}
    }

    @Override
    public void setSelectedDate(Date date) {
        onDateChanged(date);
    }

    @Override
    public void edit() {
        wizard.show(selectedExperiment, new ChartWizardPresenter.ShowResult() {
            @Override
            public void ok(List<Timeline> selectedTimelines, boolean changeTrends) {
                setChangeTrends(changeTrends);
                setTimelines(selectedTimelines);
            }
        });
    }

    @Override
    public void setSelectionManager(ISelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    @Override
    public void destroy() {
        eventBus.removeHandler(wizard);
    }

    interface ChartPointsCallback {
        void processChartPoints(List<ChartSeries> series);
    }

    class ChartPointMeasurementsCallback implements DapController.MeasurementsCallback {
        private final Map<String, Timeline> idToTimeline;
        private final ChartPointsCallback callback;
        private boolean changeTrends;

        public ChartPointMeasurementsCallback(Map<String, Timeline> idToTimeline, boolean changeTrends,
        		ChartPointsCallback callback) {
            this.idToTimeline = idToTimeline;
            this.changeTrends = changeTrends;
            this.callback = callback;
        }

        @Override
        public void processMeasurements(List<Measurement> measurements) {
            List<ChartSeries> series = map(measurements);
            callback.processChartPoints(changeTrends ? toChangeTrends(series) : series);
        }

        private List<ChartSeries> toChangeTrends(List<ChartSeries> series) {
            GWT.log("to change trends");
            for (ChartSeries chartSeries : series) {
                if (chartSeries.getValues() != null && chartSeries.getValues().length > 0) {
                    double first = chartSeries.getValues()[0][1].doubleValue();
                    for (Number[] point : chartSeries.getValues()) {
                        point[1] = point[1].doubleValue() - first;
                    }
                }
            }
            return series;
        }

        private List<ChartSeries> map(List<Measurement> measurements) {
            List<ChartSeries> series = new ArrayList<>();
            for (Map.Entry<Timeline, List<Measurement>> timelineListEntry : group(measurements)) {
                ChartSeries s = createSeries(timelineListEntry.getKey());
                s.setValues(getValues(timelineListEntry.getValue(), getDiff(timelineListEntry)));

                series.add(s);
            }

            return series;
        }

        private Set<Map.Entry<Timeline, List<Measurement>>> group(List<Measurement> measurements) {
            Map<Timeline, List<Measurement>> timelineToMeasurements = new HashMap<>();
            for (Measurement measurement : measurements) {
                Timeline timeline = idToTimeline.get(measurement.getTimelineId());
                List<Measurement> timelineMeasurements = timelineToMeasurements.get(timeline);
                if (timelineMeasurements == null) {
                    timelineMeasurements = new ArrayList<>();
                    timelineToMeasurements.put(timeline, timelineMeasurements);
                }

                timelineMeasurements.add(measurement);
            }

            return timelineToMeasurements.entrySet();
        }

        private Number[][] getValues(List<Measurement> measurements, long diff) {
            Number[][] values = new Number[measurements.size()][2];
            for (int i = 0; i < measurements.size(); i++) {
                Measurement m = measurements.get(i);
                values[i][0] = m.getTimestamp().getTime() - diff;
                values[i][1] = m.getValue();
            }
            return values;
        }

        private long getDiff(Map.Entry<Timeline, List<Measurement>> timelineListEntry) {
            if (timelineListEntry.getKey().getScenarioId() != null) {
                Measurement first = timelineListEntry.getValue().get(0);
                return first.getTimestamp().getTime() - selectedExperiment.getStart().getTime();
            }
            return 0;
        }

        private ChartSeries createSeries(Timeline timeline) {
            Parameter parameter = timeline.getParameter();

            ChartSeries series = new ChartSeries();
            series.setDeviceId(parameter.getDeviceId());
            series.setParameterId(parameter.getId());
            series.setTimelineId(timeline.getId());

			series.setName(getChartName(timeline));
            series.setLabel(parameter.getMeasurementTypeName());
            series.setUnit(parameter.getMeasurementTypeUnit());

            return series;
        }

        private String getChartName(Timeline timeline) {
        	Parameter parameter = timeline.getParameter();
        	String name = parameter.getDevice().getCustomId() + ": "
        			+ parameter.getParameterName() + " ("
        			+ parameter.getMeasurementTypeName() + ")";

        	if (timeline.getScenario() != null) {
        		name = name + " - " + view.getMessages().scenario() + " "
        				+  timeline.getScenario().getName();
        	}


        	return name;
        }

        @Override
        public void onError(ErrorDetails errorDetails) {
            eventBus.showError(errorDetails);
        }
    }

    public void setTimelines(List<Timeline> timelines) {
        getChart().setLoadingState(true);
        this.timelines = timelines;

        final Map<String, Timeline> idToRealTimeline = new HashMap<>();
        final Map<String, Timeline> idToScenarioTimeline = new HashMap<>();
        selectionManager.clear();
        idToDevice = new HashMap<>();
        for (Timeline timeline : timelines) {
            GWT.log("Showing timeline with " + timeline.getId() + "id belonging to " +
                    timeline.getContextId() + " and scenario " + timeline.getScenarioId());
            if (timeline.getScenarioId() != null) {
                idToScenarioTimeline.put(timeline.getId(), timeline);
            } else {
                idToRealTimeline.put(timeline.getId(), timeline);
            }

            Device d = timeline.getParameter().getDevice();
            selectionManager.add(d);
            idToDevice.put(d.getId(), d);
        }


        if (idToRealTimeline.size() > 0) {
            GWT.log("Loading real measurements");
            dapController.getMeasurements(idToRealTimeline.keySet(), selectedExperiment.getStart(),
                    selectedExperiment.getEnd(), new ChartPointMeasurementsCallback(
                    		idToRealTimeline, changeTrends, new ChartPointsCallback() {
                        @Override
                        public void processChartPoints(
                        		final List<ChartSeries> realTimelineToMeasurements) {
                            loadScenarioTimelines(realTimelineToMeasurements, idToScenarioTimeline);
                        }
                    }));
        } else {
            loadScenarioTimelines(new ArrayList<ChartSeries>(), idToScenarioTimeline);
        }
    }

    private pl.ismop.web.client.widgets.common.chart.ChartPresenter getChart() {
        if(chartPresenter == null) {
            chartPresenter = eventBus.addHandler(pl.ismop.web.client.widgets.common.chart.ChartPresenter.class);
            chartPresenter.setXAxisLabelsFormatter(new AxisLabelsFormatter() {
				@Override
				public String format(AxisLabelsData axisLabelsData) {
					return getXLabelDefaultFormat(axisLabelsData.getNativeData()) +
							" [" + getExperimentHours(axisLabelsData.getValueAsDouble()) +" h]";
				}
			});
            chartPresenter.setDeviceSelectHandler(new pl.ismop.web.client.widgets.common.chart.ChartPresenter.DeviceSelectHandler() {
                @Override
                public void select(ChartSeries series) {
                    selectionManager.select(getDevice(series));
                }

                @Override
                public void unselect(ChartSeries series) {
                    selectionManager.unselect(getDevice(series));
                }

                private Device getDevice(ChartSeries series) {
                    return idToDevice.get(series.getDeviceId());
                }
            });

            getView().setChart(chartPresenter.getView());
        }

        return chartPresenter;
    }

    private String getExperimentHours(double time) {
    	int interval = (int)(time - selectedExperiment.getStart().getTime()) / (1000 * 60 * 60);
    	return "" + interval;
    }

    private native String getXLabelDefaultFormat(JavaScriptObject nativeData) /*-{
		return $wnd.Highcharts.dateFormat(nativeData.dateTimeLabelFormat, nativeData.value);
	}-*/;

    public void onDateChanged(Date selectedDate) {
        getChart().selectDate(selectedDate, properties.selectionColor());
    }

    public void onRefresh() {
    	GWT.log("Refresh chart all chosen series");
    	updateChartInterval();
    	setTimelines(timelines);
    }

    public void setWizard(ChartWizardPresenter wizard) {
        this.wizard = wizard;
    }

	private void loadScenarioTimelines(final List<ChartSeries> realTimelineToMeasurements,
	                                   final Map<String, Timeline> idToScenarioTimeline) {
	    if (idToScenarioTimeline.size() > 0) {
	        GWT.log("Loading scenarios measurements");
	        dapController.getAllMeasurements(idToScenarioTimeline.keySet(),
	                new ChartPointMeasurementsCallback(idToScenarioTimeline, changeTrends, new ChartPointsCallback() {
	                    @Override
	                    public void processChartPoints(List<ChartSeries> scenarioTimelineToMeasurements) {
	                        realTimelineToMeasurements.addAll(scenarioTimelineToMeasurements);
                            setSeries(realTimelineToMeasurements);
	                    }
	                }));
	    } else {
            setSeries(realTimelineToMeasurements);
	    }
	}

    private void setSeries(List<ChartSeries> chartSeries) {
        getChart().setLoadingState(false);
        Set<String> visibleSeriesNames = chartPresenter.getVisibleChartSeriesNames();
        boolean hasSeries = chartPresenter.getSeries().size() > 0;

        chartPresenter.reset();
        for (ChartSeries s : chartSeries) {
            String color = colors.get(s.getTimelineId());
            if (color != null) {
                s.setOverrideColor(color);
            }
            s.setVisible(!hasSeries || visibleSeriesNames.contains(s.getName()));
            Series series = chartPresenter.addChartSeries(s, false);
            colors.put(s.getTimelineId(), getSeriesColor(series.getNativeSeries()));
        }
        chartPresenter.redraw();
    }

    private native String getSeriesColor(JavaScriptObject nativeSeries) /*-{
        return nativeSeries.color;
    }-*/;

    public void setChangeTrends(boolean changeTrends) {
        this.changeTrends = changeTrends;
    }
}
