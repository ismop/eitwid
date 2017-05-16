package pl.ismop.web.client.widgets.analysis.verticalslice;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSlicePresenter.Borehole;
import pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSlicePresenter.Point;

public class VerticalSliceView extends Composite implements IVerticalSliceView {
	private static VerticalSliceViewUiBinder uiBinder = GWT.create(VerticalSliceViewUiBinder.class);

	interface VerticalSliceViewUiBinder extends UiBinder<Widget, VerticalSliceView> {}

	private static final double PROFILE_HEIGHT = 4.5;

	private static final double PROFILE_TOP_WIDTH = 4;

	private JavaScriptObject scene, meshes;

	@UiField
	VerticalSliceMessages messages;

	@UiField
	FlowPanel loadingPanel, panel;

	public VerticalSliceView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void showLoadingState(boolean show) {
		loadingPanel.setVisible(show);
	}

	@Override
	public void init() {
		if(scene == null) {
			addRenderer(panel.getElement(), panel.getOffsetWidth(), panel.getOffsetHeight());
		}
	}

	@Override
	public void drawCrosssection(String parameterUnit, boolean leftBank, List<Borehole> boreholes,
			Map<Double, List<Double>> legend) {
		parameterUnit = parameterUnit.replaceAll("\u2103", "\u00B0C");

		@SuppressWarnings("unchecked")
		JsArray<JsArrayNumber> nativeLegend = (JsArray<JsArrayNumber>) JsArray.createArray();

		for (Double colorBoundary : legend.keySet()) {
			JsArrayNumber boundaryAndColor = (JsArrayNumber) JsArrayNumber.createArray();
			boundaryAndColor.push(colorBoundary);
			boundaryAndColor.push(legend.get(colorBoundary).get(0)); //R
			boundaryAndColor.push(legend.get(colorBoundary).get(1)); //G
			boundaryAndColor.push(legend.get(colorBoundary).get(2)); //B
			boundaryAndColor.push(legend.get(colorBoundary).get(3)); //value
			nativeLegend.push(boundaryAndColor);
		}

		drawLegend(nativeLegend, parameterUnit);

		Optional<Double> width = boreholes.stream().flatMap(borehole -> borehole.points.stream())
				.map(point -> point.x).max(Comparator.naturalOrder());
		double shiftX = 200;
		double shiftY = 40;
		double scale = Math.min((panel.getOffsetHeight() - shiftY) / PROFILE_HEIGHT,
				(panel.getOffsetWidth() - shiftX) / width.get());
		drawScale(scale, shiftX);
		drawDevices(boreholes, scale, shiftX, shiftY);

		for (int i = 0; i < boreholes.size() - 1; i++) {
			Borehole leftBorehole = boreholes.get(i);
			Borehole rightBorehole = boreholes.get(i + 1);
			int leftPointIndex = 0;
			int rightPointIndex = 0;
			boolean allFacesCreated = false;

			//creating faces between every two boreholes
			while (!allFacesCreated) {
				List<Point> leftPoints = Lists.reverse(leftBorehole.points).subList(leftPointIndex,
						Math.min(leftPointIndex + 2, leftBorehole.points.size()));
				List<Point> rightPoints = Lists.reverse(rightBorehole.points).subList(rightPointIndex,
						Math.min(rightPointIndex + 2, rightBorehole.points.size()));
				JavaScriptObject geometry = createGeometry();

				if (leftPoints.size() == 1 && rightPoints.size() == 2) {
					addVertices(geometry,
							createVector(leftPoints.get(0).x * scale + shiftX,
									leftPoints.get(0).y * scale + shiftY),
							createVector(rightPoints.get(0).x * scale + shiftX,
									rightPoints.get(0).y * scale + shiftY),
							createVector(rightPoints.get(1).x * scale + shiftX,
									rightPoints.get(1).y * scale + shiftY));
					addFace(geometry, createFace(0, 1, 2,
							createColor(leftPoints.get(0).r, leftPoints.get(0).g,
									leftPoints.get(0).b),
							createColor(rightPoints.get(0).r, rightPoints.get(0).g,
									rightPoints.get(0).b),
							createColor(rightPoints.get(1).r, rightPoints.get(1).g,
									rightPoints.get(1).b)));
				} else if (leftPoints.size() == 2 && rightPoints.size() == 1) {
					addVertices(geometry,
							createVector(leftPoints.get(0).x * scale + shiftX,
									leftPoints.get(0).y * scale + shiftY),
							createVector(rightPoints.get(0).x * scale + shiftX,
									rightPoints.get(0).y * scale + shiftY),
							createVector(leftPoints.get(1).x * scale + shiftX,
									leftPoints.get(1).y * scale + shiftY));
					addFace(geometry, createFace(0, 1, 2,
							createColor(leftPoints.get(0).r, leftPoints.get(0).g,
									leftPoints.get(0).b),
							createColor(rightPoints.get(0).r, rightPoints.get(0).g,
									rightPoints.get(0).b),
							createColor(leftPoints.get(1).r, leftPoints.get(1).g,
									leftPoints.get(1).b)));
				} else if (leftPoints.size() == 2 && rightPoints.size() == 2) {
					addVertices(geometry,
							createVector(leftPoints.get(0).x * scale + shiftX,
									leftPoints.get(0).y * scale + shiftY),
							createVector(rightPoints.get(0).x * scale + shiftX,
									rightPoints.get(0).y * scale + shiftY),
							createVector(rightPoints.get(1).x * scale + shiftX,
									rightPoints.get(1).y * scale + shiftY),
							createVector(leftPoints.get(1).x * scale + shiftX,
									leftPoints.get(1).y * scale + shiftY));
					addFace(geometry, createFace(0, 1, 2,
							createColor(leftPoints.get(0).r, leftPoints.get(0).g,
									leftPoints.get(0).b),
							createColor(rightPoints.get(0).r, rightPoints.get(0).g,
									rightPoints.get(0).b),
							createColor(rightPoints.get(1).r, rightPoints.get(1).g,
									rightPoints.get(1).b)));
					addFace(geometry, createFace(2, 3, 0,
							createColor(rightPoints.get(1).r, rightPoints.get(1).g,
									rightPoints.get(1).b),
							createColor(leftPoints.get(1).r, leftPoints.get(1).g,
									leftPoints.get(1).b),
							createColor(leftPoints.get(0).r, leftPoints.get(0).g,
									leftPoints.get(0).b)));
				} else {
					allFacesCreated = true;
				}

				if (!allFacesCreated) {
					JavaScriptObject mesh = createMesh(geometry);
					addMesh(mesh);

					leftPointIndex = Math.min(leftPointIndex + 1, leftBorehole.points.size() - 1);
					rightPointIndex = Math.min(rightPointIndex + 1, rightBorehole.points.size() - 1);
				}
			}
		}
	}

	@Override
	public String noMeasurementsMessage() {
		return messages.noMeasurementsMessage();
	}

	@Override
	public String cannotRenderMessage() {
		return messages.cannotRender();
	}

	@Override
	public native boolean canRender() /*-{
		return $wnd.Detector.webgl;
	}-*/;

	@Override
	public native void clear() /*-{
		if(this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::meshes) {
			for(var i = 0; i < this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::meshes.length; i++) {
				this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::scene.remove(
					this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::meshes[i]);
			}

			this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::meshes = new Array();
		}
	}-*/;

	private void drawDevices(List<Borehole> boreholes, double scale, double shiftX, double shiftY) {
		boreholes.stream().flatMap(borehole -> borehole.points.stream())
				.filter(point -> !point.virtual)
				.forEach(norVirtualPoint -> {
					drawDevice(norVirtualPoint.x, norVirtualPoint.y, scale, shiftX, shiftY,
							norVirtualPoint.fakeValue);
				});
	}

	private String format(double number) {
		return NumberFormat.getFormat("0.00").format(number);
	}

	private native void addFace(JavaScriptObject geometry, JavaScriptObject face) /*-{
		geometry.faces.push(face);
	}-*/;

	private native JavaScriptObject createFace(int firstIndex, int secondIndex, int thirdIndex,
			JavaScriptObject firstColor, JavaScriptObject secondColor,
			JavaScriptObject thirdColor) /*-{
		var face = new $wnd.THREE.Face3(firstIndex, secondIndex, thirdIndex);
		face.vertexColors.push(firstColor, secondColor, thirdColor);

		return face;
	}-*/;

	private native JavaScriptObject createMesh(JavaScriptObject geometry) /*-{
		var material = new $wnd.THREE.MeshBasicMaterial({
			vertexColors: $wnd.THREE.VertexColors,
			side: $wnd.THREE.DoubleSide
		});

		return new $wnd.THREE.Mesh(geometry, material);
	}-*/;

	private native void addVertices(JavaScriptObject geometry, JavaScriptObject firstPoint,
			JavaScriptObject secondPoint, JavaScriptObject thirdPoint) /*-{
		geometry.vertices.push(firstPoint, secondPoint, thirdPoint);
	}-*/;

	private native void addVertices(JavaScriptObject geometry, JavaScriptObject firstPoint,
			JavaScriptObject secondPoint, JavaScriptObject thirdPoint,
			JavaScriptObject fourthPoint) /*-{
		geometry.vertices.push(firstPoint, secondPoint, thirdPoint, fourthPoint);
	}-*/;

	private native JavaScriptObject createVector(double x, double y) /*-{
		return new $wnd.THREE.Vector3(x, y, 0);
	}-*/;

	private native JavaScriptObject createGeometry() /*-{
		return new $wnd.THREE.Geometry();
	}-*/;

	private native JavaScriptObject createColor(double r, double g, double b) /*-{
		var rgbValue = "rgb(" + r + ", " + g + ", " + b + ")";

		return new $wnd.THREE.Color(rgbValue);
	}-*/;

	private native void addRenderer(Element element, int width, int height) /*-{
		var scene = new $wnd.THREE.Scene();
		this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::scene = scene;
		this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::meshes = new Array();

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
			face1.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::createColor(DDD)(
				legend[i][1], legend[i][2], legend[i][3]
			);
			face1.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::createColor(DDD)(
				legend[i][1], legend[i][2], legend[i][3]
			);
			face1.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::createColor(DDD)(
				legend[i + 1][1], legend[i + 1][2], legend[i + 1][3]
			);

			var face2 = new $wnd.THREE.Face3(2, 3, 0);
			face2.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::createColor(DDD)(
				legend[i + 1][1], legend[i + 1][2], legend[i + 1][3]
			);
			face2.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::createColor(DDD)(
				legend[i + 1][1], legend[i + 1][2], legend[i + 1][3]
			);
			face2.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::createColor(DDD)(
				legend[i][1], legend[i][2], legend[i][3]
			);

			geometry.faces.push(face1);
			geometry.faces.push(face2);

			var material = new $wnd.THREE.MeshBasicMaterial({vertexColors: $wnd.THREE.VertexColors});
			var mesh = new $wnd.THREE.Mesh(geometry, material);
			var wireframe = new $wnd.THREE.EdgesHelper(mesh, 0x383838);
			this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(mesh);
			this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(wireframe);

			var text = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::format(D)(legend[i][4])
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
			this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(tickMesh);

			if(i == legend.length - 2) {
				text = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::format(D)(legend[i + 1][4])
					+ " " + parameterUnit;
				tick = new $wnd.THREE.TextGeometry(text, {
					font: 'ubuntu',
					size: textSize,
					height: 1
				});
				tickMesh = new $wnd.THREE.Mesh(tick, tickMaterial);
				tickMesh.position.set(levelWidth + moveRight + textSpacing, legend[i + 1][0] * height + lift - textSize, 0);
				this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(tickMesh);
			}
		}
	}-*/;

	private native void addMesh(JavaScriptObject mesh) /*-{
		this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::scene.add(mesh);
		this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::meshes.push(mesh);
	}-*/;

	private native void drawDevice(double x, double y, double scale, double shiftX, double shiftY,
			boolean fakeValue) /*-{
		var color = fakeValue ? 0x000000 : 0xf44f4f;
		var deviceMaterial = new $wnd.THREE.MeshLambertMaterial({color: color});
		var device = new $wnd.THREE.SphereGeometry(3, 12, 12);
		var deviceMesh = new $wnd.THREE.Mesh(device, deviceMaterial);
		deviceMesh.position.set(x * scale + shiftX, y * scale + shiftY, 0);
		this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(deviceMesh);
	}-*/;

	private native void drawScale(double scale, double shiftX) /*-{
		var material = new $wnd.THREE.LineBasicMaterial({
			color: 0x0000ff
		});

		var geometry = new $wnd.THREE.Geometry();
		geometry.vertices.push(
			new $wnd.THREE.Vector3(shiftX, 5, 0),
			new $wnd.THREE.Vector3(shiftX, 2, 0),
			new $wnd.THREE.Vector3(shiftX + scale * 10, 2, 0),
			new $wnd.THREE.Vector3(shiftX + scale * 10, 5, 0)
		);

		var line = new $wnd.THREE.Line(geometry, material);
		this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(line);

		var scaleText = new $wnd.THREE.TextGeometry("10 m", {
			font: 'ubuntu',
			size: 12,
			height: 0.5,
			curveSegments: 30
		});
		var scaleTextMaterial = new $wnd.THREE.MeshLambertMaterial({color: 0x0000ff});
		var scaleTextMesh = new $wnd.THREE.Mesh(scaleText, scaleTextMaterial);
		scaleTextMesh.position.set(shiftX, 10, 0);
		this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(scaleTextMesh);
	}-*/;
}
