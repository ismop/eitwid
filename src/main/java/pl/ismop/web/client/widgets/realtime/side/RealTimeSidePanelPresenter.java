package pl.ismop.web.client.widgets.realtime.side;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.geojson.MapFeature;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.common.refresher.RefresherPresenter;
import pl.ismop.web.client.widgets.common.refresher.RefresherPresenter.Event;
import pl.ismop.web.client.widgets.realtime.side.IRealTimeSidePanelView.IRealTimeSidePanelPresenter;

@Presenter(view = RealTimeSidePanelView.class, multiple = true)
public class RealTimeSidePanelPresenter extends BasePresenter<IRealTimeSidePanelView, MainEventBus>
		implements IRealTimeSidePanelPresenter {
	private MapPresenter mapPresenter;

	private DapController dapController;

	private List<MapFeature> featureBuffer;

	private RefresherPresenter refresher;

	@Inject
	public RealTimeSidePanelPresenter(DapController dapController) {
		this.dapController = dapController;
		featureBuffer = new ArrayList<>();
	}

	public void init() {
		if (mapPresenter == null) {
			addMap();

			if (featureBuffer.size() > 0) {
				for (MapFeature feature : featureBuffer) {
					mapPresenter.add(feature);
				}

				featureBuffer.clear();
			}
		}

		if (refresher == null) {
			refresher = eventBus.addHandler(RefresherPresenter.class);
			view.setProegressView(refresher.getView());
			refresher.setEvent(new Event() {
				@Override
				public void refresh() {
					eventBus.refreshRealTimePanel();
				}

			});
		}
	}

	public void onAddDeviceToRealtimeMap(Device device) {
		if (mapPresenter != null) {
			mapPresenter.add(device);
		} else {
			featureBuffer.add(device);
		}
	}

	public void onSelectDeviceOnRealtimeMap(Device device, boolean select) {
		if (select) {
			mapPresenter.select(device);
		} else {
			mapPresenter.unselect(device);
		}
	}

	public void onRemoveProfileFromRealtimeMap(Profile profile) {
		mapPresenter.rm(profile);
	}

	public void onAddProfileFromRealtimeMap(Profile profile) {
		mapPresenter.add(profile);
	}

	public void onAppVisibilityChange(boolean hidden) {
		refresher.pause(hidden);
	}

	public void initializeTimer() {
		refresher.initializeTimer();
	}

	public void onRealDataContentLoaded() {
		initializeTimer();
	}

	public void disableTimers() {
		refresher.disableTimers();
	}

	private void addMap() {
		mapPresenter = eventBus.addHandler(MapPresenter.class);
		view.setMapView(mapPresenter.getView());
		dapController.getSections(new SectionsCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}

			@Override
			public void processSections(List<Section> sections) {
				for (Section section : sections) {
					mapPresenter.add(section);
				}

				mapPresenter.redrawMap();
			}
		});
	}
}
