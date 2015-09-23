package pl.ismop.web.client.widgets.monitoring.readings;

import java.util.List;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.monitoring.readings.IReadingsView.IReadingsPresenter;

@Presenter(view = ReadingsView.class)
public class ReadingsPresenter extends BasePresenter<IReadingsView, MainEventBus> implements IReadingsPresenter {
	private DapController dapController;
	private MapPresenter mapPresenter;
	private Levee levee;
	private List<ChartSeries> series;
	private ChartPresenter chartPresenter ;

	@Inject
	public ReadingsPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onShowExpandedReadings(Levee levee, List<ChartSeries> series) {
		this.levee = levee;
		this.series = series;
		view.showModal(true);
	}

	@Override
	public void onModalShown() {
		if(mapPresenter == null) {
			mapPresenter = eventBus.addHandler(MapPresenter.class);
			view.setMap(mapPresenter.getView());
		}
		
		mapPresenter.reset(false);
		dapController.getSections(levee.getId(), new SectionsCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processSections(List<Section> sections) {
				for(Section section : sections) {
					mapPresenter.addSection(section);
				}
			}
		});
		
		if(chartPresenter == null) {
			chartPresenter = eventBus.addHandler(ChartPresenter.class);
			view.setChart(chartPresenter.getView());
		}
		
		chartPresenter.reset();
		
		for(ChartSeries chartSeries : series) {
			chartPresenter.addChartSeries(chartSeries);
		}
	}
}