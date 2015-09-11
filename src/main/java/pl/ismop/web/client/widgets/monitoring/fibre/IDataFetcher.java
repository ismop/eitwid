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

    interface InitializeCallback extends  ErrorCallback {
        void ready();
    }

    class ChartPoint {
        private Device device;
        private int x;
        private int y;

        public ChartPoint(Device device, int x, int y) {
            this.device = device;
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Device getDevice() {
            return device;
        }
    }

    void initialize(InitializeCallback callback);
    void getSeries(Date selectedDate, SeriesCallback callback);
}
