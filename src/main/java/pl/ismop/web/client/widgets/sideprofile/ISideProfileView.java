package pl.ismop.web.client.widgets.sideprofile;

import thothbot.parallax.core.client.AnimatedScene;

import com.google.gwt.user.client.ui.IsWidget;

public interface ISideProfileView extends IsWidget {
	interface IProfilePresenter {
		
	}

	void setScene(AnimatedScene scene);
}