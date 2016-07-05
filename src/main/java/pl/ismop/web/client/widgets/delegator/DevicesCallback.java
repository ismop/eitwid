package pl.ismop.web.client.widgets.delegator;

import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.error.ErrorCallback;
import pl.ismop.web.client.error.ErrorDetails;

public abstract class DevicesCallback implements DapController.DevicesCallback {
    ErrorCallback errorCallback;

    public DevicesCallback(ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    @Override
    public void onError(ErrorDetails errorDetails) {
        errorCallback.onError(errorDetails);
    }
}
