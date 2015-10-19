package pl.ismop.web.client.widgets.delegator;

import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.error.ErrorCallback;
import pl.ismop.web.client.error.ErrorDetails;

/**
 * Created by marek on 13.10.15.
 */
public abstract class ContextsCallback implements DapController.ContextsCallback {
    ErrorCallback errorCallback;

    public ContextsCallback(ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    @Override
    public void onError(ErrorDetails errorDetails) {
        errorCallback.onError(errorDetails);
    }
}
