package com.bananarepublic.ui.controller;

import javafx.scene.control.Alert;

public class InventoryDialogController {

    public static void showDialog(int playerIndex, String playerName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inventory - " + playerName);
        alert.setHeaderText("Player " + playerIndex + ": " + playerName);
        alert.setContentText("Inventory dialog belum diimplementasikan.");
        alert.showAndWait();
    }
}
