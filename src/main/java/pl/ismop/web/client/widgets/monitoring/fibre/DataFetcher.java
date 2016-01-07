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
import pl.ismop.web.client.widgets.common.DateChartPoint;
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
    private boolean mock;

    private Map<String, Section> idToSections = new HashMap<>();
    private Map<String, DeviceAggregate> idToDeviceAggregation = new HashMap<>();
    private Map<String, Device> idToDevice = new HashMap<>();
    private BiMap<String, Device> timelineIdToDevice;
    private boolean initialized = false;
    private String yAxisTitle;

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
                                        List<String> ids = new ArrayList<>();
                                        final Map<String, Device> parameterIdToDevice = new HashMap<String, Device>();
                                        for (Parameter p : parameters) {
                                            ids.add(p.getId());
                                            parameterIdToDevice.put(p.getId(), idToDevice.get(p.getDeviceId()));
                                        }
                                        setXAxisTitle(parameters.get(0));

                                        GWT.log(parameters.size() + " parameters loaded, loading timelines");
                                        dapController.getTimelinesForParameterIds(contextId, ids, new TimelinesCallback(callback) {
                                            @Override
                                            public void processTimelines(List<Timeline> timelines) {
                                                GWT.log(timelines.size() + " timelines loaded");
                                                initialized = true;

                                                timelineIdToDevice = HashBiMap.create(timelines.size());
                                                for (Timeline t : timelines) {
                                                    Device device = parameterIdToDevice.get(t.getParameterId());
                                                    timelineIdToDevice.put(t.getId(), device);
                                                }

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

    private void setXAxisTitle(Parameter parameter) {
        yAxisTitle = parameter.getMeasurementTypeName() + " [" + parameter.getMeasurementTypeUnit() + "]";
    }

    @Override
    public String getXAxisTitle() {
        return yAxisTitle;
    }

    @Override
    public void getSeries(Date selectedDate, SeriesCallback callback) {
        if (mock) {
            getMockedSeries(selectedDate, callback);
        } else {
            getRealSeries(selectedDate, callback);
        }
    }

    private void getRealSeries(Date selectedDate, final SeriesCallback callback) {
        dapController.getLastMeasurements(new ArrayList<>(timelineIdToDevice.keySet()),
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
                devices.add(idToDevice.get(deviceId));

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

    private void getMockedSeries(Date selectedDate, final SeriesCallback callback) {
        GWT.log("Generate mocked series");
        com.google.gwt.user.client.Timer timer = new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                callback.series(generateSeries());
                GWT.log("Mocked series generated");
            }
        };

        timer.schedule(new Random().nextInt(2000));
    }

    private Map<DeviceAggregate, List<ChartPoint>> generateSeries() {
        Map<DeviceAggregate, List<ChartPoint>> series = new HashMap<>();
        Random random = new Random();
        for (DeviceAggregate da : idToDeviceAggregation.values()) {

            List<Device> devices = new ArrayList<>();
            for(String deviceId : da.getDeviceIds()) {
                devices.add(idToDevice.get(deviceId));

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
                ChartPoint point = new ChartPoint(d, idToSections.get(d.getSectionId()),
                                                  d.getLeveeDistanceMarker(),
                                                  random.nextInt(25) + 10);

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
    public void getMeasurements(Device device, Date startDate, Date endDate, DateSeriesCallback callback) {
        if (mock) {
            getMockedMeasurements(device, startDate, endDate, callback);
        } else {
            getRealMeasurements(device, startDate, endDate, callback);
        }
    }

    @Override
    public void getMeasurements(Collection<Device> devices,
                                Date startDate, Date endDate,
                                final DevicesDateSeriesCallback callback) {
        List<String> timelineIds = new ArrayList<>();
        BiMap<Device, String> deviceToTimelineId = timelineIdToDevice.inverse();
        for (Device d : devices) {
            timelineIds.add(deviceToTimelineId.get(d));
        }
        dapController.getMeasurements(timelineIds, startDate, endDate, new MeasurementsCallback(callback) {
            @Override
            public void processMeasurements(List<Measurement> measurements) {
                Map<Device, List<DateChartPoint>> results = new HashMap<Device, List<DateChartPoint>>();
                for (Measurement m : measurements) {
                    Device device = timelineIdToDevice.get(m.getTimelineId());
                    List<DateChartPoint> series = results.get(device);
                    if(series == null) {
                        series = new ArrayList<DateChartPoint>();
                        results.put(device, series);
                    }
                    series.add(new DateChartPoint(m.getTimestamp(), m.getValue()));
                }
                callback.series(results);
            }
        });
    }

    private void getMockedMeasurements(final Device device,
                                final Date startDate, final Date endDate,
                                final DateSeriesCallback callback) {
        com.google.gwt.user.client.Timer timer = new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                callback.series(generateDateSeries(device, startDate, endDate));
            }
        };

        timer.schedule(new Random().nextInt(2000));
    }

    private void getRealMeasurements(Device device, Date startDate, Date endDate, final DateSeriesCallback callback) {
        GWT.log("From " + startDate + " to " + endDate);
        dapController.getMeasurements
                (timelineIdToDevice.inverse().get(device), startDate, endDate, new MeasurementsCallback(callback) {
                    @Override
                    public void processMeasurements(List<Measurement> measurements) {
                        List<DateChartPoint> points = new ArrayList<>();
                        for (Measurement m : measurements) {
                            points.add(new DateChartPoint(m.getTimestamp(), m.getValue()));
                        }
                        callback.series(points);
                    }
                });
    }



    private List<DateChartPoint> generateDateSeries(Device device, Date startDate, Date endDate) {
        List<DateChartPoint> points = new ArrayList<>();

        Random random = new Random();
        while(startDate.before(endDate)) {
            points.add(new DateChartPoint(startDate, random.nextInt(25) + 10));
            startDate = new Date(startDate.getTime() + 86400000);
        }

        return points;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
    }

    @Override
    public Collection<DeviceAggregate> getDeviceAggregations() {
        return idToDeviceAggregation.values();
    }
}
