package sportliga;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.sql.SQLException;

public class SportLigaApp extends Application {

    Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Аналізатор результатів Спортпрогноз");
        initRootLayout();
    }

    public void initRootLayout() {
        try {
            Database.openConnection();
        } catch (ClassNotFoundException | SQLException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Open Database Exception");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            System.exit(0);
        }
        
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SportLigaApp.class.getResource("SportLigaView.fxml"));
            VBox rootLayout = (VBox) loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setMinWidth(rootLayout.getWidth());
            primaryStage.setMaxWidth(rootLayout.prefWidth(-1));
            primaryStage.setMinHeight(400);

            primaryStage.setOnCloseRequest(we -> {
                try {
                    Database.close();
                } catch (SQLException e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Close Database Exception");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            });
            primaryStage.getIcons().add(new Image("file:img/soccer_ball.png"));

        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("FXML Loading Exception");
            alert.setHeaderText(e.getMessage());
            alert.showAndWait();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
