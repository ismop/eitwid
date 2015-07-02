package pl.ismop.web.client.widgets.section;

import com.google.gwt.user.client.ui.IsWidget;

public interface ISectionView extends IsWidget {
	interface ISectionPresenter {
		
	}

	void setHeader(String name);
	
	void setModeStyle(String panelStyle);
	
	void setThreatLevel(String threatLevel);
	
	String getHeaderLabel(String sectionName);
}