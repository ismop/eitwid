package pl.ismop.web.client.widgets.analysis.horizontalslice;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.dev.shell.Messages;
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
	
	private JavaScriptObject scene;

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
	public void drawCrosssection(String parameterUnit, double minValue, double maxValue) {
		addRenderer(panel.getElement(), panel.getOffsetWidth(), panel.getOffsetHeight());
		drawLegend(0xecf330, 0x307bf3, minValue, maxValue, parameterUnit);
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
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene.add(line);
		
		var scaleText = new $wnd.THREE.TextGeometry("10 m", {
			font: 'optimer',
			size: 12,
			height: 0.5,
			curveSegments: 30
		});
		var scaleTextMaterial = new $wnd.THREE.MeshLambertMaterial({color: 0x0000ff});
		var scaleTextMesh = new $wnd.THREE.Mesh(scaleText, scaleTextMaterial);
		scaleTextMesh.position.set(panX, 10, 0);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene.add(scaleTextMesh);
	}-*/;

	private String format(double number) {
		return NumberFormat.getFormat("0.00").format(number);
	}

	private native void drawSection(JsArray<JsArrayNumber> nativeCoordinates) /*-{
		var shape = new $wnd.THREE.Shape();
		shape.moveTo(nativeCoordinates[0][0], nativeCoordinates[0][1], 0);
		
		for(var i = 1; i < nativeCoordinates.length; i++) {
			shape.lineTo(nativeCoordinates[i][0], nativeCoordinates[i][1], 0);
		}
		
		shape.lineTo(nativeCoordinates[0][0], nativeCoordinates[0][1], 0);
		
		var geometry = new $wnd.THREE.ShapeGeometry(shape);
		var mesh = new $wnd.THREE.Mesh(geometry, new $wnd.THREE.MeshBasicMaterial({color: 0x555555}));
		var wireframe = new $wnd.THREE.EdgesHelper(mesh, 0x383838);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene.add(mesh);
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene.add(wireframe);
	}-*/;

	private native void drawLegend(int topColor, int bottomColor, double bottomValue, double topValue, String parameterUnit) /*-{
		var bottom = new $wnd.THREE.Color(bottomColor);
		var top = new $wnd.THREE.Color(topColor);
		var levels = 5;
		var height = 290;
		var lift = 5;
		var moveLeft = 5;
		var levelHeight = height / levels;
		var levelWidth = 100
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
			this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene.add(mesh);
			this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene.add(wireframe);
			
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
			this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene.add(tickMesh);
			
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
				this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene.add(tickMesh);
			}
		}
	}-*/;

	private native void addRenderer(Element element, int width, int height) /*-{
		var scene = new $wnd.THREE.Scene();
		this.@pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSliceView::scene = scene;
		
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
}