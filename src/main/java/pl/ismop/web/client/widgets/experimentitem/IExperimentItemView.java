package pl.ismop.web.client.widgets.experimentitem;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface IExperimentItemView extends IsWidget {
	public interface IExperimentItemPresenter {
		void onShowSection(String sectionId);
	}

	HasText getName();
	void setStatus(String status);
	void setDates(Date start, Date end);
	String getSimilarityLabel();
	String getProfileIdLabel();
	String getNoResultsMessage();
	void showNoResultsLabel();
	void showResultsLabel();
	void addResultItem(Map<String, String> threatLevels);
}