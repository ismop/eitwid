package pl.ismop.web.client.widgets.analysis.horizontalslice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class HorizontalSliceView extends Composite implements IHorizontalSliceView {
	private static HorizontalSliceViewUiBinder uiBinder = GWT.create(HorizontalSliceViewUiBinder.class);

	interface HorizontalSliceViewUiBinder extends UiBinder<Widget, HorizontalSliceView> {}
	
	private JavaScriptObject scene, meshes;

	@UiField
	HorizontalSliceMessages messages;
	
	@UiField
	FlowPanel loadingPanel, panel;
	
	public HorizontalSliceView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void showLoadingState(boolean show) {
		loadingPanel.setVisible(show);
	}

	@Override
	public void drawCrosssection(String parameterUnit, double minValue, double maxValue,
			Map<List<List<Double>>, Map<List<Double>, Double>> locationsWithValues) {
		int topColor = 0xecf330;
		int bottomColor = 0x307bf3;
		drawLegend(topColor, bottomColor, minValue, maxValue, parameterUnit);
		drawDevices(locationsWithValues);
		
		for(List<List<Double>> sectionCorners : locationsWithValues.keySet()) {
			@SuppressWarnings("unchecked")
			JsArray<JsArrayNumber> coordinatesAndValues = (JsArray<JsArrayNumber>) JsArray.createArray();
			List<Double> topLeftCorner = sectionCorners.get(0);
			List<Double> topRightCorner = sectionCorners.get(1);
			Iterator<List<Double>> iterator = locationsWithValues.get(sectionCorners).keySet().iterator();
			Double previousValue = null; 

			while(iterator.hasNext()) {
				List<Double> next = iterator.next();
				List<Double> bottomLeftCorner = calculateCorner(sectionCorners.get(0), sectionCorners.get(1), next, sectionCorners.get(0));
				List<Double> bottomRightCorner = calculateCorner(sectionCorners.get(0), sectionCorners.get(1), next, sectionCorners.get(1));
				JsArrayNumber topLeft = (JsArrayNumber) JsArrayNumber.createArray();
				topLeft.push(topLeftCorner.get(0));
				topLeft.push(topLeftCorner.get(1));
				coordinatesAndValues.push(topLeft);
				
				JsArrayNumber topRight = (JsArrayNumber) JsArrayNumber.createArray();
				topRight.push(topRightCorner.get(0));
				topRight.push(topRightCorner.get(1));
				coordinatesAndValues.push(topRight);
				
				JsArrayNumber bottomRight = (JsArrayNumber) JsArrayNumber.createArray();
				bottomRight.push(bottomRightCorner.get(0));
				bottomRight.push(bottomRightCorner.get(1));
				coordinatesAndValues.push(bottomRight);
				
				JsArrayNumber bottomLeft = (JsArrayNumber) JsArrayNumber.createArray();
				bottomLeft.push(bottomLeftCorner.get(0));
				bottomLeft.push(bottomLeftCorner.get(1));
				coordinatesAndValues.push(bottomLeft);
				
				JsArrayNumber values = (JsArrayNumber) JsArrayNumber.createArray();
				values.push(previousValue == null ? locationsWithValues.get(sectionCorners).get(next) : previousValue);
				values.push(locationsWithValues.get(sectionCorners).get(next));
				coordinatesAndValues.push(values);
				
				topLeftCorner = bottomLeftCorner;
				topRightCorner = bottomRightCorner;
				previousValue = locationsWithValues.get(sectionCorners).get(next);
			}
			
			JsArrayNumber topLeft = (JsArrayNumber) JsArrayNumber.createArray();
			topLeft.push(topLeftCorner.get(0));
			topLeft.push(topLeftCorner.get(1));
			coordinatesAndValues.push(topLeft);
			
			JsArrayNumber topRight = (JsArrayNumber) JsArrayNumber.createArray();
			topRight.push(topRightCorner.get(0));
			topRight.push(topRightCorner.get(1));
			coordinatesAndValues.push(topRight);
			
			JsArrayNumber bottomRight = (JsArrayNumber) JsArrayNumber.createArray();
			bottomRight.push(sectionCorners.get(2).get(0));
			bottomRight.push(sectionCorners.get(2).get(1));
			coordinatesAndValues.push(bottomRight);
			
			JsArrayNumber bottomLeft = (JsArrayNumber) JsArrayNumber.createArray();
			bottomLeft.push(sectionCorners.get(3).get(0));
			bottomLeft.push(sectionCorners.get(3).get(1));
			coordinatesAndValues.push(bottomLeft);
			
			JsArrayNumber values = (JsArrayNumber) JsArrayNumber.createArray();
			values.push(previousValue);
			values.push(previousValue);
			coordinatesAndValues.push(values);
			
			drawHeatSection(coordinatesAndValues, topColor, bottomColor, minValue, maxValue);
		}
	};
	
	@Override
	public void drawMuteSections(List<List<List<Double>>> coordinates) {
		for(List<List<Double>> sectionCoordinates : coordinates) {
			@SuppressWarnings("unchecked")
			JsArray<JsArrayNumber> nativeCoordinates = (JsArray<JsArrayNumber>) JsArray.createArray();
			
			for(List<Double> pointCoordinates : sectionCoordinates) {
				JsArrayNumber nativePointCoordinates = (JsArrayNumber) JsArray.createArray();
				
				for(Double coordinate : pointCoordinates) {
					nativePointCoordinates.push(coordinate);
				}
				
				nativeCoordinates.push(nativePointCoordinates);
			}
			
			drawSection(nativeCoordinates);
		}
	};
	
	@Override
	public int getWidth() {
		return panel.getOffsetWidth();
	}

	@Override
	public int getHeight() {
		return panel.getOffsetHeight();
	}

	@Override
	public void showNoMeasurementsMessage() {
		Window.alert(messages.noMeasurementsMessage());
	}

	@Override
	public void init() {
		if(scene == null) {
			addRenderer(panel.getElement(), panel.getOffsetWidth(), panel.getOffsetHeight());
		}
	}

	@Override
	public native void drawScale(double scale, double panX) /*-{
		var material = new $wnd.THREE.LineBasicMaterial({
			color: 0x0000ff
		});
		
		var geometry = new $wnd.THREE.Geometry();
		geometry.vertices.push(
			new $wnd.THREE.Vector3(panX, 5, 0),
			new $wnd.THREE.Vector3(panX, 2, 0),
			new $wnd.THREE.Vector3(panX + scale * 10, 2, 0),
			new $wnd.THREE.Vector3(panX + scale * 10, 5, 0)
		);
		
		var line = new $wnd.THREE.Line(geometry, material);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(line);
		
		var scaleText = new $wnd.THREE.TextGeometry("10 m", {
			font: 'optimer',
			size: 12,
			height: 0.5,
			curveSegments: 30
		});
		var scaleTextMaterial = new $wnd.THREE.MeshLambertMaterial({color: 0x0000ff});
		var scaleTextMesh = new $wnd.THREE.Mesh(scaleText, scaleTextMaterial);
		scaleTextMesh.position.set(panX, 10, 0);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(scaleTextMesh);
	}-*/;

	@Override
	public native void clear() /*-{
		if(this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::meshes) {
			for(var i = 0; i < this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::meshes.length; i++) {
				this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene.remove(
					this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::meshes[i]);
			}
			
			this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::meshes = new Array();
		}
	}-*/;

	private List<Double> calculateCorner(List<Double> firstPoint, List<Double> secondPoint, List<Double> crossPoint, List<Double> referencePoint) {
		List<Double> result = new ArrayList<>();
		//calculating the a coefficient of the first line (y = ax + b)
		double a = (secondPoint.get(1) - firstPoint.get(1)) / (secondPoint.get(0) - firstPoint.get(0));
		//calculating d coefficient of the second parallel line (y = ax + d)
		double d = crossPoint.get(1) - a * crossPoint.get(0);
		//calculating f coefficient of a perpendicular line to the first one crossing the reference point
		double f = referencePoint.get(1) + referencePoint.get(0) / a;
		//calculating the intersection point of the perpendicular and parallel lines
		result.add((f - d) / (a + (1 / a)));
		result.add(a * ((f - d) / (a + (1 / a))) + d);
		
		return result;
	}

	private void drawDevices(Map<List<List<Double>>, Map<List<Double>, Double>> locationsWithValues) {
		for(Map<List<Double>, Double> locationWithValue : locationsWithValues.values()) {
			for(List<Double> location : locationWithValue.keySet()) {
				JsArrayNumber coordinates = (JsArrayNumber) JsArrayNumber.createArray();
				
				for(Double coordinate : location) {
					coordinates.push(coordinate);
				}
				
				drawDevice(coordinates);
			}
		}
	}

	private String format(double number) {
		return NumberFormat.getFormat("0.00").format(number);
	}

	private native void drawDevice(JsArrayNumber coordinates) /*-{
		var deviceMaterial = new $wnd.THREE.MeshLambertMaterial({color: 0xf44f4f});
		var device = new $wnd.THREE.SphereGeometry(3, 12, 12);
		var deviceMesh = new $wnd.THREE.Mesh(device, deviceMaterial);
		deviceMesh.position.set(coordinates[0], coordinates[1], 0);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(deviceMesh);
	}-*/;

	private native void drawSection(JsArray<JsArrayNumber> nativeCoordinates) /*-{
		var shape = new $wnd.THREE.Shape();
		shape.moveTo(nativeCoordinates[0][0], nativeCoordinates[0][1], 0);
		
		for(var i = 1; i < nativeCoordinates.length; i++) {
			shape.lineTo(nativeCoordinates[i][0], nativeCoordinates[i][1], 0);
		}
		
		shape.lineTo(nativeCoordinates[0][0], nativeCoordinates[0][1], 0);
		
		var geometry = new $wnd.THREE.ShapeGeometry(shape);
		var mesh = new $wnd.THREE.Mesh(geometry, new $wnd.THREE.MeshBasicMaterial({color: 0xbebebe}));
		var wireframe = new $wnd.THREE.EdgesHelper(mesh, 0x383838);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(mesh);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(wireframe);
	}-*/;

	private native void drawLegend(int topColor, int bottomColor, double bottomValue, double topValue, String parameterUnit) /*-{
		var bottom = new $wnd.THREE.Color(bottomColor);
		var top = new $wnd.THREE.Color(topColor);
		var levels = 5;
		var height = 290;
		var lift = 5;
		var moveLeft = 5;
		var levelHeight = height / levels;
		var levelWidth = 50
		var valueStep = (topValue - bottomValue) / levels;
		var textSpacing = 5;
		var textSize = 12;
		
		for(var i = 0; i < levels; i++) {
			var geometry = new $wnd.THREE.Geometry();
			geometry.vertices.push(
				new $wnd.THREE.Vector3(moveLeft, i * levelHeight + lift, 0),
				new $wnd.THREE.Vector3(levelWidth + moveLeft, i * levelHeight + lift, 0),
				new $wnd.THREE.Vector3(levelWidth + moveLeft, i * levelHeight + levelHeight + lift, 0),
				new $wnd.THREE.Vector3(moveLeft, i * levelHeight + levelHeight + lift, 0)
			);
			
			var face1 = new $wnd.THREE.Face3(0, 1, 2);
			face1.vertexColors[0] = new $wnd.THREE.Color(bottom.clone().lerp(top, (i * levelHeight) / height));
			face1.vertexColors[1] = new $wnd.THREE.Color(bottom.clone().lerp(top, (i * levelHeight) / height));
			face1.vertexColors[2] = new $wnd.THREE.Color(bottom.clone().lerp(top, ((i + 1) * levelHeight) / height));
			
			var face2 = new $wnd.THREE.Face3(2, 3, 0);
			face2.vertexColors[0] = new $wnd.THREE.Color(bottom.clone().lerp(top, ((i + 1) * levelHeight) / height));
			face2.vertexColors[1] = new $wnd.THREE.Color(bottom.clone().lerp(top, ((i + 1) * levelHeight) / height));
			face2.vertexColors[2] = new $wnd.THREE.Color(bottom.clone().lerp(top, (i * levelHeight) / height));
			
			geometry.faces.push(face1);
			geometry.faces.push(face2);
			
			var material = new $wnd.THREE.MeshBasicMaterial({vertexColors: $wnd.THREE.VertexColors});
			var mesh = new $wnd.THREE.Mesh(geometry, material);
			var wireframe = new $wnd.THREE.EdgesHelper(mesh, 0x383838);
			this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(mesh);
			this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(wireframe);
			
			var text = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::format(D)(bottomValue + valueStep * i)
					+ " " + parameterUnit;
			var tickMaterial = new $wnd.THREE.MeshLambertMaterial();
			tickMaterial.color.setHex(0x555555);
			
			var tick = new $wnd.THREE.TextGeometry(text, {
				font: 'optimer',
				size: 12,
				height: 0.5,
				curveSegments: 30
			});
			var tickMesh = new $wnd.THREE.Mesh(tick, tickMaterial);
			var textPositionShift = - textSize / 2;
			
			if(i == 0) {
				textPositionShift = 0;
			}
			
			tickMesh.position.set(levelWidth + moveLeft + textSpacing, i * levelHeight + lift + textPositionShift, 0);
			this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(tickMesh);
			
			if(i == levels -1) {
				text = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::format(D)(bottomValue + valueStep * (i + 1))
					+ " " + parameterUnit;
				tick = new $wnd.THREE.TextGeometry(text, {
					font: 'optimer',
					size: textSize,
					height: 1
				});
				tickMesh = new $wnd.THREE.Mesh(tick, tickMaterial);
				tickMesh.position.set(levelWidth + moveLeft + textSpacing, (i + 1) * levelHeight + lift - textSize, 0);
				this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(tickMesh);
			}
		}
	}-*/;

	private native void addRenderer(Element element, int width, int height) /*-{
		var scene = new $wnd.THREE.Scene();
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene = scene;
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::meshes = new Array();
		
		var camera = new $wnd.THREE.OrthographicCamera( 0, width, height, 0, 1, 30 );
		camera.position.set(0, 0, 20);
		camera.lookAt(new $wnd.THREE.Vector3(0, 0, 0));
		
		var light = new $wnd.THREE.PointLight(0xffffff);
		light.position.set(0, 100, 100);
		light.intensity = 1.2;
		scene.add(light);
		
		var renderer = $wnd.Detector.webgl ? new $wnd.THREE.WebGLRenderer({antialias: true}) : new $wnd.THREE.CanvasRenderer();
		renderer.setSize(width, height);
		renderer.setClearColor(0xffffff);
		element.appendChild(renderer.domElement);

		var render = function() {
			$wnd.requestAnimationFrame(render);
			renderer.render(scene, camera);
		};

		render();
	}-*/;

	private native void drawHeatSection(JsArray<JsArrayNumber> coordinates, int topColor, int bottomColor, double minValue, double maxValue) /*-{
		var bottom = new $wnd.THREE.Color(bottomColor);
		var top = new $wnd.THREE.Color(topColor);
		for(var i = 0; i < coordinates.length; i = i + 5) {
			var geometry = new $wnd.THREE.Geometry();
			geometry.vertices.push(
				new $wnd.THREE.Vector3(coordinates[i][0], coordinates[i][1], 0),
				new $wnd.THREE.Vector3(coordinates[i + 1][0], coordinates[i + 1][1], 0),
				new $wnd.THREE.Vector3(coordinates[i + 2][0], coordinates[i + 2][1], 0),
				new $wnd.THREE.Vector3(coordinates[i + 3][0], coordinates[i + 3][1], 0)
			);
			
			var face1 = new $wnd.THREE.Face3(0, 3, 2);
			face1.vertexColors[0] = new $wnd.THREE.Color(bottom.clone().lerp(top, (coordinates[i + 4][0] - minValue) / (maxValue - minValue)));
			face1.vertexColors[1] = new $wnd.THREE.Color(bottom.clone().lerp(top, (coordinates[i + 4][1] - minValue) / (maxValue - minValue)));
			face1.vertexColors[2] = new $wnd.THREE.Color(bottom.clone().lerp(top, (coordinates[i + 4][1] - minValue) / (maxValue - minValue)));
			
			var face2 = new $wnd.THREE.Face3(2, 1, 0);
			face2.vertexColors[0] = new $wnd.THREE.Color(bottom.clone().lerp(top, (coordinates[i + 4][1] - minValue) / (maxValue - minValue)));
			face2.vertexColors[1] = new $wnd.THREE.Color(bottom.clone().lerp(top, (coordinates[i + 4][0] - minValue) / (maxValue - minValue)));
			face2.vertexColors[2] = new $wnd.THREE.Color(bottom.clone().lerp(top, (coordinates[i + 4][0] - minValue) / (maxValue - minValue)));
			
			geometry.faces.push(face1);
			geometry.faces.push(face2);
			
			var material = new $wnd.THREE.MeshBasicMaterial({vertexColors: $wnd.THREE.VertexColors});
			var mesh = new $wnd.THREE.Mesh(geometry, material);
			this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(mesh);
		}
	}-*/;
	
	private native void addMesh(JavaScriptObject mesh) /*-{
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene.add(mesh);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::meshes.push(mesh);
	}-*/;
}