package pl.ismop.web.client.widgets.monitoring.waterheight;

import com.google.gwt.user.client.ui.IsWidget;

public interface IWaterHeightView extends IsWidget {
	interface IWaterHeightPresenter {
		void onModalReady();
	}
	
	void showModal(boolean show);
	
	void setChart(IsWidget chart);

	int getChartHeight();
}
