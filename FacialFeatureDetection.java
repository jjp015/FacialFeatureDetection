import org.opencv.core.Core;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

/**
 * This class creates the style of the program and run the
 * video capture to detect faces and facial features
 */
public class FacialFeatureDetection extends Application
{
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource(
            "FaceDetection.fxml"));
			BorderPane root = (BorderPane) loader.load();
			root.setStyle("-fx-background-color: black;");
			Scene scene = new Scene(root, 950, 750);
			scene.getStylesheets().add(getClass().getResource(
            "application.css").toExternalForm());
			primaryStage.setTitle("Face Detection");
			primaryStage.setScene(scene);
			primaryStage.show();

			FaceDetectionController controller = loader.getController();
			controller.init();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

  /*
   * The main method starts at program execution and launches the program
   * @param args array of strings
   */
	public static void main(String[] args)
	{
    System.setProperty("java.library.path", "/home/linux/ieng6/cs11wb/cs11wbek/Final/x64/" +
        "opencv_java310");

    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		launch(args);
	}
}