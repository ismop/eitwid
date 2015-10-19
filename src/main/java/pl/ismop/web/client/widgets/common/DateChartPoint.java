package pl.ismop.web.client.widgets.common;

import java.util.Date;

public class DateChartPoint {
    private final Date x;
    private final double y;

    public DateChartPoint(Date x, double y) {
        this.x = x;
        this.y = y;
    }

    public Date getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
