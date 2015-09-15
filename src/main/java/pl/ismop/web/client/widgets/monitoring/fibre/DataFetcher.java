package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.gwt.core.client.GWT;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.MutableInteger;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;

import java.util.*;

/**
 * Created by marek on 14.09.15.
 */
public class DataFetcher implements IDataFetcher {


    private final Levee levee;
    private final DapController dapController;
    private final String contextId;

    private Map<String, Section> idToSections = new HashMap<>();
    private Map<String, DeviceAggregation> idToDeviceAggregation = new HashMap<>();
    private Map<String, Device> idToDevice = new HashMap<>();
    private boolean initialized = false;

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
                for(DeviceAggregation da : deviceAggreagations) {
                    idToDeviceAggregation.put(da.getId(), da);
                }

                dapController.collectDevices(deviceAggreagations, new ArrayList<Device>(), new MutableInteger(0), new DapController.DevicesCallback() {
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
                                        for (Parameter p : parameters) {
                                            ids.add(p.getId());
                                        }

                                        GWT.log(parameters.size() + " parameters loaded, loading timelines");
                                        dapController.getTimelinesForParameterIds(contextId, ids, new DapController.TimelinesCallback() {
                                            @Override
                                            public void processTimelines(List<Timeline> timelines) {
                                                GWT.log(timelines.size() + " timelines loaded");
                                                initialized = true;
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

    @Override
    public void getSeries(Date selectedDate, final SeriesCallback callback) {
        com.google.gwt.user.client.Timer timer = new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                callback.series(generateSeries());
            }
        };

        timer.schedule(new Random().nextInt(2000));
    }

    private Map<DeviceAggregation, List<ChartPoint>> generateSeries() {
        Map<DeviceAggregation, List<ChartPoint>> series = new HashMap<>();
        Random random = new Random();
        for (DeviceAggregation da : idToDeviceAggregation.values()) {

            int i = 0;
            List<Device> devices = new ArrayList<>();
            for(String deviceId : da.getDeviceIds()) {
                devices.add(idToDevice.get(deviceId));

            }
            Collections.sort(devices, new Comparator<Device>() {
                @Override
                public int compare(Device o1, Device o2) {
                    return o1.getCustomId().compareTo(o2.getCustomId());
                }
            });

            List<ChartPoint> chartPoints = new ArrayList<>();
            for (Device d : devices) {
                chartPoints.add(new ChartPoint(d, idToSections.get(d.getSectionId()), i++, random.nextInt(25) + 10));
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
}
