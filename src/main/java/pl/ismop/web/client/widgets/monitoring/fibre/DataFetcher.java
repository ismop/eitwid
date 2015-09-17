package pl.ismop.web.client.widgets.monitoring.fibre;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.gwt.core.client.GWT;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.i18n.client.DateTimeFormat;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;

public class DataFetcher implements IDataFetcher {


    private final Levee levee;
    private final DapController dapController;
    private final String contextId;
    private boolean mock;

    private Map<String, Section> idToSections = new HashMap<>();
    private Map<String, DeviceAggregation> idToDeviceAggregation = new HashMap<>();
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
        dapController.getDeviceAggregationForType("fiber", levee.getId(), new DapController.DeviceAggregationsCallback() {
            @Override
            public void processDeviceAggregations(List<DeviceAggregation> deviceAggreagations) {
                GWT.log(deviceAggreagations.size() + " devise aggregation loaded, loading devices");
                List<String> deviceAggregationIds = new ArrayList<>();
				
				for(DeviceAggregation deviceAggregation : deviceAggreagations) {
					deviceAggregationIds.add(deviceAggregation.getId());
                    idToDeviceAggregation.put(deviceAggregation.getId(), deviceAggregation);
				}
				
                dapController.getDevicesRecursivelyForAggregates(deviceAggregationIds, new DapController.DevicesCallback() {
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
                        dapController.getSections(Arrays.asList(sectionIds.toArray(new String[0])), new DapController.SectionsCallback() {
                            @Override
                            public void processSections(List<Section> sections) {
                                for (Section s : sections) {
                                    idToSections.put(s.getId(), s);
                                }

                                GWT.log(sections.size() + " sections loaded, loading parameters");
                                dapController.getParameters(ids, new DapController.ParametersCallback() {
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
                                        dapController.getTimelinesForParameterIds(contextId, ids, new DapController.TimelinesCallback() {
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

                                            @Override
                                            public void onError(ErrorDetails errorDetails) {
                                                callback.onError(errorDetails);
                                            }
                                        });
//
                                    }

                                    //
                                    @Override
                                    public void onError(ErrorDetails errorDetails) {
                                        callback.onError(errorDetails);
                                    }
                                });
                            }

                            @Override
                            public void onError(ErrorDetails errorDetails) {
                                callback.onError(errorDetails);
                            }
                        });
                    }

                    @Override
                    public void onError(ErrorDetails errorDetails) {
                        callback.onError(errorDetails);
                    }
                });
            }

            @Override
            public void onError(ErrorDetails errorDetails) {
                callback.onError(errorDetails);
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
        dapController.getLastMeasurements(new ArrayList<>(timelineIdToDevice.keySet()), selectedDate, new DapController.MeasurementsCallback() {
            @Override
            public void processMeasurements(List<Measurement> measurements) {
                GWT.log("number of loaded measurements: " + measurements.size());
                callback.series(getSeries(measurements));
            }

            @Override
            public void onError(ErrorDetails errorDetails) {
                callback.onError(errorDetails);
            }
        });
    }

    private Map<DeviceAggregation, List<ChartPoint>> getSeries(List<Measurement> measurements) {
        Map<Device, Measurement> deviceIdToMeasurement = new HashMap<>();
        for(Measurement m : measurements) {
            deviceIdToMeasurement.put(timelineIdToDevice.get(m.getTimelineId()), m);
        }

        Map<DeviceAggregation, List<ChartPoint>> series = new HashMap<>();
        for (DeviceAggregation da : idToDeviceAggregation.values()) {

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

    private Map<DeviceAggregation, List<ChartPoint>> generateSeries() {
        Map<DeviceAggregation, List<ChartPoint>> series = new HashMap<>();
        Random random = new Random();
        for (DeviceAggregation da : idToDeviceAggregation.values()) {

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
                (timelineIdToDevice.inverse().get(device), startDate, endDate, new DapController.MeasurementsCallback() {

                    @Override
                    public void onError(ErrorDetails errorDetails) {
                        callback.onError(errorDetails);
                    }

                    @Override
                    public void processMeasurements(List<Measurement> measurements) {
                        DateTimeFormat format = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601);
                        List<DateChartPoint> points = new ArrayList<>();
                        for (Measurement m : measurements) {
                            Date date = format.parse(m.getTimestamp());
                            points.add(new DateChartPoint(date, m.getValue()));
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
}
