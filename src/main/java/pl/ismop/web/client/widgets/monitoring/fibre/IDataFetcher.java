package pl.ismop.web.client.widgets.monitoring.fibre;

import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorCallback;
import scala.Array;

import java.util.Collection;
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
        private final Device device;
        private final Section section;
        private final float x;
        private final float y;

        public ChartPoint(Device device, Section section, float x, float y) {
            this.device = device;
            this.section = section;
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public Device getDevice() {
            return device;
        }

        public Section getSection() {
            return section;
        }
    }

    void initialize(InitializeCallback callback);
    void getSeries(Date selectedDate, SeriesCallback callback);
    Section getDeviceSection(Device device);
    Collection<Section> getSections();
}
