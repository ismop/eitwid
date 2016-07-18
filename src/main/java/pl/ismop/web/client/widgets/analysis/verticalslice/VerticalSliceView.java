package pl.ismop.web.client.widgets.analysis.verticalslice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	public void drawCrosssection(String parameterUnit, boolean leftBank,
			Map<Double, List<Double>> profileAndDevicePositionsWithValuesAndColors,
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
		
		List<Double> xList = new ArrayList<Double>(
				profileAndDevicePositionsWithValuesAndColors.keySet());
		double bottomWidth = xList.get(xList.size() - 1);
		double leftUpperCorner = (bottomWidth - PROFILE_TOP_WIDTH) / 2;
		double rightUpperCorner = leftUpperCorner + PROFILE_TOP_WIDTH;
		
		double shiftX = 200;
		double shiftY = 40;
		double scale = Math.min((panel.getOffsetHeight() - shiftY) / PROFILE_HEIGHT,
				(panel.getOffsetWidth() - shiftX) / xList.get(xList.size() - 1));
		drawScale(scale, shiftX);
		
		Iterator<Double> i = profileAndDevicePositionsWithValuesAndColors.keySet().iterator();
		@SuppressWarnings("unchecked")
		JsArray<JsArray<JsArrayNumber>> localCoordinates =
				(JsArray<JsArray<JsArrayNumber>>) JsArray.createArray();
		@SuppressWarnings("unchecked")
		JsArray<JsArrayNumber> valuesWithColors = (JsArray<JsArrayNumber>) JsArray.createArray();
		boolean leftAdded = false, rightAdded = false;
		double previousCoordinate = 0.0;
		
		while (i.hasNext()) {
			Double xCoordinate = i.next();
			
			@SuppressWarnings("unchecked")
			JsArray<JsArrayNumber> lineCoordinates =
					(JsArray<JsArrayNumber>) JsArrayNumber.createArray();
			JsArrayNumber coordinates = (JsArrayNumber) JsArrayNumber.createArray();
			lineCoordinates.push(coordinates);
			coordinates.push(xCoordinate * scale + shiftX);
			coordinates.push(0.0 + shiftY);
			
			if (localCoordinates.length() > 0 && i.hasNext()) {
				drawDevice(xCoordinate, scale, shiftX, shiftY);
				//middle point
				if (xCoordinate < leftUpperCorner) {
					//left slope
					coordinates = (JsArrayNumber) JsArrayNumber.createArray();
					lineCoordinates.push(coordinates);
					coordinates.push(xCoordinate * scale + shiftX);
					coordinates.push(findY(0.0, 0.0, leftUpperCorner, PROFILE_HEIGHT, xCoordinate)
							* scale + shiftY);
				} else if (xCoordinate > rightUpperCorner) {
					//right slope
					if (!rightAdded) {
						rightAdded = true;
						
						double value = calculateValueProportionallyToDistance(
								profileAndDevicePositionsWithValuesAndColors
										.get(xCoordinate).get(0),
								valuesWithColors.get(valuesWithColors.length() - 1).get(0),
										xCoordinate, previousCoordinate, rightUpperCorner);
						JsArrayNumber valueWithColor = (JsArrayNumber) JsArrayNumber.createArray();
						valueWithColor.push(value);
						valueWithColor.push(calculateIntermediateColorChannel(
								xCoordinate, previousCoordinate, rightUpperCorner,
								profileAndDevicePositionsWithValuesAndColors
										.get(xCoordinate).get(1),
								profileAndDevicePositionsWithValuesAndColors
										.get(previousCoordinate).get(1)
						));
						valueWithColor.push(calculateIntermediateColorChannel(
								xCoordinate, previousCoordinate, rightUpperCorner,
								profileAndDevicePositionsWithValuesAndColors
										.get(xCoordinate).get(2),
								profileAndDevicePositionsWithValuesAndColors
										.get(previousCoordinate).get(2)
						));
						valueWithColor.push(calculateIntermediateColorChannel(
								xCoordinate, previousCoordinate, rightUpperCorner,
								profileAndDevicePositionsWithValuesAndColors
										.get(xCoordinate).get(3),
								profileAndDevicePositionsWithValuesAndColors
										.get(previousCoordinate).get(3)
						));
						valuesWithColors.push(valueWithColor);
						
						@SuppressWarnings("unchecked")
						JsArray<JsArrayNumber> additionalCoordinates =
								(JsArray<JsArrayNumber>) JsArrayNumber.createArray();
						localCoordinates.push(additionalCoordinates);
						
						JsArrayNumber cornerCoordinates =
								(JsArrayNumber) JsArrayNumber.createArray();
						additionalCoordinates.push(cornerCoordinates);
						cornerCoordinates.push(rightUpperCorner * scale + shiftX);
						cornerCoordinates.push(0.0 + shiftY);
						
						cornerCoordinates = (JsArrayNumber) JsArrayNumber.createArray();
						additionalCoordinates.push(cornerCoordinates);
						cornerCoordinates.push(rightUpperCorner * scale + shiftX);
						cornerCoordinates.push(PROFILE_HEIGHT * scale + shiftY);
					}
					
					coordinates = (JsArrayNumber) JsArrayNumber.createArray();
					lineCoordinates.push(coordinates);
					coordinates.push(xCoordinate * scale + shiftX);
					
					//we need the last x value
					coordinates.push(findY(rightUpperCorner, PROFILE_HEIGHT,
							xList.get(xList.size() - 1), 0.0, xCoordinate) * scale + shiftY);
				} else {
					//middle section
					if(!leftAdded) {
						leftAdded = true;
						
						double value = calculateValueProportionallyToDistance(
								profileAndDevicePositionsWithValuesAndColors
										.get(xCoordinate).get(0),
								valuesWithColors.get(valuesWithColors.length() - 1).get(0),
										xCoordinate, previousCoordinate, leftUpperCorner);
						JsArrayNumber valueWithColor = (JsArrayNumber) JsArrayNumber.createArray();
						valueWithColor.push(value);
						valueWithColor.push(calculateIntermediateColorChannel(
								xCoordinate, previousCoordinate, leftUpperCorner,
								profileAndDevicePositionsWithValuesAndColors
										.get(xCoordinate).get(1),
								profileAndDevicePositionsWithValuesAndColors
										.get(previousCoordinate).get(1)
						));
						valueWithColor.push(calculateIntermediateColorChannel(
								xCoordinate, previousCoordinate, leftUpperCorner,
								profileAndDevicePositionsWithValuesAndColors
										.get(xCoordinate).get(2),
								profileAndDevicePositionsWithValuesAndColors
										.get(previousCoordinate).get(2)
						));
						valueWithColor.push(calculateIntermediateColorChannel(
								xCoordinate, previousCoordinate, leftUpperCorner,
								profileAndDevicePositionsWithValuesAndColors
										.get(xCoordinate).get(3),
								profileAndDevicePositionsWithValuesAndColors
										.get(previousCoordinate).get(3)
						));
						valuesWithColors.push(valueWithColor);
						
						@SuppressWarnings("unchecked")
						JsArray<JsArrayNumber> additionalCoordinates =
								(JsArray<JsArrayNumber>) JsArrayNumber.createArray();
						localCoordinates.push(additionalCoordinates);
						
						JsArrayNumber cornerCoordinates =
								(JsArrayNumber) JsArrayNumber.createArray();
						additionalCoordinates.push(cornerCoordinates);
						cornerCoordinates.push(leftUpperCorner * scale + shiftX);
						cornerCoordinates.push(0.0 + shiftY);
						
						cornerCoordinates = (JsArrayNumber) JsArrayNumber.createArray();
						additionalCoordinates.push(cornerCoordinates);
						cornerCoordinates.push(leftUpperCorner * scale + shiftX);
						cornerCoordinates.push(PROFILE_HEIGHT * scale + shiftY);
					}
					
					coordinates = (JsArrayNumber) JsArrayNumber.createArray();
					lineCoordinates.push(coordinates);
					coordinates.push(xCoordinate * scale + shiftX);
					coordinates.push(PROFILE_HEIGHT * scale + shiftY);
				}
			}
			
			double value = profileAndDevicePositionsWithValuesAndColors.get(xCoordinate).get(0);
			JsArrayNumber valueWithColor = (JsArrayNumber) JsArrayNumber.createArray();
			valueWithColor.push(value);
			valueWithColor.push(profileAndDevicePositionsWithValuesAndColors
					.get(xCoordinate).get(1).intValue());
			valueWithColor.push(profileAndDevicePositionsWithValuesAndColors
					.get(xCoordinate).get(2).intValue());
			valueWithColor.push(profileAndDevicePositionsWithValuesAndColors
					.get(xCoordinate).get(3).intValue());
			valuesWithColors.push(valueWithColor);
			localCoordinates.push(lineCoordinates);
			previousCoordinate = xCoordinate;
		}
		
		JsArrayNumber waterXs = (JsArrayNumber) JsArrayNumber.createArray();
		JsArrayNumber waterYs = (JsArrayNumber) JsArrayNumber.createArray();
		waterYs.push(shiftY);
		waterYs.push(2.0 * scale + shiftY);
		
		if(leftBank) {
			waterXs.push(shiftX);
			waterXs.push((bottomWidth / 2) * scale + shiftX);
		} else {
			waterXs.push((bottomWidth / 2) * scale + shiftX);
			waterXs.push(bottomWidth * scale + shiftX);
		}
		
		drawSlice(localCoordinates, valuesWithColors, waterXs, waterYs);
	}

	private int calculateIntermediateColorChannel(Double rightCoordinate, double leftCoordinate,
			double currentCoordinate, Double rightColorChannel, Double leftColorChannel) {
		return new Double(leftColorChannel + (rightColorChannel - leftColorChannel)
				* ((currentCoordinate - leftCoordinate) / (rightCoordinate - leftCoordinate)))
				.intValue();
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

	private double findY(double firstX, double firstY, double secondX, double secondY, Double xCoordinate) {
		double a = (secondY - firstY) / (secondX - firstX);
		double b = firstY - a * firstX;
		
		return a * xCoordinate + b;
	}

	private String format(double number) {
		return NumberFormat.getFormat("0.00").format(number);
	}

	private double calculateValueProportionallyToDistance(double currentValue, double previousValue,
			double rightX, double leftX, double currentX) {
		double result = previousValue + (currentValue - previousValue) * ((currentX - leftX)
				/ (rightX - leftX));
		
		return result; 
	}

	private native void drawSlice(JsArray<JsArray<JsArrayNumber>> localCoordinates,
			JsArray<JsArrayNumber> valuesWithColors, JsArrayNumber waterXs,
			JsArrayNumber waterYs) /*-{
		$wnd.console.log("coords: " + localCoordinates + ", values with colors: " + valuesWithColors);
				
		for(var i = 0; i < localCoordinates.length - 1; i++) {
			var geometry = new $wnd.THREE.Geometry();
			
			if(localCoordinates[i].length == 1) {
				geometry.vertices.push(
					new $wnd.THREE.Vector3(localCoordinates[i][0][0], localCoordinates[i][0][1], 0),
					new $wnd.THREE.Vector3(localCoordinates[i + 1][0][0], localCoordinates[i + 1][0][1], 0),
					new $wnd.THREE.Vector3(localCoordinates[i + 1][1][0], localCoordinates[i + 1][1][1], 0)
				);
				
				var face = new $wnd.THREE.Face3(0, 1, 2);
				face.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i][1], valuesWithColors[i][2], valuesWithColors[i][3]
				);
				face.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i + 1][1], valuesWithColors[i + 1][2], valuesWithColors[i + 1][3]
				);
				face.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i + 1][1], valuesWithColors[i + 1][2], valuesWithColors[i + 1][3]
				);
				
				geometry.faces.push(face);
			} else if(localCoordinates[i + 1].length == 1) {
				geometry.vertices.push(
					new $wnd.THREE.Vector3(localCoordinates[i][0][0], localCoordinates[i][0][1], 0),
					new $wnd.THREE.Vector3(localCoordinates[i + 1][0][0], localCoordinates[i + 1][0][1], 0),
					new $wnd.THREE.Vector3(localCoordinates[i][1][0], localCoordinates[i][1][1], 0)
				);
				
				var face = new $wnd.THREE.Face3(0, 1, 2);
				face.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i][1], valuesWithColors[i][2], valuesWithColors[i][3]
				);
				face.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i + 1][1], valuesWithColors[i + 1][2], valuesWithColors[i + 1][3]
				);
				face.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i][1], valuesWithColors[i][2], valuesWithColors[i][3]
				);
				
				geometry.faces.push(face);
			} else {
				geometry.vertices.push(
					new $wnd.THREE.Vector3(localCoordinates[i][0][0], localCoordinates[i][0][1], 0),
					new $wnd.THREE.Vector3(localCoordinates[i + 1][0][0], localCoordinates[i + 1][0][1], 0),
					new $wnd.THREE.Vector3(localCoordinates[i + 1][1][0], localCoordinates[i + 1][1][1], 0),
					new $wnd.THREE.Vector3(localCoordinates[i][1][0], localCoordinates[i][1][1], 0)
				);
				
				var face1 = new $wnd.THREE.Face3(0, 1, 2);
				face1.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i][1], valuesWithColors[i][2], valuesWithColors[i][3]
				);
				face1.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i + 1][1], valuesWithColors[i + 1][2], valuesWithColors[i + 1][3]
				);
				face1.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i + 1][1], valuesWithColors[i + 1][2], valuesWithColors[i + 1][3]
				);
				
				var face2 = new $wnd.THREE.Face3(2, 3, 0);
				face2.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i + 1][1], valuesWithColors[i + 1][2], valuesWithColors[i + 1][3]
				);
				face2.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i][1], valuesWithColors[i][2], valuesWithColors[i][3]
				);
				face2.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
					valuesWithColors[i][1], valuesWithColors[i][2], valuesWithColors[i][3]
				);
				
				geometry.faces.push(face1);
				geometry.faces.push(face2);
			}
			
			var material = new $wnd.THREE.MeshBasicMaterial({vertexColors: $wnd.THREE.VertexColors});
			var mesh = new $wnd.THREE.Mesh(geometry, material);
			this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(mesh);
			
			var waterShape = new $wnd.THREE.Shape();
			waterShape.moveTo(waterXs[0], waterYs[0]);
			waterShape.lineTo(waterXs[0], waterYs[1]);
			waterShape.lineTo(waterXs[1], waterYs[1]);
			waterShape.lineTo(waterXs[1], waterYs[0]);
			waterShape.lineTo(waterXs[0], waterYs[0]);
			
			var waterGeometry = new $wnd.THREE.ShapeGeometry(waterShape);
			var waterMaterial = new $wnd.THREE.MeshBasicMaterial({color: 0xd2e6f7, opacity: 0.6});
			var mesh = new $wnd.THREE.Mesh(waterGeometry, waterMaterial);
			mesh.translateZ(-0.1);
			this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::addMesh(Lcom/google/gwt/core/client/JavaScriptObject;)(mesh);
		}
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
			face1.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
				legend[i][1], legend[i][2], legend[i][3]
			);
			face1.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
				legend[i][1], legend[i][2], legend[i][3]
			);
			face1.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
				legend[i + 1][1], legend[i + 1][2], legend[i + 1][3]
			);
			
			var face2 = new $wnd.THREE.Face3(2, 3, 0);
			face2.vertexColors[0] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
				legend[i + 1][1], legend[i + 1][2], legend[i + 1][3]
			);
			face2.vertexColors[1] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
				legend[i + 1][1], legend[i + 1][2], legend[i + 1][3]
			);
			face2.vertexColors[2] = this.@pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSliceView::buildColor(DDD)(
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

	private native void drawDevice(Double xCoordinate, double scale, double shiftX, double shiftY) /*-{
		var deviceMaterial = new $wnd.THREE.MeshLambertMaterial({color: 0xf44f4f});
		var device = new $wnd.THREE.SphereGeometry(3, 12, 12);
		var deviceMesh = new $wnd.THREE.Mesh(device, deviceMaterial);
		deviceMesh.position.set(xCoordinate * scale + shiftX, 0.1 * scale + shiftY, 0);
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
	
	private native void buildColor(double r, double g, double b) /*-{
		var rgbValue = "rgb("
				+ r + ", "
				+ g + ", "
				+ b
				+ ")";
		
		return new $wnd.THREE.Color(rgbValue);
	}-*/;
}