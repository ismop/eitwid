package pl.ismop.web.client.widgets.monitoring.sidepanel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveesCallback;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.monitoring.sidepanel.IMonitoringSidePanel.IMonitoringSidePanelPresenter;

@Presenter(view = MonitoringSidePanelView.class, multiple = true)
public class MonitoringSidePanelPresenter extends BasePresenter<IMonitoringSidePanel, MainEventBus> implements IMonitoringSidePanelPresenter {
	private DapController dapController;
	private Levee selectedLevee;

	@Inject
	public MonitoringSidePanelPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onLeveeNavigatorReady() {
		if(selectedLevee != null) {
			eventBus.leveeSelected(selectedLevee);
		}
	}
	
	public void onShowProfileMetadata(Profile profile, boolean show) {
		view.clearMetadata();
		
		if(show) {
			view.addMetadata(view.getInternalIdLabel(), profile.getId());
		}
	}
	
	public void onShowSectionMetadata(Section section, boolean show) {
		view.clearMetadata();
		
		if(show) {
			view.addMetadata(view.getInternalIdLabel(), section.getId());
			view.addMetadata(view.getNameLabel(), section.getName());
		}
	}
	
	public void onShowDeviceMetadata(Device device, boolean show) {
		view.clearMetadata();
		
		if(show) {
			view.addMetadata(view.getInternalIdLabel(), device.getId());
			view.addMetadata(view.getNameLabel(), device.getCustomId());
		}
	}
	
	public void onDeviceSelected(Device device, boolean selected) {
		//TODO
		
		final Chart chart = new Chart()  
	            .setType(Series.Type.LINE)  
	            .setMarginRight(130)  
	            .setMarginBottom(25)   
	            .setLegend(new Legend()  
	                .setLayout(Legend.Layout.VERTICAL)  
	                .setAlign(Legend.Align.RIGHT)  
	                .setVerticalAlign(Legend.VerticalAlign.TOP)  
	                .setX(-10)  
	                .setY(100)  
	                .setBorderWidth(0)  
	            )  
	            .setToolTip(new ToolTip()  
	                .setFormatter(new ToolTipFormatter() {  
	                    public String format(ToolTipData toolTipData) {  
	                        return "<b>" + toolTipData.getSeriesName() + "</b><br/>" +  
	                            toolTipData.getXAsString() + ": " + toolTipData.getYAsDouble() + "Â°C";  
	                    }  
	                })  
	            );  
	  
	        chart.getXAxis()  
	            .setCategories(  
	                "Jan", "Feb", "Mar", "Apr", "May", "Jun",  
	                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"  
	            );  
	  
	        chart.getYAxis()  
	            .setAxisTitleText("Temperature Â°C");  
	  
	        chart.addSeries(chart.createSeries()  
	            .setName("Tokyo")  
	            .setPoints(new Number[]{  
	                7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6  
	            })  
	        );
	        chart.setHeight(view.getChartContainerHeight());
	        
	        view.setChart(chart);
	        view.showChartExpandButton(true);
	}

	public void reset() {
		view.showLeveeName(false);
		view.showLeveeList(false);
		view.showLeveeProgress(true);
		loadLevees();
	}

	@Override
	public void handleShowFibreClick() {
		eventBus.showFibrePanel(selectedLevee);
	}

	@Override
	public void handleShowWeatherClick() {
		eventBus.showWeatherPanel();
	}

	private void loadLevees() {
		dapController.getLevees(new LeveesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				view.showLeveeProgress(false);
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processLevees(List<Levee> levees) {
				view.showLeveeProgress(false);
				
				if(levees.size() > 0) {
					if(levees.size() == 1) {
						view.setLeveeName(levees.get(0).getName());
						view.showLeveeName(true);
					} else {
						Collections.sort(levees, new Comparator<Levee>() {
							@Override
							public int compare(Levee o1, Levee o2) {
								return o1.getName().compareTo(o2.getName());
							}
						});
						
						for(Levee levee : levees) {
							view.addLeveeOption(levee.getId(), levee.getName());
						}
					}
					
					selectedLevee = levees.get(0);
					eventBus.leveeSelected(selectedLevee);
				} else {
					view.showNoLeveesMessage();
				}
			}
		});
	}
}