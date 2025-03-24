package org.example.cititorscriitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("cititor-scriitor.fxml"));
        Pane root = loader.load();

        // Accesăm controller-ul pentru a ne asigura că se inițializează corect
        BookSimulation controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Writer and Reader Simulation");
        stage.show();
    }
}
