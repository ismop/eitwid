package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.gwt.i18n.client.Messages;

public interface FibreMessages extends Messages {
    String fibreChartTitle();
    String firbreChartXAxisTitle();
    String deviceChartInitTitle();
    String loadingDeviceValues(String sensorName);
    String loadingDevicesValues();
    String loadingFibreShare();
    String loadingData();

    String deviceTooltip(String value, String leveeDistanceMarker, String cableDistanceMarker, String sensorId);

    String errorLoadingDataFromDap();
}
