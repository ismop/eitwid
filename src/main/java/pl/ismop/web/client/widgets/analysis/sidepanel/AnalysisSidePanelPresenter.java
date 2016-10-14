package pl.ismop.web.client.widgets.analysis.sidepanel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.IsmopProperties;
import pl.ismop.web.client.IsmopWebEntryPoint;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.threatlevel.Scenario;
import pl.ismop.web.client.dap.threatlevel.ThreatAssessment;
import pl.ismop.web.client.dap.threatlevel.ThreatLevel;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.geojson.MapFeature;
import pl.ismop.web.client.util.WaterHeight;
import pl.ismop.web.client.widgets.analysis.sidepanel.IAnalysisSidePanelView.IAnalysisSidePanelPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.common.refresher.RefresherPresenter;
import pl.ismop.web.client.widgets.common.refresher.RefresherPresenter.Event;
import pl.ismop.web.client.widgets.delegator.MeasurementsCallback;
import pl.ismop.web.client.widgets.delegator.ParametersCallback;

@Presenter(view = AnalysisSidePanelView.class, multiple = true)
public class AnalysisSidePanelPresenter extends BasePresenter<IAnalysisSidePanelView, MainEventBus> implements IAnalysisSidePanelPresenter {
    private final DapController dapController;
    private final IsmopProperties properties;

    private MapPresenter miniMap;
    private ChartPresenter waterWave;

    private Experiment selectedExperiment;
    private AnalysisSidePanelMessages messages;
	private RefresherPresenter refresher;

    @Inject
    public AnalysisSidePanelPresenter(DapController dapController, IsmopProperties properties) {
        this.dapController = dapController;
        this.properties = properties;
    }

    public void init() {
        this.messages = getView().getMessages();
        initExperiments();
    }

    private void initExperiments() {
        dapController.getExperiments(new DapController.ExperimentsCallback() {
            @Override
            public void processExperiments(List<Experiment> loadedExperiments) {
                getView().setExperiments(loadedExperiments);
                for (Experiment loadedExperiment : loadedExperiments) {
                    if (isActiveExperiment(loadedExperiment)) {
                        selectExperiment(loadedExperiment);
                        break;
                    }
                }
            }

            @Override
            public void onError(ErrorDetails errorDetails) {
                eventBus.showError(errorDetails);
            }
        });
    }

    private boolean isActiveExperiment(Experiment experiment) {
    	Date currentDate = new Date();

    	return currentDate.after(experiment.getStart()) && currentDate.before(experiment.getEnd());
    }

    private void initWaterWave() {
        if (waterWave == null) {
        	waterWave = eventBus.addHandler(ChartPresenter.class);
        	waterWave.setHeight(view.getWaterWavePanelHeight());
        	waterWave.initChart();
        	view.setWaterWavePanel(waterWave.getView());
        }
    }

    private void initMinimap() {
        if (miniMap == null) {
            miniMap = eventBus.addHandler(MapPresenter.class);
            view.setMinimap(miniMap.getView());
        }
    }

    @Override
    public void selectExperiment(Experiment selectedExperiment) {
        if (this.selectedExperiment != selectedExperiment) {
            this.selectedExperiment = selectedExperiment;
            getView().selectExperiment(selectedExperiment);
            initRefresher();
            initWaterWave();
            initMinimap();
            loadExperimentWaveShape();
            loadWaterHeight();
            loadExperimentLevee();
            eventBus.experimentChanged(selectedExperiment);
        }
    }

	private void initRefresher() {
		if(isActiveExperiment(selectedExperiment)) {
			if (refresher == null) {
				refresher = eventBus.addHandler(RefresherPresenter.class);
				view.setRefresher(refresher.getView());
				refresher.setEvent(new Event() {
					@Override
					public void refresh() {
						eventBus.refresh();
						refresher.initializeTimer();
					}
				});
				refresher.initializeTimer();
			}
		} else {
			view.clearRefresher();
			if(refresher != null) {
				eventBus.removeHandler(refresher);
				refresher = null;
			}
		}
	}

	@Override
    public void export() {
        Window.open(IsmopWebEntryPoint.properties.get("dapEndpoint")
                + "/experiment_exporter/" + selectedExperiment.getId() +
                "?private_token=" + IsmopWebEntryPoint.properties.get("dapToken"), "_self", null);

    }

    private void loadExperimentWaveShape() {
        if (selectedExperiment != null) {
            waterWave.showLoading(messages.loadingWaterWave());
            dapController.getExperimentTimelines(selectedExperiment.getId(), new DapController.TimelinesCallback() {
                @Override
                public void processTimelines(final List<Timeline> timelines) {
                    if (timelines.size() > 0) {
                        final Map<String, String> parameterIdToTimelineId = new HashMap<>();
                        for (Timeline timeline : timelines) {
                            parameterIdToTimelineId.put(timeline.getParameterId(), timeline.getId());
                        }
                        dapController.getParametersById(parameterIdToTimelineId.keySet(), new ParametersCallback(this) {
                            @Override
                            public void processParameters(final List<Parameter> parameters) {
                                final Map<String, Parameter> timelineIdToParameter = new HashMap<>();
                                for (Parameter parameter : parameters) {
                                    timelineIdToParameter.put(parameterIdToTimelineId.get(parameter.getId()), parameter);
                                }
                                dapController.getAllMeasurements(parameterIdToTimelineId.values(), new MeasurementsCallback(this) {
                                    @Override
                                    public void processMeasurements(List<Measurement> measurements) {
                                        Map<Parameter, List<Measurement>> series = new HashMap<>();
                                        for (Measurement measurement : measurements) {
                                            Parameter parameter = timelineIdToParameter.get(measurement.getTimelineId());
                                            List<Measurement> m = series.get(parameter);
                                            if (m == null) {
                                                m = new ArrayList<>();
                                                series.put(parameter, m);
                                            }
                                            m.add(measurement);
                                        }

                                        showExperimentWaveShape(series);
                                        waterWave.hideLoading();
                                    }
                                });
                            }
                        });
                    } else {
                        waterWave.showLoading(messages.noWaterWave());
                    }
                }

                @Override
                public void onError(ErrorDetails errorDetails) {
                    eventBus.showError(errorDetails);
                }
            });
        }
    }

    private void loadWaterHeight() {
    	waterWave.showLoading(messages.loadingWaterWave());
    	new WaterHeight(dapController).loadAverage(selectedExperiment.getStart(), selectedExperiment.getEnd(), new WaterHeight.WaterHeightCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
				waterWave.hideLoading();
			}

			@Override
			public void success(Stream<ChartSeries> series) {
				series.forEach(s -> {
					Parameter seriesParameter = new Parameter();
					seriesParameter.setId(s.getParameterId());
					waterWave.removeChartSeriesForParameter(seriesParameter);
					waterWave.addChartSeries(s);
				});
				waterWave.hideLoading();
			}
		});
	}

    private void loadExperimentLevee() {
        miniMap.reset(false);
        dapController.getSections(selectedExperiment.getLeveeId() + "", new DapController.SectionsCallback() {
            @Override
            public void processSections(List<Section> sections) {
                selectedExperiment.setSections(sections);
                for (Section section : sections) {
                    miniMap.add(section);
                }
            }

            @Override
            public void onError(ErrorDetails errorDetails) {
                eventBus.showError(errorDetails);
            }
        });
    }

    private void showExperimentWaveShape(Map<Parameter, List<Measurement>> series) {
        waterWave.reset();
        for (Map.Entry<Parameter, List<Measurement>> entry : series.entrySet()) {
        	Parameter parameter = entry.getKey();
            ChartSeries s = new ChartSeries();
            s.setName(parameter.getParameterName());
            s.setUnit(parameter.getMeasurementTypeUnit());
            s.setLabel(parameter.getMeasurementTypeName());
            s.setParameterId(parameter.getId());

            long diff = 0;
            if(entry.getValue().size() > 0) {
            	s.setTimelineId(entry.getValue().get(0).getTimelineId());
                diff = entry.getValue().get(0).getTimestamp().getTime() - selectedExperiment.getStart().getTime();
            }

            int size = entry.getValue().size();
            Number[][] values = new Number[size][2];
            for(int i = 0; i < size; i++) {
            	Measurement measurement = entry.getValue().get(i);
            	long time = measurement.getTimestamp().getTime() - diff;
            	if (time > selectedExperiment.getEnd().getTime()) {
            		GWT.log("Warning experiment water wave is longer then experiment");
            		break;
            	}
            	values[i][0] = time;
    			values[i][1] = measurement.getValue();
            }
            s.setValues(values);
            waterWave.addChartSeries(s);
        }
    }

    public void onDateChanged(Date selectedDate) {
    	selectDateOnWaterChart(selectedDate);
    	loadThreatLevels(selectedDate);
    }

	private void selectDateOnWaterChart(Date selectedDate) {
    	if (waterWave != null) {
        	waterWave.selectDate(selectedDate, properties.selectionColor());
        }
    }

    public void onRefresh() {
    	loadWaterHeight();
    }

    private void loadThreatLevels(Date selectedDate) {
		GWT.log("Loading threat levels for selected date: " + selectedDate);
		ListenableFuture<List<ThreatLevel>> threatLevels = dapController.getThreatLevels(1, selectedExperiment.getStart(), selectedDate, "finished");
		Futures.addCallback(threatLevels, new FutureCallback<List<ThreatLevel>>() {

			@Override
			public void onFailure(Throwable error) {
				eventBus.showError(new ErrorDetails(error.getMessage()));
			}

			@Override
			public void onSuccess(List<ThreatLevel> threatLevels) {
				eventBus.threatLevelsChanged(threatLevels);
				showThreatLevelsOnMinimap(threatLevels);
			}

		});
	}

    private void showThreatLevelsOnMinimap(List<ThreatLevel> threatLevels) {
    	miniMap.clearStrokeColors();
    	Map<String, Integer> alertLevels = new HashMap<>();
    	if (threatLevels != null) {
    		threatLevels.stream().forEach(tl -> {
    			if(tl.getThreatAssessments() != null && tl.getThreatAssessments().size() > 0) {
    				ThreatAssessment ta = tl.getThreatAssessments().get(0);
    				if (ta != null && ta.getScenarios() != null && ta.getScenarios().size() > 0) {
    					Scenario mostLikelyScenario = ta.getScenarios().get(0);
    					Integer current = alertLevels.get(tl.getSectionId());
    					if(current == null || current < mostLikelyScenario.getThreatLevel()) {
    						alertLevels.put(tl.getSectionId(), mostLikelyScenario.getThreatLevel());
    					}
    				}
    			}
    		});
    	}

    	alertLevels.entrySet().stream().forEach(ks -> {
    		Section section = getSection(ks.getKey());
    		if(section != null) {
    			miniMap.setFeatureStrokeColor(section, getScenarioColor(ks.getValue()));
    			miniMap.rm(section);
    			miniMap.add(section);
    		}
    	});
	}

    private String getScenarioColor(Integer threatLevel) {
		String color;
		switch(threatLevel) {
			case 0:  color = "#3c763d";
					 break;
			case 1:  color = "#8a6d3b";
					 break;
			case 2:  color = "#a94442";
					 break;
			default: color = null;
					 break;
		}
		return color;
	}

    private Section getSection(String sectionId) {
    	if (selectedExperiment.getSections() != null) {
    		for (Section section : selectedExperiment.getSections()) {
				if (section.getId().equals(sectionId)) {
					return section;
				}
			}
    	}
    	return null;
    }

	public void onAdd(MapFeature mapFeature) {
        miniMap.add(mapFeature);
    }

    public void onRm(MapFeature mapFeature) {
        miniMap.rm(mapFeature);
    }

    public void onSelect(MapFeature mapFeature) {
        miniMap.select(mapFeature);
    }

    public void onUnselect(MapFeature mapFeature) {
        miniMap.unselect(mapFeature);
    }

    public void onHighlight(MapFeature mapFeature) {
        miniMap.highlight(mapFeature);
    }

    public void onUnhighlight(MapFeature mapFeature) {
        miniMap.unhighlight(mapFeature);
    }

    public void onClearMinimap() {
        miniMap.reset(true);
    }
}
