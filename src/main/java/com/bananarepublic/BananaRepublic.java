package com.bananarepublic;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BananaRepublic extends Application {
    private static final double MIN_WINDOW_WIDTH = 1200;
    private static final double MIN_WINDOW_HEIGHT = 675;
    private static final double DEFAULT_WINDOW_WIDTH = 1280;
    private static final double DEFAULT_WINDOW_HEIGHT = 720;
    private static final String APP_TITLE = "Banana Republic";
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.setResizable(true);
        primaryStage.setWidth(DEFAULT_WINDOW_WIDTH);
        primaryStage.setHeight(DEFAULT_WINDOW_HEIGHT);
        primaryStage.centerOnScreen();
        primaryStage.setMaximized(true);
        showMainMenu();
    }

    public static void showMainMenu() throws Exception {
        switchScene("/com/bananarepublic/fxml/main-menu.fxml");
    }

    public static void showLobby() throws Exception {
        switchScene("/com/bananarepublic/fxml/lobby.fxml");
    }

    public static void showGame() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                BananaRepublic.class.getResource("/com/bananarepublic/fxml/game.fxml")
        );

        Scene scene = new Scene(loader.load(), 1280, 720);
        scene.getStylesheets().add(
                BananaRepublic.class.getResource("/com/bananarepublic/css/app.css").toExternalForm()
        );

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public static void showLoadGame() throws Exception {
        switchScene("/com/bananarepublic/fxml/load-game.fxml");
    }

    private static void switchScene(String fxmlPath) throws IOException {
        boolean wasMaximized = primaryStage.isMaximized();
        double widthBeforeSwitch = primaryStage.getWidth();
        double heightBeforeSwitch = primaryStage.getHeight();

        Scene scene = createScene(fxmlPath);

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);

        if (wasMaximized) {
            primaryStage.setMaximized(true);
        } else if (widthBeforeSwitch > 0 && heightBeforeSwitch > 0) {
            primaryStage.setWidth(widthBeforeSwitch);
            primaryStage.setHeight(heightBeforeSwitch);
        } else {
            primaryStage.setWidth(DEFAULT_WINDOW_WIDTH);
            primaryStage.setHeight(DEFAULT_WINDOW_HEIGHT);
        }

        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    private static Scene createScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(BananaRepublic.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        scene.getStylesheets().add(
                BananaRepublic.class.getResource("/com/bananarepublic/css/app.css").toExternalForm()
        );
        return scene;
    }
}

