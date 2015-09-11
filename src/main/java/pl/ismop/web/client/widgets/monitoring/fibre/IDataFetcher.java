package pl.ismop.web.client.widgets.monitoring.fibre;

import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.error.ErrorCallback;
import scala.Array;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by marek on 11.09.15.
 */
public interface IDataFetcher {
    interface SeriesCallback extends ErrorCallback {
        void series(Map<DeviceAggregation, List<ChartPoint>> series);
    }

    class ChartPoint {
        private String deviceId;
        private int x;
        private int y;

        public ChartPoint(String deviceId, int x, int y) {
            this.deviceId = deviceId;
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public String getDeviceId() {
            return deviceId;
        }
    }

    void getSeries(Date selectedDate, SeriesCallback callback);

    Device getDevice(String deviceId);
}
