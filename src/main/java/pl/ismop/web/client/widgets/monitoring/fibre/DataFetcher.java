package pl.ismop.web.client.widgets.monitoring.fibre;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;

import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;

/**
 * Created by marek on 14.09.15.
 */
public class DataFetcher implements IDataFetcher {

    private class DeviceHolder {
        private final Device device;
        private final Timeline temperatureTimeline;

        public DeviceHolder(Device device, Timeline temperatureTimeline) {
            this.device = device;
            this.temperatureTimeline = temperatureTimeline;
        }

        public Device getDevice() {
            return device;
        }

        public String getId() {
            return device.getId();
        }

        public String getTimelineId() {
            return temperatureTimeline.getId();
        }
    }

    private final Levee levee;
    private final DapController dapController;
    private final String contextId;

    private final Map<DeviceAggregation, List<Device>> cache = new HashMap<>();

    public DataFetcher(DapController dapController, Levee levee) {
        this.dapController = dapController;
        this.levee = levee;
        this.contextId = "1";
    }

    @Override
    public void initialize(final InitializeCallback callback) {
        GWT.log("Loading device aggregations");
        dapController.getDeviceAggregationForType("fiber", levee.getId(), new DapController.DeviceAggregationsCallback() {
            @Override
            public void processDeviceAggregations(List<DeviceAggregation> deviceAggreagations) {
                GWT.log(deviceAggreagations.size() + " devise aggregation loaded, loading devices");
                List<String> deviceAggregationIds = new ArrayList<>();
				
				for(DeviceAggregation deviceAggregation : deviceAggreagations) {
					deviceAggregationIds.add(deviceAggregation.getId());
				}
				
                dapController.getDevicesRecursivelyForAggregates(deviceAggregationIds, new DapController.DevicesCallback() {
                    @Override
                    public void processDevices(List<Device> devices) {
                        List<String> ids = new ArrayList<>();
                        Set<String> sectionIds = new HashSet<>();
                        for (Device d : devices) {
                            ids.add(d.getId());
                            sectionIds.add(d.getSectionId());
                        }

                        GWT.log(devices.size() + " devices loaded, loading parameters");
                        dapController.getParameters(ids, new DapController.ParametersCallback() {
                            @Override
                            public void processParameters(List<Parameter> parameters) {
                                List<String> ids = new ArrayList<String>();
                                for (Parameter p : parameters) {
                                    ids.add(p.getId());
                                }

                                GWT.log(parameters.size() + " parameters loaded, loading timelines");
                                dapController.getTimelinesForParameterIds(contextId, ids, new DapController.TimelinesCallback() {
                                    @Override
                                    public void processTimelines(List<Timeline> timelines) {
                                        GWT.log(timelines.size() + " timelines loaded");
                                        //TODO

                                        callback.ready();
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

                        GWT.log(devices.size() + " devices loaded, loading sections");
                        dapController.getSections(Arrays.asList(sectionIds.toArray(new String[0])), new DapController.SectionsCallback() {
                            @Override
                            public void processSections(List<Section> sections) {
                                GWT.log(sections.size() + " sections loaded");
                                // TODO
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

    private void setDeviseAggregation(List<DeviceAggregation> deviceAggregations) {
        for (DeviceAggregation d : deviceAggregations) {
            cache.put(d, new ArrayList<Device>());
        }
    }

    private void setDevices(List<Device> devices) {
        Map<String, DeviceAggregation> mapping = new HashMap<>();
        for(DeviceAggregation d : cache.keySet()) {
            mapping.put(d.getId(), d);
        }

        for(Device d : devices) {
            cache.get(d.getId()).add(d);
        }
    }

    @Override
    public void getSeries(Date selectedDate, SeriesCallback callback) {

    }

    @Override
    public Section getDeviceSection(Device device) {
        return null;
    }
}
