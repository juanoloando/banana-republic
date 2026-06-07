package com.bananarepublic.view.util;

import java.io.File;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class DialogUtil {

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static File showOpenFile(String title, Window owner, FileChooser.ExtensionFilter... filters) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(filters);
        return chooser.showOpenDialog(owner);
    }

    public static File showSaveFile(String title, String initialFileName, Window owner,
                                    FileChooser.ExtensionFilter... filters) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        if (initialFileName != null) chooser.setInitialFileName(initialFileName);
        chooser.getExtensionFilters().addAll(filters);
        return chooser.showSaveDialog(owner);
    }
}
