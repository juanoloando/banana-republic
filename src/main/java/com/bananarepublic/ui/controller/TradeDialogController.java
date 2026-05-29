package com.bananarepublic.ui.controller;

import javafx.scene.control.Alert;

public class TradeDialogController {

    public static void showDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Trade");
        alert.setHeaderText("Trade");
        alert.setContentText("Trade dialog belum diimplementasikan.");
        alert.showAndWait();
    }
}
