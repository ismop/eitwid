package pl.ismop.web.client.widgets.analysis.horizontalslice;

import static java.util.function.Function.identity;

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

import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.Tuple4;
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
			Map<String, Seq<? extends Map<Tuple4<Double, Double, Boolean, Boolean>,
			Tuple3<Integer, Integer, Integer>>>> locationsAndColors) {
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
		drawDevices(locationsAndColors.values().flatMap(identity()).flatMap(Map::keySet));

		for (Seq<? extends Map<Tuple4<Double, Double, Boolean, Boolean>,
				Tuple3<Integer, Integer, Integer>>> sectionLocations
				: locationsAndColors.values()) {
			drawSection(sectionLocations);
		}
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

	private void drawDevices(Seq<Tuple4<Double, Double, Boolean, Boolean>> deviceLocations) {
		deviceLocations
			.filter(location -> location._3())
			.forEach(location -> {
				JsArrayNumber coordinates = (JsArrayNumber) JsArrayNumber.createArray();
				coordinates.push(location._1());
				coordinates.push(location._2());
				drawDevice(coordinates, location._4());
			});
	}

	private String format(double number) {
		return NumberFormat.getFormat("0.00").format(number);
	}

	private void drawSection(Seq<? extends Map<Tuple4<Double, Double, Boolean, Boolean>,
			Tuple3<Integer, Integer, Integer>>> sectionLocations) {

		sectionLocations.sliding(2).forEach(twoProfiles -> drawSectionSegment(
				twoProfiles.get(0), twoProfiles.get(1)));
	}

	private void drawSectionSegment(
			Map<Tuple4<Double, Double, Boolean, Boolean>,
				Tuple3<Integer, Integer, Integer>> leftProfile,
			Map<Tuple4<Double, Double, Boolean, Boolean>,
				Tuple3<Integer, Integer, Integer>> rightProfile) {

		List<Tuple2<Tuple4<Double, Double, Boolean, Boolean>,
			Tuple3<Integer, Integer, Integer>>> leftProfileList = leftProfile.toList();
		List<Tuple2<Tuple4<Double, Double, Boolean, Boolean>,
			Tuple3<Integer, Integer, Integer>>> rightProfileList = rightProfile.toList();

		if (leftProfileList.size() == rightProfileList.size()) {
			List.range(0, leftProfileList.size()).sliding(2).forEach(indexes ->
				drawFourPointFace(
						leftProfileList.get(indexes.get(0)),
						leftProfileList.get(indexes.get(1)),
						rightProfileList.get(indexes.get(0)),
						rightProfileList.get(indexes.get(1))));
		} else if (leftProfileList.size() < rightProfileList.size()) {
			int difference = rightProfileList.size() - leftProfileList.size();
			int breakIndex = leftProfileList.size() / 2;
			List.rangeClosed(0, breakIndex).sliding(2).forEach(indexes ->
				drawFourPointFace(
						leftProfileList.get(indexes.get(0)),
						leftProfileList.get(indexes.get(1)),
						rightProfileList.get(indexes.get(0)),
						rightProfileList.get(indexes.get(1))));
			List.rangeClosed(breakIndex, breakIndex + difference).sliding(2).forEach(indexes ->
				drawThreePointFace(
						leftProfileList.get(breakIndex),
						rightProfileList.get(indexes.get(0)),
						rightProfileList.get(indexes.get(1))));
			List.range(breakIndex, leftProfileList.size()).sliding(2).forEach(indexes ->
				drawFourPointFace(
						leftProfileList.get(indexes.get(0)),
						leftProfileList.get(indexes.get(1)),
						rightProfileList.get(indexes.get(0) + difference),
						rightProfileList.get(indexes.get(1) + difference)));
		} else {
			int difference = leftProfileList.size() - rightProfileList.size();
			int breakIndex = rightProfileList.size() / 2;
			List.rangeClosed(0, breakIndex).sliding(2).forEach(indexes ->
				drawFourPointFace(
						leftProfileList.get(indexes.get(0)),
						leftProfileList.get(indexes.get(1)),
						rightProfileList.get(indexes.get(0)),
						rightProfileList.get(indexes.get(1))));
			List.rangeClosed(breakIndex, breakIndex + difference).sliding(2).forEach(indexes ->
				drawThreePointFace(
						leftProfileList.get(indexes.get(0)),
						leftProfileList.get(indexes.get(1)),
						rightProfileList.get(breakIndex)));
			List.range(breakIndex, rightProfileList.size()).sliding(2).forEach(indexes ->
				drawFourPointFace(
						leftProfileList.get(indexes.get(0) + difference),
						leftProfileList.get(indexes.get(1) + difference),
						rightProfileList.get(indexes.get(0)),
						rightProfileList.get(indexes.get(1))));
		}
	}

	@SuppressWarnings("unchecked")
	private void drawThreePointFace(
			Tuple2<Tuple4<Double, Double, Boolean, Boolean>,
				Tuple3<Integer, Integer, Integer>> first,
			Tuple2<Tuple4<Double, Double, Boolean, Boolean>,
				Tuple3<Integer, Integer, Integer>> second,
			Tuple2<Tuple4<Double, Double, Boolean, Boolean>,
				Tuple3<Integer, Integer, Integer>> third) {

		JsArray<JsArrayNumber> coordinatesWithColors =
				(JsArray<JsArrayNumber>) JsArray.createArray();
		JsArrayNumber firstCoordinateWithColor = (JsArrayNumber) JsArrayNumber.createArray();
		firstCoordinateWithColor.push(first._1()._1());
		firstCoordinateWithColor.push(first._1()._2());
		firstCoordinateWithColor.push(first._2()._1());
		firstCoordinateWithColor.push(first._2()._2());
		firstCoordinateWithColor.push(first._2()._3());
		coordinatesWithColors.push(firstCoordinateWithColor);

		JsArrayNumber secondCoordinateWithColor = (JsArrayNumber) JsArrayNumber.createArray();
		secondCoordinateWithColor.push(second._1()._1());
		secondCoordinateWithColor.push(second._1()._2());
		secondCoordinateWithColor.push(second._2()._1());
		secondCoordinateWithColor.push(second._2()._2());
		secondCoordinateWithColor.push(second._2()._3());
		coordinatesWithColors.push(secondCoordinateWithColor);

		JsArrayNumber thirdCoordinateWithColor = (JsArrayNumber) JsArrayNumber.createArray();
		thirdCoordinateWithColor.push(third._1()._1());
		thirdCoordinateWithColor.push(third._1()._2());
		thirdCoordinateWithColor.push(third._2()._1());
		thirdCoordinateWithColor.push(third._2()._2());
		thirdCoordinateWithColor.push(third._2()._3());
		coordinatesWithColors.push(thirdCoordinateWithColor);

		drawFace(coordinatesWithColors);
	}

	private void drawFourPointFace(
			Tuple2<Tuple4<Double, Double, Boolean, Boolean>,
				Tuple3<Integer, Integer, Integer>> first,
			Tuple2<Tuple4<Double, Double, Boolean, Boolean>,
				Tuple3<Integer, Integer, Integer>> second,
			Tuple2<Tuple4<Double, Double, Boolean, Boolean>,
				Tuple3<Integer, Integer, Integer>> third,
			Tuple2<Tuple4<Double, Double, Boolean, Boolean>,
				Tuple3<Integer, Integer, Integer>> fourth) {

		JsArray<JsArrayNumber> coordinatesWithColors =
				(JsArray<JsArrayNumber>) JsArray.createArray();
		JsArrayNumber firstCoordinateWithColor = (JsArrayNumber) JsArrayNumber.createArray();
		firstCoordinateWithColor.push(first._1()._1());
		firstCoordinateWithColor.push(first._1()._2());
		firstCoordinateWithColor.push(first._2()._1());
		firstCoordinateWithColor.push(first._2()._2());
		firstCoordinateWithColor.push(first._2()._3());
		coordinatesWithColors.push(firstCoordinateWithColor);

		JsArrayNumber secondCoordinateWithColor = (JsArrayNumber) JsArrayNumber.createArray();
		secondCoordinateWithColor.push(second._1()._1());
		secondCoordinateWithColor.push(second._1()._2());
		secondCoordinateWithColor.push(second._2()._1());
		secondCoordinateWithColor.push(second._2()._2());
		secondCoordinateWithColor.push(second._2()._3());
		coordinatesWithColors.push(secondCoordinateWithColor);

		JsArrayNumber thirdCoordinateWithColor = (JsArrayNumber) JsArrayNumber.createArray();
		thirdCoordinateWithColor.push(third._1()._1());
		thirdCoordinateWithColor.push(third._1()._2());
		thirdCoordinateWithColor.push(third._2()._1());
		thirdCoordinateWithColor.push(third._2()._2());
		thirdCoordinateWithColor.push(third._2()._3());
		coordinatesWithColors.push(thirdCoordinateWithColor);

		JsArrayNumber fourthCoordinateWithColor = (JsArrayNumber) JsArrayNumber.createArray();
		fourthCoordinateWithColor.push(fourth._1()._1());
		fourthCoordinateWithColor.push(fourth._1()._2());
		fourthCoordinateWithColor.push(fourth._2()._1());
		fourthCoordinateWithColor.push(fourth._2()._2());
		fourthCoordinateWithColor.push(fourth._2()._3());
		coordinatesWithColors.push(fourthCoordinateWithColor);

		drawFace(coordinatesWithColors);
	}

	private native void drawFace(JsArray<JsArrayNumber> coordinatesWithColors) /*-{
		var geometry = new $wnd.THREE.Geometry();

		for(var i = 0; i < coordinatesWithColors.length; i = i + 1) {
			geometry.vertices.push(
				new $wnd.THREE.Vector3(coordinatesWithColors[i][0], coordinatesWithColors[i][1], 0)
			);
		}

		var face = new $wnd.THREE.Face3(0, 1, 2);
		face.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
			coordinatesWithColors[0][2], coordinatesWithColors[0][3], coordinatesWithColors[0][4]
		);
		face.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
			coordinatesWithColors[1][2], coordinatesWithColors[1][3], coordinatesWithColors[1][4]
		);
		face.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
			coordinatesWithColors[2][2], coordinatesWithColors[2][3], coordinatesWithColors[2][4]
		);
		geometry.faces.push(face);

		if (coordinatesWithColors.length == 4) {
			var face2 = new $wnd.THREE.Face3(1, 2, 3);
			face2.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				coordinatesWithColors[1][2], coordinatesWithColors[1][3], coordinatesWithColors[1][4]
			);
			face2.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				coordinatesWithColors[2][2], coordinatesWithColors[2][3], coordinatesWithColors[2][4]
			);
			face2.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::buildColor(DDD)(
				coordinatesWithColors[3][2], coordinatesWithColors[3][3], coordinatesWithColors[3][4]
			);
			geometry.faces.push(face2);
		}

		var material = new $wnd.THREE.MeshBasicMaterial({vertexColors: $wnd.THREE.VertexColors,
				side: $wnd.THREE.DoubleSide});
		var mesh = new $wnd.THREE.Mesh(geometry, material);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(mesh);
	}-*/;

	private native void drawDevice(JsArrayNumber coordinates, boolean fake) /*-{
		var color = fake ? 0x000000 : 0xf44f4f
		var deviceMaterial = new $wnd.THREE.MeshLambertMaterial({color: color});
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
