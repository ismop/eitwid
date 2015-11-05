package pl.ismop.web.client.widgets.delegator;

import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.error.ErrorCallback;

public abstract class ScenariosCallback extends ErrorCallbackDelegator implements DapController.ScenariosCallback {
    public ScenariosCallback(ErrorCallback errorCallback) {
        super(errorCallback);
    }
}