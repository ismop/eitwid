package pl.ismop.web.client.widgets.sideprofile;

import java.util.ArrayList;
import java.util.List;

import thothbot.parallax.core.client.AnimatedScene;
import thothbot.parallax.core.client.RenderingPanel;
import thothbot.parallax.core.shared.cameras.PerspectiveCamera;
import thothbot.parallax.core.shared.core.ExtrudeGeometry;
import thothbot.parallax.core.shared.core.Geometry;
import thothbot.parallax.core.shared.core.Raycaster;
import thothbot.parallax.core.shared.core.Raycaster.Intersect;
import thothbot.parallax.core.shared.curves.Shape;
import thothbot.parallax.core.shared.geometries.CylinderGeometry;
import thothbot.parallax.core.shared.geometries.PlaneGeometry;
import thothbot.parallax.core.shared.lights.PointLight;
import thothbot.parallax.core.shared.materials.LineBasicMaterial;
import thothbot.parallax.core.shared.materials.MeshLambertMaterial;
import thothbot.parallax.core.shared.math.Color;
import thothbot.parallax.core.shared.math.Vector3;
import thothbot.parallax.core.shared.objects.Line;
import thothbot.parallax.core.shared.objects.Mesh;
import thothbot.parallax.core.shared.scenes.FogExp2;

import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;

public class SideProfileScene extends AnimatedScene {
	private Mesh mesh;
	private PerspectiveCamera camera;
	private double mouseX = 0;
	private double mouseY = 0;
	private RenderingPanel renderingPanel;
	private Raycaster raycaster;
	private List<Mesh> sensors;
	
	public SideProfileScene() {
		sensors = new ArrayList<>();
	}

	@Override
	protected void onUpdate(double duration) {
		Vector3 mouse = new Vector3(mouseX, mouseY, 1);
		
		raycaster.set(camera.getPosition(), mouse.sub(camera.getPosition()).normalize());
		List<Raycaster.Intersect> intersects = raycaster.intersectObjects(sensors, false);
		
		if(intersects.size() > 0) {
			for(Intersect intersected : intersects) {
				intersected.object.getMaterial().setOpacity(0.5);
			}
		} else {
			sensors.get(0).getMaterial().setOpacity(1.0);
		}
		
		getRenderer().render(getScene(), camera);
	}

	@Override
	protected void onStart() {
		raycaster = new Raycaster();
		camera = new PerspectiveCamera(70, getRenderer().getAbsoluteAspectRation(), 1, 500);
		camera.setPosition(new Vector3(-70, 70, 100));
		camera.lookAt(new Vector3(-20, 0, 0));
		
//		addAxes();
		FogExp2 fog = new FogExp2(0xbebebe, 0.003);
		getScene().setFog(fog);
		
		PointLight light = new PointLight(0xffffff);
		light.setPosition(new Vector3(0, 100, 100));;
		light.setIntensity(1.2);
		getScene().add(light);
		
		Shape profileSide = new Shape();
		profileSide.moveTo(0, 0);
		profileSide.lineTo(200, 0);
		profileSide.lineTo(125, 50);
		profileSide.lineTo(75, 50);
		profileSide.lineTo(0, 0);
		
		ExtrudeGeometry.ExtrudeGeometryParameters extrudeSettings = new ExtrudeGeometry.ExtrudeGeometryParameters();
		extrudeSettings.amount = 300;
		extrudeSettings.steps = 2;
		extrudeSettings.bevelEnabled = false;
		
		MeshLambertMaterial profileMaterial = new MeshLambertMaterial();
		profileMaterial.setColor(new Color(0xe1b154));
		
		ExtrudeGeometry profile = profileSide.extrude(extrudeSettings);
		mesh = new Mesh(profile, profileMaterial);
		mesh.setPosition(new Vector3(-100, 0, -300));
		getScene().add(mesh);
		
		MeshLambertMaterial planeMaterial = new MeshLambertMaterial();
		planeMaterial.setColor(new Color(0xa2e56d));
		
		Mesh plane = new Mesh(new PlaneGeometry(1000, 1000, 100, 100), planeMaterial);
		plane.getRotation().addX(270 * Math.PI / 180);
		getScene().add(plane);
		
		Shape waterShape = new Shape();
		waterShape.moveTo(200, 0);
		waterShape.lineTo(400, 0);
		waterShape.lineTo(400, 30);
		waterShape.lineTo(150, 30);
		waterShape.lineTo(200, 0);
		
		ExtrudeGeometry water = waterShape.extrude(extrudeSettings);
		MeshLambertMaterial waterMaterial = new MeshLambertMaterial();
		waterMaterial.setColor(new Color(0x3d90dd));
		waterMaterial.setOpacity(0.6);
		
		Mesh waterMesh = new Mesh(water, waterMaterial);
		waterMesh.setPosition(new Vector3(-100, 0, -301));
		getScene().add(waterMesh);
		
		MeshLambertMaterial sensorMaterial = new MeshLambertMaterial();
		sensorMaterial.setColor(new Color(0xf44f4f));
		
		CylinderGeometry sensor = new CylinderGeometry(2, 2, 20, 10, 10);
		Mesh sensorMesh = new Mesh(sensor, sensorMaterial);
		sensorMesh.setPosition(new Vector3(-70, 20, 0));
		sensors.add(sensorMesh);
		getScene().add(sensorMesh);
		
		renderingPanel.getCanvas().addMouseMoveHandler(new MouseMoveHandler() {
			@Override
			public void onMouseMove(MouseMoveEvent event) {
//				mouseX = (((float) event.getX()) / renderingPanel.getOffsetWidth()) * 2 - 1;
//				mouseY = - (((float) event.getY()) / renderingPanel.getOffsetHeight() ) * 2 + 1;
				mouseX = event.getX();
				mouseY = event.getY();
			}
		});
	}

	private void addAxes() {
		float length = 1000;
		getScene().add(createAxis(new Vector3(-length, 0, 0), new Vector3(length, 0, 0), 0xff0000));
		getScene().add(createAxis(new Vector3(0, -length, 0), new Vector3(0, length, 0), 0x00ff00));
		getScene().add(createAxis(new Vector3(0, 0, -length), new Vector3(0, 0, length), 0x0000ff));
	}

	private Line createAxis(Vector3 from, Vector3 to, int color) {
		Geometry geometry = new Geometry();
		geometry.getVertices().add(from);
		geometry.getVertices().add(to);
		
		Line result = new Line(geometry);
		LineBasicMaterial material = new LineBasicMaterial();
		material.setColor(new Color(color));
		result.setMaterial(material);
		
		return result;
	}

	public void setRenderingPanel(RenderingPanel renderingPanel) {
		this.renderingPanel = renderingPanel;
	}
}