package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.core.client.GWT;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorCallback;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.delegator.ErrorCallbackDelegator;
import pl.ismop.web.client.widgets.delegator.MeasurementsCallback;
import pl.ismop.web.client.widgets.delegator.ParametersCallback;
import pl.ismop.web.client.widgets.delegator.TimelinesCallback;

import java.util.*;

public class DataFetcher implements IDataFetcher {
    private abstract class DeviceAggregationsCallback extends ErrorCallbackDelegator implements DapController.DeviceAggregatesCallback {
        public DeviceAggregationsCallback(ErrorCallback callback) {
            super(callback);
        }
    }

    private abstract class DevicesCallback extends ErrorCallbackDelegator implements DapController.DevicesCallback {
        public DevicesCallback(ErrorCallback errorCallback) {
            super(errorCallback);
        }
    }

    private abstract class SectionsCallback extends ErrorCallbackDelegator implements DapController.SectionsCallback {
        public SectionsCallback(ErrorCallback errorCallback) {
            super(errorCallback);
        }
    }

    private final Levee levee;
    private final DapController dapController;
    private final String contextId;

    private Map<String, Section> idToSections = new HashMap<>();
    private Map<String, DeviceAggregate> idToDeviceAggregation = new HashMap<>();
    private Map<String, Device> idToDevice = new HashMap<>();
    private Map<String, Parameter> idToPrameter = new HashMap<>();
    private BiMap<String, Device> timelineIdToDevice;
    private Set<Device> heatingDevices = new HashSet<>();
    private boolean initialized = false;
    private String yAxisTitle;
    private Date earliestMeasurementTime;

    public DataFetcher(DapController dapController, Levee levee) {
        this.dapController = dapController;
        this.levee = levee;
        this.contextId = "1";
    }

    @Override
    public void initialize(InitializeCallback callback) {
        if (initialized) {
            callback.ready();
        } else {
            performInitialization(callback);
        }
    }

    public void performInitialization(final InitializeCallback callback) {
        GWT.log("Loading device aggregations");
        dapController.getDeviceAggregationForType("fiber", levee.getId(), new DeviceAggregationsCallback(callback) {
            @Override
            public void processDeviceAggregations(List<DeviceAggregate> deviceAggreagations) {
                GWT.log(deviceAggreagations.size() + " devise aggregation loaded, loading devices");
                List<String> deviceAggregationIds = new ArrayList<>();
				for(DeviceAggregate deviceAggregation : deviceAggreagations) {
					deviceAggregationIds.add(deviceAggregation.getId());
                    idToDeviceAggregation.put(deviceAggregation.getId(), deviceAggregation);
				}
				
                dapController.getDevicesRecursivelyForAggregates(deviceAggregationIds, new DevicesCallback(callback) {
                    @Override
                    public void processDevices(List<Device> devices) {
                        final List<String> ids = new ArrayList<>();
                        Set<String> sectionIds = new HashSet<>();
                        for (Device d : devices) {
                            ids.add(d.getId());
                            sectionIds.add(d.getSectionId());

                            idToDevice.put(d.getId(), d);

                            if(!"fiber_optic_node".equals(d.getDeviceType())) {
                                heatingDevices.add(d);
                            }
                        }

                        GWT.log(devices.size() + " devices loaded, loading sections");
                        dapController.getSections(Arrays.asList(sectionIds.toArray(new String[0])), new SectionsCallback(callback) {
                            @Override
                            public void processSections(List<Section> sections) {
                                for (Section s : sections) {
                                    idToSections.put(s.getId(), s);
                                }

                                GWT.log(sections.size() + " sections loaded, loading parameters");
                                dapController.getParameters(ids, new ParametersCallback(callback) {
                                    @Override
                                    public void processParameters(List<Parameter> parameters) {
                                        final Map<String, Device> parameterIdToDevice = new HashMap<String, Device>();
                                        for (Parameter p : parameters) {
                                            idToPrameter.put(p.getId(), p);
                                            parameterIdToDevice.put(p.getId(), idToDevice.get(p.getDeviceId()));
                                        }
                                        setXAxisTitle(parameters.get(0));

                                        GWT.log(parameters.size() + " parameters loaded, loading timelines");
                                        dapController.getTimelinesForParameterIds(contextId, idToPrameter.keySet(), new TimelinesCallback(callback) {
                                            @Override
                                            public void processTimelines(List<Timeline> timelines) {
                                                GWT.log(timelines.size() + " timelines loaded");
                                                initialized = true;

                                                timelineIdToDevice = HashBiMap.create(timelines.size());
                                                for (Timeline t : timelines) {
                                                    Device device = parameterIdToDevice.get(t.getParameterId());
                                                    timelineIdToDevice.put(t.getId(), device);
                                                }
                                                calculateOldestMeasurement(timelines);

                                                callback.ready();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void calculateOldestMeasurement(List<Timeline> timelines) {
        earliestMeasurementTime = new Date();
        for (Timeline timeline : timelines) {
            if (timeline.getEarliestMeasurementTimestamp() != null &&
                    earliestMeasurementTime.after(timeline.getEarliestMeasurementTimestamp())) {
                earliestMeasurementTime = timeline.getEarliestMeasurementTimestamp();
            }
        }
        GWT.log("Ealier measurement timestamp: " + earliestMeasurementTime);
    }

    public Date getEarliestMeasurementTime() {
        return earliestMeasurementTime;
    }

    private void setXAxisTitle(Parameter parameter) {
        yAxisTitle = parameter.getMeasurementTypeName() + " [" + parameter.getMeasurementTypeUnit() + "]";
    }

    @Override
    public String getXAxisTitle() {
        return yAxisTitle;
    }

    @Override
    public void getSeries(Date selectedDate, final SeriesCallback callback) {
        dapController.getLastMeasurementsWith24HourMod(new ArrayList<>(timelineIdToDevice.keySet()),
                selectedDate, new MeasurementsCallback(callback) {
            @Override
            public void processMeasurements(List<Measurement> measurements) {
                GWT.log("number of loaded measurements: " + measurements.size());
                if (measurements.size() > 0){
                    callback.series(getSeries(measurements));
                } else {
                    callback.noData();
                }
            }
        });
    }

    private Map<DeviceAggregate, List<ChartPoint>> getSeries(List<Measurement> measurements) {
        Map<Device, Measurement> deviceIdToMeasurement = new HashMap<>();
        for(Measurement m : measurements) {
            deviceIdToMeasurement.put(timelineIdToDevice.get(m.getTimelineId()), m);
        }

        Map<DeviceAggregate, List<ChartPoint>> series = new HashMap<>();
        for (DeviceAggregate da : idToDeviceAggregation.values()) {

            List<Device> devices = new ArrayList<>();
            for(String deviceId : da.getDeviceIds()) {
                Device device = idToDevice.get(deviceId);
                if (device != null) {
                    devices.add(idToDevice.get(deviceId));
                }
            }
            Collections.sort(devices, new Comparator<Device>() {
                @Override
                public int compare(Device o1, Device o2) {
                    return o1.getLeveeDistanceMarker().
                            compareTo(o2.getLeveeDistanceMarker());
                }
            });

            List<ChartPoint> chartPoints = new ArrayList<>();
            for (Device d : devices) {
                Measurement measurement = deviceIdToMeasurement.get(d);
                double value = measurement != null ? measurement.getValue() : 0d;

                ChartPoint point = new ChartPoint(d, idToSections.get(d.getSectionId()),
                        d.getLeveeDistanceMarker(), value);
                chartPoints.add(point);
            }

            series.put(da, chartPoints);
        }

        return series;
    }

    @Override
    public Section getDeviceSection(Device device) {
        return idToSections.get(device.getSectionId());
    }

    @Override
    public Collection<Section> getSections() {
        return idToSections.values();
    }

    @Override
    public void getMeasurements(final Device device, Date startDate, Date endDate, final DateSeriesCallback callback) {
        GWT.log("From " + startDate + " to " + endDate);
        dapController.getMeasurements
                (timelineIdToDevice.inverse().get(device), startDate, endDate, new MeasurementsCallback(callback) {
            @Override
            public void processMeasurements(List<Measurement> measurements) {
                callback.series(createChartSeries(device, measurements));
            }
        });
    }

    private ChartSeries createChartSeries(Device device, List<Measurement> measurements) {
        Parameter parameter = idToPrameter.get(device.getParameterIds().get(0));

        ChartSeries chartSeries = new ChartSeries();
        chartSeries.setDeviceId(device.getId());
        chartSeries.setParameterId(parameter.getId());
        chartSeries.setName(parameter.getParameterName());
        chartSeries.setUnit(parameter.getMeasurementTypeUnit());
        chartSeries.setLabel(parameter.getMeasurementTypeName());
        
        if (measurements.size() > 0) {
        	chartSeries.setTimelineId(measurements.get(0).getTimelineId());
        }
        
        Number[][] values = new Number[measurements.size()][2];
        chartSeries.setValues(values);

        int index = 0;
        for (Measurement point : measurements) {
            values[index][0] = point.getTimestamp().getTime();
            values[index][1] = point.getValue();
            index++;
        }

        return chartSeries;
    }

    @Override
    public void getMeasurements(Collection<Device> devices,
                                final Date startDate, final Date endDate,
                                final DevicesDateSeriesCallback callback) {
        List<String> timelineIds = new ArrayList<>();
        BiMap<Device, String> deviceToTimelineId = timelineIdToDevice.inverse();
        for (Device d : devices) {
            timelineIds.add(deviceToTimelineId.get(d));
        }

        if (timelineIds.size() > 0) {
            dapController.getMeasurements(timelineIds, startDate, endDate, new MeasurementsCallback(callback) {
                @Override
                public void processMeasurements(List<Measurement> measurements) {
                    Map<String, List<Measurement>> timelineToMeasurements = new HashMap<String, List<Measurement>>();
                    for (Measurement measurement : measurements) {
                        List<Measurement> values = timelineToMeasurements.get(measurement.getTimelineId());
                        if (values == null) {
                            values = new ArrayList<Measurement>();
                            timelineToMeasurements.put(measurement.getTimelineId(), values);
                        }
                        values.add(measurement);
                    }

                    final List<ChartSeries> series = new ArrayList<ChartSeries>();
                    Map<String, List<Measurement>> heatingMeasurements = new HashMap<>();
                    for (Map.Entry<String, List<Measurement>> entry : timelineToMeasurements.entrySet()) {
                        if (getHeatingTimelineIds().contains(entry.getKey())) {
                            heatingMeasurements.put(entry.getKey(), entry.getValue());
                        } else {
                            series.add(createChartSeries(timelineIdToDevice.get(entry.getKey()), entry.getValue()));
                        }
                    }

                    if (heatingMeasurements.size() > 0) {
                        getHeatingChartSeries(heatingMeasurements, startDate, endDate, new DevicesDateSeriesCallback() {
                            @Override
                            public void onError(ErrorDetails errorDetails) {
                                callback.onError(errorDetails);
                            }

                            @Override
                            public void series(List<ChartSeries> heatingSeries) {
                                series.addAll(heatingSeries);
                                callback.series(series);
                            }
                        });
                    } else {
                        callback.series(series);
                    }
                }
            });
        } else {
            callback.series(new ArrayList<ChartSeries>());
        }
    }

    private void getHeatingChartSeries(final Map<String, List<Measurement>> heatingMeasurements,
                                       final Date startDate, final Date endDate,
                                       final DevicesDateSeriesCallback callback) {
        GWT.log("Get last heating measurements before: " + startDate);
        dapController.getLastMeasurements(heatingMeasurements.keySet(), startDate, new MeasurementsCallback(callback) {
            @Override
            public void processMeasurements(List<Measurement> previousMeasurements) {
                GWT.log("Preparing heating measurements");
                Map<Device, List<Measurement>> results = seriesWithLastPoint(seriesWithStartPoint(previousMeasurements));
                List<ChartSeries> heatingSeries = new ArrayList<ChartSeries>();
                for(Map.Entry<Device, List<Measurement>> series : results.entrySet()) {
                    heatingSeries.add(createChartSeries(series.getKey(), addIntermediateSteps(series.getValue())));
                }

                callback.series(heatingSeries);
            }

            private Map<Device, List<Measurement>> seriesWithStartPoint(List<Measurement> previousMeasurements) {
                Map<Device, List<Measurement>> results = new HashMap<>();
                for (Map.Entry<String, List<Measurement>> entry : heatingMeasurements.entrySet()) {
                    List<Measurement> series = entry.getValue();
                    series.add(0, new Measurement(entry.getKey(), startDate, 0f));

                    results.put(timelineIdToDevice.get(entry.getKey()), series);
                }

                for (Measurement previousMeasurement : previousMeasurements) {
                    List<Measurement> series = results.get(timelineIdToDevice.get(previousMeasurement.getTimelineId()));
                    series.remove(0);
                    series.add(new Measurement(previousMeasurement.getTimelineId(),
                            startDate, previousMeasurement.getValue()));
                }

                return results;
            }

            private Map<Device, List<Measurement>> seriesWithLastPoint(Map<Device, List<Measurement>> results) {
                for(List<Measurement> series : results.values()) {
                    if (series.size() > 0) {
                        Measurement lastPoint = series.get(series.size() - 1);
                        if (lastPoint.getTimestamp().compareTo(endDate) < 0) {
                            series.add(new Measurement(lastPoint.getTimelineId(), endDate, lastPoint.getValue()));
                        }
                    }
                }

                return results;
            }

            private List<Measurement> addIntermediateSteps(List<Measurement> series) {
                List<Measurement> withIntermediateSteps = new ArrayList<>();

                withIntermediateSteps.add(series.get(0));
                for (int i = 1; i < series.size() - 1; i++) {
                    Measurement point = series.get(i);
                    Measurement previous = series.get(i - 1);
                    if(point.getValue() != previous.getValue()) {
                        withIntermediateSteps.add(new Measurement(previous.getTimelineId(),
                                point.getTimestamp(), previous.getValue()));
                    }
                    withIntermediateSteps.add(point);
                }
                withIntermediateSteps.add(series.get(series.size() - 1));

                return withIntermediateSteps;
            }
        });
    }

    private List<String> getHeatingTimelineIds() {
        List<String> timelineIds = new ArrayList<>();
        BiMap<Device, String> deviceToTimelineId = timelineIdToDevice.inverse();
        for (Device device : heatingDevices) {
            String timelineId = deviceToTimelineId.get(device);
            if (timelineId != null) {
                timelineIds.add(timelineId);
            }
        }

        return timelineIds;
    }

    public Collection<Device> getHeatingDevices() {
        return heatingDevices;
    }

    @Override
    public Collection<DeviceAggregate> getDeviceAggregations() {
        return idToDeviceAggregation.values();
    }
}
