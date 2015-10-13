package pl.ismop.web.client.widgets.common.profile;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.util.CoordinatesUtil;
import pl.ismop.web.client.widgets.common.profile.ISideProfileView.ISideProfilePresenter;

@Presenter(view = SideProfileView.class, multiple = true)
public class SideProfilePresenter extends BasePresenter<ISideProfileView, MainEventBus> implements ISideProfilePresenter {
	private static final double PROFILE_HEIGHT = 4.5;
	
	private static final double PROFILE_TOP_WIDTH = 4;
	
	private String selectedSensorId;
	
	private int width, height;
	
	private List<String> hoveredDeviceIds;

	private CoordinatesUtil coordinatesUtil;
	
	@Inject
	public SideProfilePresenter(CoordinatesUtil coordinatesUtil) {
		this.coordinatesUtil = coordinatesUtil;
		hoveredDeviceIds = new ArrayList<>();
	}
	
	public void setProfileAndDevices(Profile profile, List<Device> devices) {
		view.setScene(profile.getId(), width, height);
		
		List<List<Double>> fullProfile = createFullProfile(coordinatesUtil.projectCoordinates(profile.getShape().getCoordinates()));
		boolean leftBank = true;

		//TODO: come up with a better way of telling where the water is
		if(profile.getShape().getCoordinates().get(0).get(0) > 19.676851838778) {
			leftBank = false;
		}
		
		double xShift = -fullProfile.get(1).get(0) / 2;
		view.drawProfile(fullProfile, leftBank, xShift);
		
		Map<List<String>, List<Double>> devicePositions = collectDevicePositions(getReferencePoint(profile.getShape().getCoordinates()), devices);
		view.drawDevices(devicePositions, xShift);
	}

	public void onDeviceSelected(final List<String> deviceIds, boolean selected) {
		hoveredDeviceIds.clear();
		
		if(selected) {
			hoveredDeviceIds.addAll(deviceIds);
		}
		
		eventBus.devicesHovered(deviceIds, selected);
	}

	public void setWidthAndHeight(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void markDevice(String deviceId, boolean mark) {
		view.markDevice(deviceId, mark);
	}

	public void clear() {
		view.removeObjects();
	}

	@Override
	public void onBack() {
		eventBus.backFromSideProfile();
	}

	@Override
	public void onMouseClicked() {
		if(!hoveredDeviceIds.isEmpty()) {
			eventBus.devicesClicked(hoveredDeviceIds);
		}
	}

	private List<Double> getReferencePoint(List<List<Double>> coordinates) {
		Collections.sort(coordinates, new Comparator<List<Double>>() {
			@Override
			public int compare(List<Double> o1, List<Double> o2) {
				return o1.get(0).compareTo(o2.get(0));
			}
		});
		
		return coordinates.get(0);
	}

	private Map<List<String>, List<Double>> collectDevicePositions(List<Double> referencePoint, List<Device> devices) {
		Map<List<String>, List<Double>> result = new HashMap<>();
		List<List<Double>> referenceSource = new ArrayList<>();
		referenceSource.add(referencePoint);
		
		List<Double> projectedReference = coordinatesUtil.projectCoordinates(referenceSource).get(0);
		Map<List<String>, List<List<Double>>> similar = findSimilar(devices);
		
		for(List<String> similarDeviceIds : similar.keySet()) {
			List<List<Double>> projectedValues = coordinatesUtil.projectCoordinates(similar.get(similarDeviceIds));
			double distance = sqrt(
				pow(projectedValues.get(0).get(0) - projectedReference.get(0), 2) +
				pow(projectedValues.get(0).get(1) - projectedReference.get(1), 2)
			);
			
			//for now let's position the devices half meter above ground
			result.put(similarDeviceIds, Arrays.asList(distance, 0.5));
		}
		
		return result;
	}

	private Map<List<String>, List<List<Double>>> findSimilar(List<Device> devices) {
		Map<String, List<Device>> similar = new HashMap<>();
		
		for(Device device : devices) {
			String key = String.valueOf(device.getPlacement().getCoordinates().get(0)) + String.valueOf(device.getPlacement().getCoordinates().get(1));
			
			if(!similar.containsKey(key)) {
				similar.put(key, new ArrayList<Device>());
			}
			
			similar.get(key).add(device);
		}
		
		Map<List<String>, List<List<Double>>> result = new HashMap<>();
		
		for(String key : similar.keySet()) {
			List<Device> similarDevices = similar.get(key);
			List<String> ids = new ArrayList<>();
			
			for(Device device : similarDevices) {
				ids.add(device.getId());
			}
			
			List<List<Double>> packagedCoordinates = new ArrayList<>();
			packagedCoordinates.add(similarDevices.get(0).getPlacement().getCoordinates());
			result.put(ids, packagedCoordinates);
		}
		
		return result;
	}

	private List<List<Double>> createFullProfile(List<List<Double>> profile) {
		List<List<Double>> result = new ArrayList<>();
		double length = sqrt(
			pow(profile.get(0).get(0) - profile.get(1).get(0), 2) +
			pow(profile.get(0).get(1) - profile.get(1).get(1), 2)
		);
		result.add(asList(new Double[] {0.0, 0.0}));
		result.add(asList(new Double[] {length, 0.0}));
		result.add(asList(new Double[] {(length - PROFILE_TOP_WIDTH) / 2, PROFILE_HEIGHT}));
		result.add(asList(new Double[] {(length - PROFILE_TOP_WIDTH) / 2 + PROFILE_TOP_WIDTH, PROFILE_HEIGHT}));
		
		return result;
	}
}