package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Timer;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.dap.section.Section;

import java.util.*;
import java.util.Random;

/**
 * Created by marek on 11.09.15.
 */
public class MockDateFetcher implements IDataFetcher {
    @Override
    public void initialize(final InitializeCallback callback) {
        Timer timer = new Timer() {
            @Override
            public void run() {
                callback.ready();
            }
        };

        timer.schedule(new Random().nextInt(4000));
    }

    @Override
    public void getSeries(Date selectedDate, final SeriesCallback callback) {
        Timer timer = new Timer() {
            @Override
            public void run() {
                callback.series(generateSeries());
            }
        };

        timer.schedule(new Random().nextInt(2000));
    }

    @Override
    public Section getDeviceSection(Device device) {
        Section section = new Section();
        section.setId(device.getId());
        section.setName("Section with device " + device.getId());
        return section;
    }

    private Map<DeviceAggregation, List<ChartPoint>> generateSeries() {
        Random random = new Random();
        Map<DeviceAggregation, List<ChartPoint>> series = new HashMap<>();
        for (int i = 0; i < 2; i++) {
            List<ChartPoint> s = new ArrayList<>();
            for (int j = 0; j < 250; j++) {
                Device d = new Device();
                d.setId((i + 1) + "-" + (j + 1));
                s.add(new ChartPoint(d, j, random.nextInt(25) + 10));
            }

            DeviceAggregation aggregation = new DeviceAggregation();
            aggregation.setId("Światłowód " + (i + 1));
            series.put(aggregation, s);
        }

        return series;
    }
}
