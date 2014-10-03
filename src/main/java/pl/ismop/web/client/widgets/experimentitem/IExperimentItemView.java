package pl.ismop.web.client.widgets.experimentitem;

import java.util.Date;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface IExperimentItemView extends IsWidget {
	public interface IExperimentItemPresenter {
		void onShowResults();
	}

	HasText getName();
	void setStatus(String status);
	void setStartDate(Date date);
	void setEndDate(Date date);
	String getSimilarityLabel();
	String getProfileIdLabel();
	String getNoResultsMessage();
}