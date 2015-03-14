package pl.ismop.web.client.widgets.sideprofile;

import thothbot.parallax.core.client.AnimatedScene;
import thothbot.parallax.core.shared.cameras.PerspectiveCamera;
import thothbot.parallax.core.shared.geometries.BoxGeometry;
import thothbot.parallax.core.shared.materials.MeshBasicMaterial;
import thothbot.parallax.core.shared.math.Color;
import thothbot.parallax.core.shared.objects.Mesh;

public class SideProfileScene extends AnimatedScene {
	private Mesh mesh;
	private PerspectiveCamera camera;

	@Override
	protected void onUpdate(double duration) {
		mesh.getRotation().addX(0.005);
		mesh.getRotation().addY(0.01);
		getRenderer().render(getScene(), camera);
	}

	@Override
	protected void onStart() {
		camera = new PerspectiveCamera(70, getRenderer().getAbsoluteAspectRation(), 1, 1000);
		camera.getPosition().setZ(400);

		BoxGeometry geometry = new BoxGeometry(200, 200, 200);
		MeshBasicMaterial material = new MeshBasicMaterial();
		material.setColor(new Color(0xFF0000));
		material.setWireframe(true);
		mesh = new Mesh(geometry, material);
		getScene().add(mesh);
	}
}