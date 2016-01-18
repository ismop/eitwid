package pl.ismop.web.client.widgets.root;

import com.google.gwt.i18n.client.Messages;

public interface RootPanelMessages extends Messages {
	String logoutLabel();
	
	String monitoringLabel();
	
	String analysisLabel();
	
	String brokenDevicesLabel(int numberOfBrokenDevices);

	String brokenParametersDetails(String string);
	
	String realTimeLabel();
}