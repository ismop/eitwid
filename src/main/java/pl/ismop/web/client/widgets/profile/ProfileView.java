package pl.ismop.web.client.widgets.profile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ProfileView extends Composite implements IProfileView {
	private static ProfileViewUiBinder uiBinder = GWT.create(ProfileViewUiBinder.class);
	interface ProfileViewUiBinder extends UiBinder<Widget, ProfileView> {}
	
	@UiField HeadingElement header;
	@UiField DivElement threatPanel;
	@UiField ParagraphElement threat;
	@UiField ProfileViewMessages messages;

	public ProfileView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setHeader(String name) {
		header.setInnerText(name);
	}

	@Override
	public void setModeStyle(String style) {
		threatPanel.removeAttribute("class");
		threatPanel.addClassName("panel");
		threatPanel.addClassName(style);
	}

	@Override
	public void setThreatLevel(String threatLevel) {
		threat.setInnerText(translateThreatLevel(threatLevel));
	}
	
	private String translateThreatLevel(String threatLevel) {
		switch(threatLevel) {
			case "none":
				return messages.threatLevelNone();
			case "heightened":
				return messages.threatLevelElevated();
			case "severe":
				return messages.threatLevelSevere();
			default:
				return messages.threatLevelUnknown();
		}
	}
}