package pl.ismop.web.client.widgets.analysis.horizontalslice;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class HorizontalSliceView extends Composite implements IHorizontalSliceView {
	private static HorizontalSliceViewUiBinder uiBinder = GWT.create(HorizontalSliceViewUiBinder.class);

	interface HorizontalSliceViewUiBinder extends UiBinder<Widget, HorizontalSliceView> {}
	
	private JavaScriptObject scene;

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
	public void drawCrosssection() {
		addRenderer(panel.getElement(), panel.getOffsetWidth(), panel.getOffsetHeight());
		drawLegend(0xecf330, 0x307bf3, 15.0, 20.0);
	};
	
	private native void drawLegend(int topColor, int bottomColor, double bottomValue, double topValue) /*-{
		var bottom = new $wnd.THREE.Color(bottomColor);
		var top = new $wnd.THREE.Color(topColor);
		var levels = 5;
		var height = 290;
		var lift = 5;
		var moveLeft = 5;
		var levelHeight = height / levels;
		var levelWidth = 100
		
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
		
//		var raycaster = new $wnd.THREE.Raycaster();
//		
//		var object = this;
//		element.addEventListener('mousemove', function(event) {
//			object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::mouseX =
//				((event.clientX - element.getBoundingClientRect().left) / width ) * 2 - 1;
//			object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::mouseY =
//				-((event.clientY - element.getBoundingClientRect().top) / height ) * 2 + 1;
//		}, false);
//		element.addEventListener('click', function(event) {
//			object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::mouseClicked()();
//		});

		var render = function() {
//			if(object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::mouseX < 2.0 &&
//					object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::mouseY < 2.0) {
//				raycaster.setFromCamera(new $wnd.THREE.Vector2(
//					object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::mouseX,
//					object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::mouseY
//				), camera);
//			}
//			
//			var intersects = raycaster.intersectObjects(object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::devices);
//			
//			if(intersects.length > 0) {
//				if(object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::selectedDevice != null) {
//					if(object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::selectedDevice != intersects[0].object) {
//						object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::selectedDevice.material.transparent = false;
//						object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::selectedDevice.material.opacity = 1.0;
//						object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::deviceSelected(Z)(false);
//						object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::selectedDevice = intersects[0].object;
//						object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::deviceSelected(Z)(true);
//						intersects[0].object.material.transparent = true;
//						intersects[0].object.material.opacity = 0.7;
//					}
//				} else {
//					object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::selectedDevice = intersects[0].object;
//					intersects[0].object.material.transparent = true;
//					intersects[0].object.material.opacity = 0.7;
//					object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::deviceSelected(Z)(true);
//				}
//			} else {
//				if(object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::selectedDevice != null) {
//					object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::selectedDevice.material.transparent = false;
//					object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::selectedDevice.material.opacity = 1.0;
//					object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::deviceSelected(Z)(false);
//					object.@pl.ismop.web.client.widgets.common.profile.SideProfileView::selectedDevice = null;
//				}
//			}
			
			$wnd.requestAnimationFrame(render);
			renderer.render(scene, camera);
		};

		render();
	}-*/;
}