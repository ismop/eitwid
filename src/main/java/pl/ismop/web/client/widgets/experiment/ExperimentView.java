package pl.ismop.web.client.widgets.experiment;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gwtbootstrap3.client.shared.event.ShownEvent;
import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Small;

import pl.ismop.web.client.widgets.experiment.IExperimentView.IExperimentPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
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
	private String currentExperiment;
	private boolean pompsActive;
	private Map<Integer, Double> secondPomp;
	
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
	@UiField ListBox experimentSelector;
	@UiField Button showPomps;

	public ExperimentView() {
		initWidget(uiBinder.createAndBindUi(this));
		fillInSensors();
		plot.getElement().setAttribute("id", "singlePlot");
		wave.getElement().setAttribute("id", "wave");
		levels = new LinkedHashMap<>();
		currentExperiment = "1";
		secondPomp = new LinkedHashMap<>();
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
	
	@UiHandler("experimentSelector")
	void experimentSelected(ChangeEvent event) {
		currentExperiment = experimentSelector.getSelectedValue();
		checkGraph();
	}
	
	@UiHandler("showPomps")
	void showPompsClicked(ClickEvent event) {
		if(showPomps.isActive()) {
			pompsActive = false;
		} else {
			pompsActive = true;
		}
		
		checkGraph();
	}

	private void checkGraph() {
		if(pompsActive) {
			switch(currentExperiment) {
				case "1":
					removeAllPoints();
					showFirstPomps();
				break;
				case "2":
					removeAllPoints();
					showSecondPomps();
				break;
				case "3":
					removeAllPoints();
					showThirdPomps();
			}
		} else {
			switch(currentExperiment) {
			case "1":
				removeAllPoints();
				showFirstWave();
			break;
			case "2":
				removeAllPoints();
				showSecondWave();
			break;
			case "3":
				removeAllPoints();
				showThirdWave();
		}
		}
	}

	private void showFirstPomps() {
		secondPomp.put(0, 0.0);
		addLevelPoint(0, 0.0);
		secondPomp.put(1, 200.0);
		addLevelPoint(1, 100.0);
		secondPomp.put(35, 200.0);
		addLevelPoint(35, 100.0);
		secondPomp.put(36, 0.0);
		addLevelPoint(36, 0.0);
	}

	private void showSecondPomps() {
		secondPomp.put(0, 0.0);
		addLevelPoint(0, 0.0);
		secondPomp.put(1, 123.0);
		addLevelPoint(1, 100.0);
		secondPomp.put(35, 123.0);
		addLevelPoint(35, 100.0);
		secondPomp.put(36, 0.0);
		addLevelPoint(36, 0.0);
	}

	private void showThirdPomps() {
		secondPomp.put(0, 0.0);
		addLevelPoint(0, 0.0);
		secondPomp.put(1, 200.0);
		addLevelPoint(1, 100.0);
		secondPomp.put(35, 200.0);
		addLevelPoint(35, 100.0);
		secondPomp.put(36, 0.0);
		addLevelPoint(36, 0.0);
	}

	private void removeAllPoints() {
		levels.clear();
		secondPomp.clear();
		regenerateWave(createData());
	}

	private void addLevelPoint(int hours, double level) {
		Integer key = hours;
		levels.put(key, level);
		regenerateWave(createData());
	}

	private String createData() {
		String data = null;
		
		if(pompsActive) {
			data = "Czas w godzinach,Wydajność pompy 100 m^3/h,Wydajność pompy 200 m^3/h\n";
		} else {
			data = "Czas w godzinach,Planowana fala,Rzeczywisty poziom\n";
		}
		
		
		if(pompsActive) {
			Iterator<Integer> i1 = levels.keySet().iterator();
			Iterator<Integer> i2 = secondPomp.keySet().iterator();
			
			while(i1.hasNext() && i2.hasNext()) {
				Integer key1 = i1.next();
				Integer key2 = i2.next();
				double v1 = levels.get(key1);
				double v2 = secondPomp.get(key2);
				data += "" + key1 + "," + v1 + "," + v2 + "\n";
			}
		} else {
			for(Integer h : levels.keySet()) {
				double real = levels.get(h) + Math.random() * 0.4 - 0.2;
				data += "" + h + "," + levels.get(h) + "," + real + "\n";
			}
		}
		
		return data;
	}

	private native void regenerateWave(String data) /*-{
		var x = null;
		var y = null;
		
		if(this.@pl.ismop.web.client.widgets.experiment.ExperimentView::pompsActive) {
			y = "Wydajność pompy, m^3/h";
			x = "Czas, h";
		} else {
			y = "Poziom wody, m";
			x = "Czas, h";
		}
		
		new $wnd.Dygraph(
		    $doc.getElementById("wave"),
			    data, {
			    	ylabel: y,
			    	xlabel: x
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
	public void showFirstWave() {
		addLevelPoint(0, 0.0);
		addLevelPoint(36, 4.0);
		addLevelPoint(108, 0.0);
	};
	
	private void showSecondWave() {
		addLevelPoint(0, 0.0);
		addLevelPoint(48, 4.0);
		addLevelPoint(96, 4.0);
		addLevelPoint(216, 0.0);
	}

	private void showThirdWave() {
		addLevelPoint(0, 0.0);
		addLevelPoint(36, 4.0);
		addLevelPoint(156, 4.0);
		addLevelPoint(228, 0.0);
	}
}