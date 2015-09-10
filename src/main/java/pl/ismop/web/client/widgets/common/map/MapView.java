package pl.ismop.web.client.widgets.common.map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class MapView extends Composite implements IMapView {
	private static MapViewUiBinder uiBinder = GWT.create(MapViewUiBinder.class);

	interface MapViewUiBinder extends UiBinder<Widget, MapView> {}
	
	private String elementId;

	@UiField
	HTMLPanel panel;
	
	public MapView() {
		initWidget(uiBinder.createAndBindUi(this));
		
		elementId = "map-" + hashCode();
		panel.getElement().setAttribute("id", elementId);
	}
}