package pl.ismop.web.client.widgets.monitoring.sidepanel;

import com.google.gwt.i18n.client.Messages;

public interface MonitoringSidePanelMessages extends Messages {
	String leveeNameLabel();

	String leveeProgressTitle();

	String noLeveesLabel();

	String showWeatherLabel();

	String showFibreLabel();

	String metadataHeading();

	String metadataHelp();

	String nameLabel();

	String internalIdLabel();

	String chartHeading();

	String chartHelp();

	String noMeasurementsForDevice();

	String typeLabel();

	String profileTypeLabel();

	String sectionTypeLabel();

	String deviceTypeLabel();

	String deviceAggregateTypeLabel();

	String expandChartTitle();

	String clearChartTitle();

	String aggregateContents();

	String soilType();

	String granularDensity();

	String bulkDensity();

	String filtrationCoefficient();

	String maxMinAvg(String max, String min, String avg);

	String showWaterHightLabel();

	String coordinatesLabel();
}