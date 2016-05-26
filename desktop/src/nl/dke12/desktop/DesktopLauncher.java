package nl.dke12.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import nl.dke12.controller.StateController;
import nl.dke12.heightmap.Terra;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "World of Woods";
		config.foregroundFPS = 30;

		StateController stateController = new StateController();
		new LwjglApplication(stateController, config);

		//new LwjglApplication(new Terra(), config);
	}
}
