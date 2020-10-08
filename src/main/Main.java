package main;

import controller.RootLayoutController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import util.ViewLoader;
import util.ViewLoader.View;

public class Main extends Application {
    
	@Override
	public void start(Stage mainStage) {
		mainStage.setTitle("CM2102 Coursework Marker");
		mainStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/cu_logo.png")));
		RootLayoutController controller = new RootLayoutController(mainStage);
		Scene scene = new Scene(ViewLoader.loadFXML(View.ROOT_LAYOUT, controller));
		mainStage.setScene(scene);
		mainStage.setMaximized(true);
		mainStage.show();  
	}

	public static void main(String[] args) {
		launch(args);
	}	
}