package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Timer;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;

import java.util.*;
import java.util.Random;

/**
 * Created by marek on 11.09.15.
 */
public class MockDateFetcher implements IDataFetcher {
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

    private Map<DeviceAggregation, List<ChartPoint>> generateSeries() {
        Random random = new Random();
        Map<DeviceAggregation, List<ChartPoint>> series = new HashMap<>();
        for (int i = 0; i < 2; i++) {
            List<ChartPoint> s = new ArrayList<>();
            for (int j = 0; j < 250; j++) {
                s.add(new ChartPoint(i + "-" + j, j, random.nextInt(25) + 10));
            }

            DeviceAggregation aggregation = new DeviceAggregation();
            aggregation.setId("Światłowód " + (i + 1));
            series.put(aggregation, s);
        }

        return series;
    }


    @Override
    public Device getDevice(String deviceId) {
        Device device = new Device();
        device.setId(deviceId);

        return device;
    }
}
