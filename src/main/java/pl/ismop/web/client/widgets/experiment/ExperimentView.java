package pl.ismop.web.client.widgets.experiment;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.experiment.IExperimentView.IExperimentPresenter;

public class ExperimentView extends Composite implements IExperimentView, ReverseViewInterface<IExperimentPresenter> {
	private static ExperimentViewUiBinder uiBinder = GWT.create(ExperimentViewUiBinder.class);
	interface ExperimentViewUiBinder extends UiBinder<Widget, ExperimentView> {}
	
	private IExperimentPresenter presenter;
	
	private Map<Integer, Double> levels;
	
	private String currentExperiment;
	
	private boolean pompsActive;
	
	private Map<Integer, Double> secondPomp;
	
	@UiField ExperimentMessages messages;
	
	@UiField FlowPanel wave;
	
	@UiField TextBox time;
	
	@UiField TextBox height;
	
	@UiField Modal modal;

	public ExperimentView() {
		initWidget(uiBinder.createAndBindUi(this));
		wave.getElement().setAttribute("id", "wave");
		levels = new LinkedHashMap<>();
		currentExperiment = "1";
		secondPomp = new LinkedHashMap<>();
	}
	
	@UiHandler("addPoint")
	void addPoint(ClickEvent event) {
		getPresenter().addChartPoint(Integer.parseInt(time.getValue()), Double.parseDouble(height.getValue()));
		time.setValue("");
		height.setValue("");
	}
	
	@UiHandler("removePoint")
	void removePoint(ClickEvent event) {
		getPresenter().removeLastPoint();
	}
	
	@UiHandler("save")
	void save(ClickEvent event) {
		Window.alert("To be implemented");
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

	@Override
	public String getMainTitle() {
		return messages.mainTitle();
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
	
	@Override
	public void showModal(boolean show) {
		modal.show();
	}

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

	@Override
	public void setChart(Chart chart) {
		wave.add(chart);
	}
}