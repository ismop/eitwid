package pl.ismop.web.client.widgets.error;

import javax.inject.Inject;

import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;

import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.error.ErrorDetails;

@EventHandler
public class ErrorPresenter extends BaseEventHandler<MainEventBus> {
	private ErrorMessages errorMessages;

	@Inject
	public ErrorPresenter(ErrorMessages errorMessages) {
		this.errorMessages = errorMessages;
	}
	
	public void onShowError(ErrorDetails errorDetails) {
		NotifySettings settings = NotifySettings.newSettings();
		settings.setAnimation(Animation.FADE_IN_DOWN, Animation.FADE_OUT_RIGHT);
		settings.setType(NotifyType.DANGER);
		Notify.notify(errorMessages.errorTitle(), errorDetails.getMessage(), settings);
	}
}