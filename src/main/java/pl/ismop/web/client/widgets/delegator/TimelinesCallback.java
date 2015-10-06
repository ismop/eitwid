package pl.ismop.web.client.widgets.delegator;

import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.error.ErrorCallback;

public abstract class TimelinesCallback extends ErrorCallbackDelegator implements DapController.TimelinesCallback {
    public TimelinesCallback(ErrorCallback errorCallback) {
        super(errorCallback);
    }
}
