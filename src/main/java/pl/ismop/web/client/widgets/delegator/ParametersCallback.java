package pl.ismop.web.client.widgets.delegator;

import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.error.ErrorCallback;

public abstract class ParametersCallback extends ErrorCallbackDelegator implements DapController.ParametersCallback {
    public ParametersCallback(ErrorCallback errorCallback) {
        super(errorCallback);
    }
}