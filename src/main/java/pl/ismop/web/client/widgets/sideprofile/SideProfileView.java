package pl.ismop.web.client.widgets.sideprofile;

import thothbot.parallax.core.client.AnimatedScene;
import thothbot.parallax.core.client.RenderingPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SideProfileView extends Composite implements ISideProfileView {
	private static ProfileViewUiBinder uiBinder = GWT.create(ProfileViewUiBinder.class);
	interface ProfileViewUiBinder extends UiBinder<Widget, SideProfileView> {}
	
	@UiField RenderingPanel panel;

	public SideProfileView() {
		initWidget(uiBinder.createAndBindUi(this));
		panel.setBackground(0xaaaaaa);
	}

	@Override
	public void setScene(AnimatedScene scene) {
		panel.setAnimatedScene(scene);
	}
}