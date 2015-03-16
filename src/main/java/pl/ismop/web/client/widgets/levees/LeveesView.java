package pl.ismop.web.client.widgets.levees;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class LeveesView extends Composite implements ILeveesView {
	private static LeveesViewUiBinder uiBinder = GWT.create(LeveesViewUiBinder.class);
	interface LeveesViewUiBinder extends UiBinder<Widget, LeveesView> {}
	
	@UiField LeveesViewMessages messages;
	@UiField HTMLPanel container;

	public LeveesView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public String popupTitle() {
		return messages.popupTitle();
	}

	@Override
	public void showNoLeveesMessage() {
		container.clear();
		container.add(new HTML(messages.noLevees()));
	}

	@Override
	public void addLeveeWidget(IsWidget view) {
		container.add(view);
	}
}