package pl.ismop.web.client.widgets.monitoring.readings;

import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Well;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.monitoring.readings.IReadingsView.IReadingsPresenter;

public class ReadingsView extends Composite implements IReadingsView, ReverseViewInterface<IReadingsPresenter> {
	private static ReadingsViewUiBinder uiBinder = GWT.create(ReadingsViewUiBinder.class);

	interface ReadingsViewUiBinder extends UiBinder<Widget, ReadingsView> {}
	
	interface Style extends CssResource {
		String label();
		
		String icon();
	}
	
	private IReadingsPresenter presenter;
	
	private Map<String, HTMLPanel> labels;

	@UiField
	ReadingsMessages messages;
	
	@UiField
	Style style;
	
	@UiField
	Modal modal;
	
	@UiField
	FlowPanel mapContainer, miscContainer, chartContainer;
	
	@UiField
	ListBox additionalReadings;
	
	@UiField
	Well additionalLabels;
	
	public ReadingsView() {
		initWidget(uiBinder.createAndBindUi(this));
		labels = new HashMap<>();
	}
	
	@UiHandler("modal")
	void modalShown(ModalShownEvent event) {
		getPresenter().onModalShown();
	}
	
	@UiHandler("additionalReadings")
	void additionalReadingsPicked(ChangeEvent event) {
		getPresenter().onAdditionalReadingsPicked(additionalReadings.getSelectedValue());
	}

	@Override
	public void showModal(boolean show) {
		if(show) {
			modal.show();
		} else {
			modal.hide();
		}
	}

	@Override
	public void setPresenter(IReadingsPresenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public IReadingsPresenter getPresenter() {
		return presenter;
	}
	
	@Override
	public void setMap(IsWidget map) {
		mapContainer.add(map);
	}

	@Override
	public void setChart(IsWidget chart) {
		chartContainer.add(chart);
	}

	@Override
	public int getChartContainerHeight() {
		return chartContainer.getOffsetHeight();
	}

	@Override
	public void addAdditionalReadingsOption(String id, String parameterName) {
		additionalReadings.addItem(parameterName, id);
	}

	@Override
	public void resetAdditionalReadings() {
		additionalReadings.clear();
		additionalLabels.clear();
	}

	@Override
	public String pickAdditionalReadingLabel() {
		return messages.pickAdditionalReadingsLabel();
	}

	@Override
	public void setSelectedAdditionalReadings(String optionId) {
		for(int i = 0; i < additionalReadings.getItemCount(); i++) {
			if(additionalReadings.getValue(i).equals(optionId)) {
				additionalReadings.setSelectedIndex(i);
				
				break;
			}
		}
	}

	@Override
	public void addAdditionalReadingsLabel(final String id, String labelText) {
		HTMLPanel label = new HTMLPanel("span", "");
		label.addStyleName("label label-primary");
		label.add(new Span(labelText));
		
		Icon icon = new Icon(IconType.REMOVE);
		icon.addStyleName(style.icon());
		icon.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getPresenter().onAdditionalReadingsRemoved(id);
			}
		});
		label.add(icon);
		label.addStyleName(style.label());
		additionalLabels.add(label);
		labels.put(id, label);
	}

	@Override
	public void showNoAdditionalReadingsLabel(boolean show) {
		if(show) {
			Label label = new Label(messages.noAdditionalReadingsLabel());
			additionalLabels.add(label);
		} else {
			additionalLabels.clear();
		}
	}

	@Override
	public void removeAdditionalReadingsLabel(String id) {
		if(labels.containsKey(id)) {
			additionalLabels.remove(labels.get(id));
		}
	}
}