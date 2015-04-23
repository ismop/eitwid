package pl.ismop.web.client.widgets.experiment;

import org.gwtbootstrap3.client.shared.event.ShownEvent;
import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Small;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.experiment.IExperimentView.IExperimentPresenter;

public class ExperimentView extends Composite implements IExperimentView, ReverseViewInterface<IExperimentPresenter> {
	private static ExperimentViewUiBinder uiBinder = GWT.create(ExperimentViewUiBinder.class);
	interface ExperimentViewUiBinder extends UiBinder<Widget, ExperimentView> {}
	
	private IExperimentPresenter presenter;
	
	@UiField ExperimentMessages messages;
	@UiField PanelBody analysisBody;
	@UiField ListBox sensors;
	@UiField FlowPanel plot;
	@UiField FlowPanel blog;
	@UiField ListBox type;
	@UiField TextBox message;
	@UiField FlowPanel wave;

	public ExperimentView() {
		initWidget(uiBinder.createAndBindUi(this));
		fillInSensors();
		plot.getElement().setAttribute("id", "singlePlot");
		wave.getElement().setAttribute("id", "wave");
	}
	
	@UiHandler("plotCollapse")
	void plotShowed(ShownEvent event) {
		getPresenter().showPlot();
	}
	
	@UiHandler("waveCollapse")
	void waveShowed(ShownEvent event) {
		showWave();
	}
	
	@UiHandler("add")
	void add(ClickEvent event) {
		if(message.getValue().trim().isEmpty()) {
			Window.alert("Wiadomosc nie moze byc pusta");
		} else {
			BlockQuote block = new BlockQuote();
			Paragraph p = new Paragraph();
			p.setText(type.getSelectedItemText() + ": " + message.getText());
			block.add(p);
			
			Small s = new Small();
			s.setText("Jan Nowak dnia 23.03.2015 o godzinie 15:" + Math.round(60 * Math.random()));
			block.add(s);
			blog.add(block);
			type.setSelectedIndex(0);
			message.setText("");
		}
	}

	@Override
	public String getMainTitle() {
		return messages.mainTitle();
	}

	@Override
	public void addAnalysis(IsWidget view) {
		analysisBody.clear();
		analysisBody.add(view);
	}
	
	private void fillInSensors() {
		for(int i = 1; i < 6; i++) {
			sensors.addItem(messages.getSensorLabel(i));
		}
	}

	@Override
	public void addPlot(FlowPanel panel) {
		plot.add(panel);
	}

	@Override
	public void setPresenter(IExperimentPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IExperimentPresenter getPresenter() {
		return presenter;
	}
	
	public native void showWave() /*-{
		new $wnd.Dygraph(
		    $doc.getElementById("wave"),
			    "Czas w godzinach,Poziom wody wału\n" +
			    "0,0.5\n" +
			    "10,3.0\n" +
			    "30,3.0\n" +
			    "40,0.4\n"
		   );
	}-*/;
}