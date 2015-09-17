package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.gwt.i18n.client.Messages;

public interface FibreMessages extends Messages {
    String fibreChartTitle();
    String firbreChartXAxisTitle();
    String deviceChartInitTitle();
    String deviceChartSelectTitle(String sensorName);
    String loadingDeviceValues(String sensorName);
    String loadingFibreShare();
    String loadingData();

    String errorLoadingDataFromDap();

}
