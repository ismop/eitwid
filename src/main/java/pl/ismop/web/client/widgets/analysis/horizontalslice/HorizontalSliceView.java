package pl.ismop.web.client.widgets.analysis.horizontalslice;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import javaslang.Tuple3;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Seq;

public class HorizontalSliceView extends Composite implements IHorizontalSliceView {
	private static HorizontalSliceViewUiBinder uiBinder =
			GWT.create(HorizontalSliceViewUiBinder.class);

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
	public void drawCrosssection(Map<Double, Seq<Double>> legend, String parameterUnit,
			Map<String, Map<Tuple3<Double, Double, Boolean>,
			Tuple3<Integer, Integer, Integer>>> locationsAndColors) {
		parameterUnit = parameterUnit.replaceAll("\u2103", "\u00B0C");

		@SuppressWarnings("unchecked")
		JsArray<JsArrayNumber> nativeLegend = (JsArray<JsArrayNumber>) JsArray.createArray();

		legend.forEach((colorBoundary, colors) -> {
			JsArrayNumber boundaryAndColor = (JsArrayNumber) JsArrayNumber.createArray();
			boundaryAndColor.push(colorBoundary);
			boundaryAndColor.push(colors.get(0)); //R
			boundaryAndColor.push(colors.get(1)); //G
			boundaryAndColor.push(colors.get(2)); //B
			boundaryAndColor.push(colors.get(3)); //value
			nativeLegend.push(boundaryAndColor);
		});

		drawLegend(nativeLegend, parameterUnit);
		GWT.log("Locations and colors: " + locationsAndColors);
		drawDevices(locationsAndColors.values().flatMap(Map::keySet));

//		for (Seq<Seq<Double>> sectionCorners : locationsWithValues.keySet()) {
//			@SuppressWarnings("unchecked")
//			JsArray<JsArrayNumber> coordinatesAndValues =
//					(JsArray<JsArrayNumber>) JsArray.createArray();
//			Seq<Double> topLeftCorner = sectionCorners.get(0);
//			Seq<Double> topRightCorner = sectionCorners.get(1);
//			Seq<Double> previousValue = null;
//
//			for (Seq<Double> next : locationsWithValues.get(sectionCorners).get().keySet()) {
//				Seq<Double> bottomLeftCorner = calculateCorner(sectionCorners.get(0),
//						sectionCorners.get(1), next, sectionCorners.get(0));
//				Seq<Double> bottomRightCorner = calculateCorner(sectionCorners.get(0),
//						sectionCorners.get(1), next, sectionCorners.get(1));
//				JsArrayNumber topLeft = (JsArrayNumber) JsArrayNumber.createArray();
//				topLeft.push(topLeftCorner.get(0));
//				topLeft.push(topLeftCorner.get(1));
//				coordinatesAndValues.push(topLeft);
//
//				JsArrayNumber topRight = (JsArrayNumber) JsArrayNumber.createArray();
//				topRight.push(topRightCorner.get(0));
//				topRight.push(topRightCorner.get(1));
//				coordinatesAndValues.push(topRight);
//
//				JsArrayNumber bottomRight = (JsArrayNumber) JsArrayNumber.createArray();
//				bottomRight.push(bottomRightCorner.get(0));
//				bottomRight.push(bottomRightCorner.get(1));
//				coordinatesAndValues.push(bottomRight);
//
//				JsArrayNumber bottomLeft = (JsArrayNumber) JsArrayNumber.createArray();
//				bottomLeft.push(bottomLeftCorner.get(0));
//				bottomLeft.push(bottomLeftCorner.get(1));
//				coordinatesAndValues.push(bottomLeft);
//
//				JsArrayNumber values = (JsArrayNumber) JsArrayNumber.createArray();
//
//				if (previousValue != null) {
//					values.push(previousValue.get(0));
//					values.push(new Double(previousValue.get(1)).intValue());
//					values.push(new Double(previousValue.get(2)).intValue());
//					values.push(new Double(previousValue.get(3)).intValue());
//				} else {
//					values.push(locationsWithValues.get(sectionCorners).get().get(next).get().get(0));
//					values.push(new Double(
//							locationsWithValues.get(sectionCorners).get().get(next).get().get(1)).intValue());
//					values.push(new Double(
//							locationsWithValues.get(sectionCorners).get().get(next).get().get(2)).intValue());
//					values.push(new Double(
//							locationsWithValues.get(sectionCorners).get().get(next).get().get(3)).intValue());
//				}
//
//				values.push(locationsWithValues.get(sectionCorners).get().get(next).get().get(0));
//				values.push(new Double(
//						locationsWithValues.get(sectionCorners).get().get(next).get().get(1)).intValue());
//				values.push(new Double(
//						locationsWithValues.get(sectionCorners).get().get(next).get().get(2)).intValue());
//				values.push(new Double(
//						locationsWithValues.get(sectionCorners).get().get(next).get().get(3)).intValue());
//				coordinatesAndValues.push(values);
//
//				topLeftCorner = bottomLeftCorner;
//				topRightCorner = bottomRightCorner;
//				previousValue = locationsWithValues.get(sectionCorners).get().get(next).get();
//			}
//
//			JsArrayNumber topLeft = (JsArrayNumber) JsArrayNumber.createArray();
//			topLeft.push(topLeftCorner.get(0));
//			topLeft.push(topLeftCorner.get(1));
//			coordinatesAndValues.push(topLeft);
//
//			JsArrayNumber topRight = (JsArrayNumber) JsArrayNumber.createArray();
//			topRight.push(topRightCorner.get(0));
//			topRight.push(topRightCorner.get(1));
//			coordinatesAndValues.push(topRight);
//
//			JsArrayNumber bottomRight = (JsArrayNumber) JsArrayNumber.createArray();
//			bottomRight.push(sectionCorners.get(2).get(0));
//			bottomRight.push(sectionCorners.get(2).get(1));
//			coordinatesAndValues.push(bottomRight);
//
//			JsArrayNumber bottomLeft = (JsArrayNumber) JsArrayNumber.createArray();
//			bottomLeft.push(sectionCorners.get(3).get(0));
//			bottomLeft.push(sectionCorners.get(3).get(1));
//			coordinatesAndValues.push(bottomLeft);
//
//			JsArrayNumber values = (JsArrayNumber) JsArrayNumber.createArray();
//			values.push(previousValue.get(0));
//			values.push(new Double(previousValue.get(1)).intValue());
//			values.push(new Double(previousValue.get(2)).intValue());
//			values.push(new Double(previousValue.get(3)).intValue());
//			values.push(previousValue.get(0));
//			values.push(new Double(previousValue.get(1)).intValue());
//			values.push(new Double(previousValue.get(2)).intValue());
//			values.push(new Double(previousValue.get(3)).intValue());
//			coordinatesAndValues.push(values);
//
//			drawHeatSection(coordinatesAndValues);
//		}
	};

	@Override
	public void drawMuteSections(Seq<Seq<Seq<Double>>> coordinates) {
		for (Seq<Seq<Double>> sectionCoordinates : coordinates) {
			@SuppressWarnings("unchecked")
			JsArray<JsArrayNumber> nativeCoordinates =
					(JsArray<JsArrayNumber>) JsArray.createArray();

			for (Seq<Double> pointCoordinates : sectionCoordinates) {
				JsArrayNumber nativePointCoordinates = (JsArrayNumber) JsArray.createArray();

				for (Double coordinate : pointCoordinates) {
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
	public void init() {
		if (scene == null) {
			addRenderer(panel.getElement(), panel.getOffsetWidth(), panel.getOffsetHeight());
		}
	}

	@Override
	public String noMeasurementsMessage() {
		return messages.noMeasurementsMessage();
	}

	@Override
	public native boolean canRender() /*-{
		return $wnd.Detector.webgl;
	}-*/;

	@Override
	public String cannotRenderMessages() {
		return messages.cannotRender();
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
			font: 'ubuntu',
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

	private Seq<Double> calculateCorner(Seq<Double> firstPoint, Seq<Double> secondPoint,
			Seq<Double> crossPoint, Seq<Double> referencePoint) {
		Seq<Double> result = List.empty();
		//calculating the a coefficient of the first line (y = ax + b)
		double a = (secondPoint.get(1) - firstPoint.get(1)) / (secondPoint.get(0) - firstPoint.get(0));
		//calculating d coefficient of the second parallel line (y = ax + d)
		double d = crossPoint.get(1) - a * crossPoint.get(0);
		//calculating f coefficient of a perpendicular line to the first one crossing the reference point
		double f = referencePoint.get(1) + referencePoint.get(0) / a;
		//calculating the intersection point of the perpendicular and parallel lines
		result = result.append((f - d) / (a + (1 / a)));
		result = result.append(a * ((f - d) / (a + (1 / a))) + d);

		return result;
	}

	private void drawDevices(Seq<Tuple3<Double, Double, Boolean>> deviceLocations) {
		deviceLocations
				.filter(location -> location._3())
				.forEach(location -> {
					JsArrayNumber coordinates = (JsArrayNumber) JsArrayNumber.createArray();
					coordinates.push(location._1());
					coordinates.push(location._2());
					drawDevice(coordinates);
				});
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

		var geometry = new $wnd.THREE.ShapeGeometry(shape);
		var mesh = new $wnd.THREE.Mesh(geometry, new $wnd.THREE.MeshBasicMaterial({color: 0xbebebe}));
		var wireframe = new $wnd.THREE.EdgesHelper(mesh, 0x383838);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(mesh);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(wireframe);
	}-*/;

	private native void drawLegend(JsArray<JsArrayNumber> legend, String parameterUnit) /*-{
		var height = 290;
		var lift = 5;
		var moveRight = 5;
		var levelWidth = 50
		var textSpacing = 5;
		var textSize = 12;

		for (var i = 0; i < legend.length - 1; i++) {
			var geometry = new $wnd.THREE.Geometry();
			geometry.vertices.push(
				new $wnd.THREE.Vector3(moveRight, legend[i][0] * height + lift, 0),
				new $wnd.THREE.Vector3(levelWidth + moveRight, legend[i][0] * height + lift, 0),
				new $wnd.THREE.Vector3(levelWidth + moveRight, legend[i + 1][0] * height + lift, 0),
				new $wnd.THREE.Vector3(moveRight, legend[i + 1][0] * height + lift, 0)
			);

			var face1 = new $wnd.THREE.Face3(0, 1, 2);
			face1.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				legend[i][1], legend[i][2], legend[i][3]
			);
			face1.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				legend[i][1], legend[i][2], legend[i][3]
			);
			face1.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				legend[i + 1][1], legend[i + 1][2], legend[i + 1][3]
			);

			var face2 = new $wnd.THREE.Face3(2, 3, 0);
			face2.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				legend[i + 1][1], legend[i + 1][2], legend[i + 1][3]
			);
			face2.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				legend[i + 1][1], legend[i + 1][2], legend[i + 1][3]
			);
			face2.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				legend[i][1], legend[i][2], legend[i][3]
			);

			geometry.faces.push(face1);
			geometry.faces.push(face2);

			var material = new $wnd.THREE.MeshBasicMaterial({vertexColors: $wnd.THREE.VertexColors});
			var mesh = new $wnd.THREE.Mesh(geometry, material);
			var wireframe = new $wnd.THREE.EdgesHelper(mesh, 0x383838);
			this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(mesh);
			this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(wireframe);

			var text = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::format(D)(legend[i][4])
					+ " " + parameterUnit;
			var tickMaterial = new $wnd.THREE.MeshLambertMaterial();
			tickMaterial.color.setHex(0x555555);

			var tick = new $wnd.THREE.TextGeometry(text, {
				font: 'ubuntu',
				size: 12,
				height: 0.5,
				curveSegments: 30
			});
			var tickMesh = new $wnd.THREE.Mesh(tick, tickMaterial);
			var textPositionShift = - textSize / 2;

			if(i == 0) {
				textPositionShift = 0;
			}

			tickMesh.position.set(levelWidth + moveRight + textSpacing, legend[i][0] * height + lift + textPositionShift, 0);
			this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(tickMesh);

			if(i == legend.length - 2) {
				text = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::format(D)(legend[i + 1][4])
					+ " " + parameterUnit;
				tick = new $wnd.THREE.TextGeometry(text, {
					font: 'ubuntu',
					size: textSize,
					height: 1
				});
				tickMesh = new $wnd.THREE.Mesh(tick, tickMaterial);
				tickMesh.position.set(levelWidth + moveRight + textSpacing, legend[i + 1][0] * height + lift - textSize, 0);
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

	private native void drawHeatSection(JsArray<JsArrayNumber> coordinates) /*-{
		for(var i = 0; i < coordinates.length; i = i + 5) {
			var geometry = new $wnd.THREE.Geometry();
			geometry.vertices.push(
				new $wnd.THREE.Vector3(coordinates[i][0], coordinates[i][1], 0),
				new $wnd.THREE.Vector3(coordinates[i + 1][0], coordinates[i + 1][1], 0),
				new $wnd.THREE.Vector3(coordinates[i + 2][0], coordinates[i + 2][1], 0),
				new $wnd.THREE.Vector3(coordinates[i + 3][0], coordinates[i + 3][1], 0)
			);

			var face1 = new $wnd.THREE.Face3(0, 3, 2);
			face1.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				coordinates[i + 4][1], coordinates[i + 4][2], coordinates[i + 4][3]
			);
			face1.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				coordinates[i + 4][5], coordinates[i + 4][6], coordinates[i + 4][7]
			);
			face1.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				coordinates[i + 4][5], coordinates[i + 4][6], coordinates[i + 4][7]
			);

			var face2 = new $wnd.THREE.Face3(2, 1, 0);
			face2.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				coordinates[i + 4][5], coordinates[i + 4][6], coordinates[i + 4][7]
			);
			face2.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				coordinates[i + 4][1], coordinates[i + 4][2], coordinates[i + 4][3]
			);
			face2.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				coordinates[i + 4][1], coordinates[i + 4][2], coordinates[i + 4][3]
			);

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

	private native void buildColor(double r, double g, double b) /*-{
		var rgbValue = "rgb("
				+ r + ", "
				+ g + ", "
				+ b
				+ ")";

		return new $wnd.THREE.Color(rgbValue);
	}-*/;
}
