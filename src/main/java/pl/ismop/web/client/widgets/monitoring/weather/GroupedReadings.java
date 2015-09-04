package pl.ismop.web.client.widgets.monitoring.weather;

import java.util.Date;
import java.util.List;
import java.util.Map;

import pl.ismop.web.client.widgets.old.plot.Readings;

public class GroupedReadings {
	public static class LatestReading {
		public Number value;
		
		public String unit;
		
		public String label;
		
		public Date timestamp;
	}
	
	private List<Readings> readingsList;
	
	private Map<String, List<LatestReading>> latestReadings;

	public List<Readings> getReadingsList() {
		return readingsList;
	}

	public void setReadingsList(List<Readings> readingsList) {
		this.readingsList = readingsList;
	}

	public Map<String, List<LatestReading>> getLatestReadings() {
		return latestReadings;
	}

	public void setLatestReadings(Map<String, List<LatestReading>> latestReadings) {
		this.latestReadings = latestReadings;
	}
}