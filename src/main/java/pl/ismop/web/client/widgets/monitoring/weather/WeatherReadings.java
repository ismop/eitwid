package pl.ismop.web.client.widgets.monitoring.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;

public class WeatherReadings {
	
	final Map<String, Device> deviceMap = new HashMap<>();
	final Map<String, Parameter> parameterMap = new HashMap<>();
	final Map<String, Timeline> timelineMap = new HashMap<>();
	
	final List<String> deviceIds = new ArrayList<>();
	final List<String> timelineIds = new ArrayList<>();
	final List<String> parameterIds = new ArrayList<>();
	
	final Map<String, Timeline> parameterToTimeline = new HashMap<>();
	
	Context context; 
	
	List<Parameter> getParametersForDevice(String deviceId) {
		ArrayList<Parameter> list = new ArrayList<Parameter>();
		
		for (Parameter parameter : parameterMap.values()) {
			if (parameter.getDeviceId().equals(deviceId)) {
				list.add(parameter);
			}
		}
		
		Collections.sort(list, parameterComparator);
		
		return list;
	}
	
	void addDevices(List<Device> devices) {
		for (Device device : devices) {
			deviceMap.put(device.getId(), device);
		}
		deviceIds.addAll(deviceMap.keySet());
		Collections.sort(deviceIds);
	}
	
	void addParemeters(List<Parameter> parameters) {
		for (Parameter parameter : parameters) {
			parameterMap.put(parameter.getId(), parameter);
		}
		parameterIds.addAll(parameterMap.keySet());
		Collections.sort(parameterIds);
	}

	void addTimelines(List<Timeline> timelines) {
		for (Timeline timeline : timelines) {
			timelineMap.put(timeline.getId(), timeline);
			parameterToTimeline.put(timeline.getParameterId(), timeline);	
		}
		timelineIds.addAll(timelineMap.keySet());
		Collections.sort(timelineIds);
	}
	
	void setContext(Context context) {
		this.context = context;
	}

	Context getContext() {
		return context;
	}
	
	Map<String, Measurement> getLastMeasurements(List<Measurement> measurements) {
		Map<String, Measurement> lastReadings = new HashMap<String, Measurement>();
		for (Measurement measurement : measurements) {
			Timeline t = timelineMap.get(measurement.getTimelineId());
			Parameter p = parameterMap.get(t.getParameterId());
			Measurement old = lastReadings.get(p.getId());
			if (old == null || old.getTimestamp().before(measurement.getTimestamp())) {
				lastReadings.put(p.getId(), measurement);
			}
		}
		return lastReadings;
	}
	
	void clear() {
		deviceMap.clear();
		timelineMap.clear();
		parameterMap.clear();
		deviceIds.clear();
		timelineIds.clear();
		parameterIds.clear();
		parameterToTimeline.clear();
		context = null;
	}
	
	private Comparator<Parameter> parameterComparator = new Comparator<Parameter>() {
		@Override
		public int compare(Parameter o1, Parameter o2) {
			//this is custom behavior to push some of the weather parameters down the list 
			if(o1.getCustomId().equals("ASP.PATM") || o1.getCustomId().equals("weatherStation.rainfallHour")) {
				return 1;
			}
			
			if(o2.getCustomId().equals("ASP.PATM") || o2.getCustomId().equals("weatherStation.rainfallHour")) {
				return -1;
			}
			
			return o1.getParameterName().compareTo(o2.getParameterName());
		}
	};
}