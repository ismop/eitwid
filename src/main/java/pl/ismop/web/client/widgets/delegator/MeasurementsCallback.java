package pl.ismop.web.client.widgets.delegator;

import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.error.ErrorCallback;

public abstract class MeasurementsCallback extends ErrorCallbackDelegator implements DapController.MeasurementsCallback {
    public MeasurementsCallback(ErrorCallback errorCallback) {
        super(errorCallback);
    }
}