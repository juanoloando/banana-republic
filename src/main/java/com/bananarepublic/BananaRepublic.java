package com.bananarepublic;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class BananaRepublic extends Application {
    private static final double MIN_WINDOW_WIDTH = 1200;
    private static final double MIN_WINDOW_HEIGHT = 675;
    private static final String APP_TITLE = "Banana Republic";
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.setResizable(true);
        showMainMenu();
    }

    public static void showMainMenu() throws Exception {
        switchScene("/com/bananarepublic/fxml/main-menu.fxml");
    }

    public static void showLobby() throws Exception {
        switchScene("/com/bananarepublic/fxml/lobby.fxml");
    }

    public static void showGame() throws Exception {
        // game.fxml (old) kept but disabled — active scene is game-revision.fxml
        switchScene("/com/bananarepublic/fxml/game-revision.fxml");
    }

    public static void showLoadGame() throws Exception {
        switchScene("/com/bananarepublic/fxml/load-game.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void switchScene(String fxmlPath) throws IOException {
        Scene scene = createScene(fxmlPath);

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);

        // Explicitly fill the screen so setMaximized no-op on Linux doesn't leave gaps
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
        primaryStage.setMaximized(true);

        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    private static Scene createScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(BananaRepublic.class.getResource(fxmlPath));
        Parent root = loader.load();
        // No fixed scene dimensions — window size drives layout
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                BananaRepublic.class.getResource("/com/bananarepublic/css/app.css").toExternalForm()
        );
        return scene;
    }
}
