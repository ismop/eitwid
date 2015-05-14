package pl.ismop.web.client.widgets.sideprofile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

public class SideProfileView extends Composite implements ISideProfileView, ReverseViewInterface<SideProfilePresenter> {
	private static ProfileViewUiBinder uiBinder = GWT.create(ProfileViewUiBinder.class);
	interface ProfileViewUiBinder extends UiBinder<Widget, SideProfileView> {}
	
	private SideProfilePresenter presenter;
	private JavaScriptObject scene;
	private double mouseX;
	private double mouseY;
	private JavaScriptObject selectedSensor;
	private Map<JavaScriptObject, String> sensorMap;
	private JavaScriptObject sensors;
	private JavaScriptObject name;
	private JavaScriptObject measurement;
	
	@UiField SideProfileViewMessages messages;
	@UiField HTMLPanel panel;

	public SideProfileView() {
		initWidget(uiBinder.createAndBindUi(this));
		sensorMap = new HashMap<>();
		mouseX = 2.0;
		mouseY = 2.0;
	}

	@Override
	public void setScene(String profileName, List<String> sensorIds) {
		if(scene == null) {
			addRenderer(panel.getElement());
		}
		
		setProfileName(messages.profileName(profileName));
		
		for(int i = 0; i < sensorIds.size(); i++) {
			String sensorId = sensorIds.get(i);
			sensorMap.put(addSensor(i, sensorIds.size()), sensorId);
		}
	}

	@Override
	public void setPresenter(SideProfilePresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public SideProfilePresenter getPresenter() {
		return presenter;
	}
	
	@Override
	public void clearSensors() {
		sensorMap.clear();
		removeSensors();
	}
	
	private native void removeSensors() /*-{
		var sensors = this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::sensors;
		
		if(sensors != null) {
			for(var i = 0; i < sensors.length; i++) {
				this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::scene.remove(sensors[i]);
			}
			
			this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::sensors = new $wnd.Array();
		}
	}-*/;

	private native void setProfileName(String profileName) /*-{
		var nameMaterial = new $wnd.THREE.MeshLambertMaterial();
		nameMaterial.color.setHex(0xaaaaaa);
		
		if(this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::name != null) {
			this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::scene.remove(this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::name);
		}
		
		var name = new $wnd.THREE.TextGeometry(profileName, {
			font: 'optimer',
			size: 15,
			height: 1
		});
		var nameMesh = new $wnd.THREE.Mesh(name, nameMaterial);
		this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::name = nameMesh;
		nameMesh.rotation.x = 270 * $wnd.Math.PI / 180;
		nameMesh.position.set(-50, 0, 30);
		this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::scene.add(nameMesh);
	}-*/;
	
	private native JavaScriptObject addSensor(int index, int numberOfSensors) /*-{
		var sensorMaterial = new $wnd.THREE.MeshLambertMaterial();
		sensorMaterial.color.setHex(0xf44f4f);
		
		var height = 0.0;
		var startX = 0.0;
		var numberInRow = 0;
		
		if(index < 5) {
			height = 10;
			startX = -70;
			numberInRow = index;
		} else if(index < 8) {
			height = 25;
			startX = -35;
			numberInRow = index - 5;
		} else if(index < 9) {
			height = 40;
			startX = 0;
			numberInRow = index - 8;
		}
		
		var sensor = new $wnd.THREE.SphereGeometry(2, 12, 12);
		var sensorMesh = new $wnd.THREE.Mesh(sensor, sensorMaterial);
		var distance = startX + 35 * numberInRow;
		sensorMesh.position.set(distance, height, 0);
		this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::sensors.push(sensorMesh);
		
		if(height > 0.0) {
			this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::scene.add(sensorMesh);
		}
		
		return sensorMesh;
	}-*/;

	private native void addRenderer(Element element) /*-{
		var scene = new $wnd.THREE.Scene();
		this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::scene = scene;
		this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::sensors = new $wnd.Array();
		
		var camera = new $wnd.THREE.PerspectiveCamera(70, 800/600, 1, 500);
		camera.position.set(-70, 70, 100);
		camera.lookAt(new $wnd.THREE.Vector3(0, 0, 0));
		
		//this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::addAxes()();
		
		var fog = new $wnd.THREE.FogExp2(0xbebebe, 0.003);
		scene.fog = fog;
		
		var light = new $wnd.THREE.PointLight(0xffffff);
		light.position.set(0, 100, 100);
		light.intensity = 1.2;
		scene.add(light);
		
		var profileSide = new $wnd.THREE.Shape();
		profileSide.moveTo(0, 0);
		profileSide.lineTo(200, 0);
		profileSide.lineTo(125, 50);
		profileSide.lineTo(75, 50);
		profileSide.lineTo(0, 0);
		
		var profileMaterial = new $wnd.THREE.MeshLambertMaterial();
		profileMaterial.color.setHex(0xe1b154);
		profileMaterial.vertexColors = $wnd.THREE.VertexColors;
		
		var profile = profileSide.extrude({
			amount: 300,
			steps: 2,
			bevelEnabled: false
		});
		var vertexIndexes = ['a', 'b', 'c', 'd'];
		for(var i = 0; i < profile.faces.length; i++) {
			var face = profile.faces[i];
			var numberOfSides = (face instanceof $wnd.THREE.Face3) ? 3 : 4;
			for(var j = 0; j < numberOfSides; j++) {
				var vertexIndex = face[vertexIndexes[j]];
				var point = profile.vertices[vertexIndex];
				$wnd.console.log(point);
				if(point.x == 200) {
					face.vertexColors[j] = new $wnd.THREE.Color(0x0049e5);
				} else if(point.x == 125) {
					face.vertexColors[j] = new $wnd.THREE.Color(0xffe51a);
				} else if(point.x == 75) {
					face.vertexColors[j] = new $wnd.THREE.Color(0xffe51a);
				} else {
					face.vertexColors[j] = new $wnd.THREE.Color(0xffe51a);
				}
			}
		}
		mesh = new $wnd.THREE.Mesh(profile, profileMaterial);
		mesh.position.set(-100, 0, -300);
		scene.add(mesh);
		
		var profile2 = profileSide.extrude({
			amount: 1000,
			steps: 2,
			bevelEnabled: false
		});
		mesh2 = new $wnd.THREE.Mesh(profile2, profileMaterial);
		mesh2.position.set(200, 0, -500);
		scene.add(mesh2);
		
		var planeMaterial = new $wnd.THREE.MeshLambertMaterial();
		planeMaterial.color.setHex(0xa2e56d);
		
		var plane = new $wnd.THREE.Mesh(new $wnd.THREE.PlaneGeometry(10000, 10000, 100, 100), planeMaterial);
		plane.rotation.x = 270 * $wnd.Math.PI / 180;
		scene.add(plane);
		
		var waterShape = new $wnd.THREE.Shape();
		waterShape.moveTo(200, 0);
		waterShape.lineTo(400, 0);
		waterShape.lineTo(400, 30);
		waterShape.lineTo(150, 30);
		waterShape.lineTo(200, 0);
		
		var water = waterShape.extrude({
			amount: 300,
			steps: 2,
			bevelEnabled: false
		});
		var waterMaterial = new $wnd.THREE.MeshLambertMaterial({transparent: true});
		waterMaterial.color.setHex(0x3d90dd);
		waterMaterial.opacity = 0.7;
		
		var waterMesh = new $wnd.THREE.Mesh(water, waterMaterial);
		waterMesh.position.set(-100, 0, -301);
		scene.add(waterMesh);

		var renderer = new $wnd.THREE.WebGLRenderer({antialias: true});
		renderer.setSize(800, 600);
		renderer.setClearColor(0xbad7f3);
		element.appendChild(renderer.domElement);
		
		var raycaster = new $wnd.THREE.Raycaster();
		
		var object = this;
		element.addEventListener('mousemove', function(event) {
			object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::mouseX = ((event.clientX - element.getBoundingClientRect().left) / 800 ) * 2 - 1;
			object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::mouseY = - ((event.clientY - element.getBoundingClientRect().top) / 600 ) * 2 + 1;
		}, false);
		
		var diff = -0.05;

		var render = function() {
			if(object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::mouseX < 2.0 &&
					object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::mouseY < 2.0) {
				raycaster.setFromCamera(new $wnd.THREE.Vector2(
					object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::mouseX,
					object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::mouseY
				), camera);
			}
			
			var intersects = raycaster.intersectObjects(object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::sensors);
			
			if(intersects.length > 0) {
				if(object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::selectedSensor != null) {
					if(object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::selectedSensor != intersects[0].object) {
						object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::selectedSensor.material.transparent = false;
						object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::selectedSensor.material.opacity = 1.0;
						object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::sensorSelected(Z)(false);
						object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::selectedSensor = intersects[0].object;
						object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::sensorSelected(Z)(true);
						intersects[0].object.material.transparent = true;
						intersects[0].object.material.opacity = 0.7;
					}
				} else {
					object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::selectedSensor = intersects[0].object;
					intersects[0].object.material.transparent = true;
					intersects[0].object.material.opacity = 0.7;
					object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::sensorSelected(Z)(true);
				}
			} else {
				if(object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::selectedSensor != null) {
					object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::selectedSensor.material.transparent = false;
					object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::selectedSensor.material.opacity = 1.0;
					object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::sensorSelected(Z)(false);
					object.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::selectedSensor = null;
				}
			}
			
			if(waterMesh.position.y > 0) {
				diff = -0.05;
			}
			
			if(waterMesh.position.y < -29.0) {
				diff = 0.05;
			}
			
			waterMesh.position.y += diff;
			
			$wnd.requestAnimationFrame(render);
			renderer.render(scene, camera);
		};

		render();
	}-*/;
	
	private native void addAxes() /*-{
		var createAxis = function(from, to, color) {
			var geometry = new $wnd.THREE.Geometry();
			geometry.vertices.push(from);
			geometry.vertices.push(to);
			
			var material = new $wnd.THREE.LineBasicMaterial();
			material.color.setHex(color);

			var result = new $wnd.THREE.Line(geometry);
			result.material = material;
			
			return result;
		};
		
		var length = 1000;
		this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::scene.add(
			createAxis(new $wnd.THREE.Vector3(-length, 0, 0), new $wnd.THREE.Vector3(length, 0, 0), 0xff0000));
		this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::scene.add(
			createAxis(new $wnd.THREE.Vector3(0, -length, 0), new $wnd.THREE.Vector3(0, length, 0), 0x00ff00));
		this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::scene.add(
			createAxis(new $wnd.THREE.Vector3(0, 0, -length), new $wnd.THREE.Vector3(0, 0, length), 0x0000ff));
	}-*/;
	
	private void sensorSelected(boolean selected) {
		getPresenter().onSensorSelected(sensorMap.get(selectedSensor), selected);
	}

	@Override
	public native void showMeasurement(String measurement) /*-{
		this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::removeMeasurement()();
		
		var measurementMaterial = new $wnd.THREE.MeshLambertMaterial();
		measurementMaterial.color.setHex(0x555555);
		
		var measurement = new $wnd.THREE.TextGeometry(measurement, {
			font: 'optimer',
			size: 7,
			height: 0.5
		});
		var measurementMesh = new $wnd.THREE.Mesh(measurement, measurementMaterial);
		this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::measurement = measurementMesh;
		measurementMesh.rotation.x = 270 * $wnd.Math.PI / 180;
		measurementMesh.position.set(-50, 0, 45);
		this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::scene.add(measurementMesh);
	}-*/;

	@Override
	public native void removeMeasurement() /*-{
		if(this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::measurement != null) {
			this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::scene.remove(
					this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::measurement);
			this.@pl.ismop.web.client.widgets.sideprofile.SideProfileView::measurement = null;
		}
	}-*/;

	@Override
	public String getNoMeasurementLabel() {
		return messages.noMeasurementLabel();
	}
}