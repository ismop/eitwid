package pl.ismop.web.client.widgets.experiment;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gwtbootstrap3.client.shared.event.ShownEvent;
import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Small;

import pl.ismop.web.client.widgets.experiment.IExperimentView.IExperimentPresenter;

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

public class ExperimentView extends Composite implements IExperimentView, ReverseViewInterface<IExperimentPresenter> {
	private static ExperimentViewUiBinder uiBinder = GWT.create(ExperimentViewUiBinder.class);
	interface ExperimentViewUiBinder extends UiBinder<Widget, ExperimentView> {}
	
	private IExperimentPresenter presenter;
	private Map<Integer, Double> levels;
	
	@UiField ExperimentMessages messages;
	@UiField PanelBody analysisBody;
	@UiField ListBox sensors;
	@UiField FlowPanel plot;
	@UiField FlowPanel blog;
	@UiField ListBox type;
	@UiField TextBox message;
	@UiField FlowPanel wave;
	@UiField TextBox time;
	@UiField TextBox height;

	public ExperimentView() {
		initWidget(uiBinder.createAndBindUi(this));
		fillInSensors();
		plot.getElement().setAttribute("id", "singlePlot");
		wave.getElement().setAttribute("id", "wave");
		levels = new LinkedHashMap<>();
	}
	
	@UiHandler("addPoint")
	void addPoint(ClickEvent event) {
		addLevelPoint(Integer.parseInt(time.getValue()), Double.parseDouble(height.getValue()));
		time.setValue("");
		height.setValue("");
	}
	
	@UiHandler("removePoint")
	void removePoint(ClickEvent event) {
		Integer last = null;
		
		for(Integer key : levels.keySet()) {
			last = key;
		}
		
		if(last != null && levels.size() > 0) {
			levels.remove(last);
		}
		
		regenerateWave(createData());
	}
	
	private void addLevelPoint(int hours, double level) {
		Integer key = hours;
		levels.put(key, level);
		regenerateWave(createData());
	}

	private String createData() {
		String data = "Czas w godzinach,Planowana fala,Rzeczywisty poziom\n";
		
		for(Integer h : levels.keySet()) {
			double real = levels.get(h) + Math.random() * 0.4 - 0.2;
			data += "" + h + "," + levels.get(h) + "," + real + "\n";
		}
		
		return data;
	}

	private native void regenerateWave(String data) /*-{
		new $wnd.Dygraph(
		    $doc.getElementById("wave"),
			    data, {
			    	ylabel: "Poziom wody, m",
			    	xlabel: "Czas, h"
			    }
		   );
	}-*/;

	@UiHandler("plotCollapse")
	void plotShowed(ShownEvent event) {
		getPresenter().showPlot();
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
	
	@Override
	public void showWave() {
		addLevelPoint(0, 0.5);
		addLevelPoint(10, 3.0);
		addLevelPoint(30, 3.0);
		addLevelPoint(40, 0.4);
	};
}