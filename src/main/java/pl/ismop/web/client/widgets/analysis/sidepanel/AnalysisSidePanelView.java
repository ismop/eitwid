package pl.ismop.web.client.widgets.analysis.sidepanel;

import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.widgets.analysis.sidepanel.IAnalysisSidePanelView.IAnalysisSidePanelPresenter;

public class AnalysisSidePanelView extends Composite
		implements IAnalysisSidePanelView, ReverseViewInterface<IAnalysisSidePanelPresenter> {
	private static AnalysisSidePanelUiBinder uiBinder = GWT.create(AnalysisSidePanelUiBinder.class);
	private IAnalysisSidePanelPresenter presenter;

	interface AnalysisSidePanelUiBinder extends UiBinder<Widget, AnalysisSidePanelView> {}

	public interface Css extends CssResource {
		String minimapWithRefresher();
		String minimapWithoutRefresher();
	}

	@UiField
	FlowPanel waterWavePanel;

	@UiField
	FlowPanel miniMapPanel;

	@UiField
	FlowPanel refresherPanel;

	@UiField
	Anchor selectedExperiment;

	@UiField
	DropDownMenu experimentList;

	@UiField
	AnalysisSidePanelMessages messages;

	@UiField
	Button export;

	@UiField
	Css style;

	public AnalysisSidePanelView() {
		initWidget(uiBinder.createAndBindUi(this));

		selectedExperiment.setText(messages.loadingExperiments());
		selectedExperiment.setEnabled(false);
		export.setVisible(false);
	}

	@Override
	public void setWaterWavePanel(IsWidget widget) {
		waterWavePanel.add(widget);
		export.setVisible(true);
	}

	@Override
	public void setMinimap(IsWidget widget) {
		miniMapPanel.add(widget);
	}

	@Override
	public int getWaterWavePanelHeight() {
		return waterWavePanel.getOffsetHeight();
	}

	@Override
	public void setExperiments(List<Experiment> experiments) {
		experimentList.clear();
		selectedExperiment.setText(messages.selectExperiment());
		selectedExperiment.setEnabled(true);

		for (Experiment experiment : experiments) {
			AnchorListItem item = new AnchorListItem(experiment.getName());
			item.setTitle(experiment.getDescription());
			final Experiment currentExperiment = experiment;
			item.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					if (peformExperimentchange()) {
						getPresenter().selectExperiment(currentExperiment);
					}
				}
			});
			experimentList.add(item);
		}
	}

	@UiHandler("export")
	void onExport(ClickEvent click) {
		getPresenter().export();
	}

	@Override
	public void selectExperiment(Experiment currentExperiment) {
		selectedExperiment.setText(currentExperiment.getName());
		selectedExperiment.setTitle(currentExperiment.getDescription());
	}

	private boolean peformExperimentchange() {
		return selectedExperiment.getText().equals(messages.selectExperiment()) ||
				Window.confirm(messages.confirmExperimentChange());
	}

	@Override
	public void setPresenter(IAnalysisSidePanelPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IAnalysisSidePanelPresenter getPresenter() {
		return presenter;
	}

	@Override
	public AnalysisSidePanelMessages getMessages() {
		return messages;
	}

	@Override
	public void setRefresher(IsWidget widget) {
		refresherPanel.clear();
		miniMapPanel.setStyleName(style.minimapWithRefresher());
		refresherPanel.add(widget);
	}

	@Override
	public void clearRefresher() {
		refresherPanel.clear();
		miniMapPanel.setStyleName(style.minimapWithoutRefresher());
	}
}