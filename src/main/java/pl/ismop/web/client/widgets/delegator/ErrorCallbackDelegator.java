package pl.ismop.web.client.widgets.delegator;

import pl.ismop.web.client.error.ErrorCallback;
import pl.ismop.web.client.error.ErrorDetails;

public class ErrorCallbackDelegator implements ErrorCallback {
    ErrorCallback errorCallback;

    public ErrorCallbackDelegator(ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    @Override
    public void onError(ErrorDetails errorDetails) {
        errorCallback.onError(errorDetails);
    }
}