package pl.ismop.web.client.widgets.monitoring.fibre;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorCallback;
import pl.ismop.web.client.widgets.common.DateChartPoint;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;

/**
 * Created by marek on 11.09.15.
 */
public interface IDataFetcher {
    Collection<DeviceAggregate> getDeviceAggregations();

    interface SeriesCallback extends ErrorCallback {
        void series(Map<DeviceAggregate, List<ChartPoint>> series);
        void noData();
    }

    interface DateSeriesCallback extends ErrorCallback {
        void series(ChartSeries series);
    }

    interface DevicesDateSeriesCallback extends ErrorCallback {
        void series(List<ChartSeries> series);
    }

    interface InitializeCallback extends  ErrorCallback {
        void ready();
    }

    class ChartPoint {
        private final Device device;
        private final Section section;
        private final double x;
        private final double y;

        public ChartPoint(Device device, Section section, double x, double y) {
            this.device = device;
            this.section = section;
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
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

    String getXAxisTitle();

    void getSeries(Date selectedDate, SeriesCallback callback);
    Section getDeviceSection(Device device);
    Collection<Section> getSections();

    void getMeasurements(Device device, Date startDate, Date endDate, DateSeriesCallback callback);
    void getMeasurements(Collection<Device> devices, Date startDate, Date endDate, DevicesDateSeriesCallback callback);
}
