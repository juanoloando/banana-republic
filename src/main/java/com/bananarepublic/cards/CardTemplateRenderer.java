package com.bananarepublic.cards;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

public class CardTemplateRenderer {

    public StackPane buildCardNode(CardDefinition definition,
                                   int quantity,
                                   boolean selected,
                                   boolean disabled) {
        return buildCardNode(definition, quantity, selected, disabled, 135, 202);
    }

    public StackPane buildCardNode(CardDefinition definition,
                                   int quantity,
                                   boolean selected,
                                   boolean disabled,
                                   double width,
                                   double height) {
        ImageView cardTemplate = new ImageView(new Image(
                getClass().getResource(definition.getTemplateImagePath()).toExternalForm()));
        cardTemplate.setFitWidth(width);
        cardTemplate.setFitHeight(height);
        cardTemplate.setPreserveRatio(false);
        cardTemplate.setSmooth(true);

        double titleFontSize = Math.max(8, Math.min(12, height * 0.06));
        Label titleLabel = new Label(definition.getDisplayName().toUpperCase());
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(width * 0.82);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setStyle("-fx-text-fill: #2f1f12; -fx-font-size: " + titleFontSize + "px; -fx-font-weight: bold;"
                + "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.32), 1, 0.2, 0, 0);");

        double qtyFontSize = Math.max(8, Math.min(11, height * 0.055));
        Label qtyLabel = new Label("x" + quantity);
        qtyLabel.setStyle("-fx-text-fill: #3f2d17; -fx-font-size: " + qtyFontSize + "px; -fx-font-weight: bold;");

        StackPane root = new StackPane(cardTemplate);
        root.setPrefSize(width, height);
        root.setMaxSize(width, height);
        root.setStyle(selected
                ? "-fx-border-color: #ffb11b; -fx-border-width: 3; -fx-border-radius: 8;"
                : "-fx-border-color: transparent; -fx-border-width: 0;");

        StackPane.setAlignment(titleLabel, Pos.BOTTOM_CENTER);
        titleLabel.setTranslateY(-Math.max(8, height * 0.10));
        root.getChildren().add(titleLabel);

        StackPane.setAlignment(qtyLabel, Pos.TOP_LEFT);
        qtyLabel.setTranslateX(Math.max(4, width * 0.06));
        qtyLabel.setTranslateY(Math.max(3, height * 0.03));
        root.getChildren().add(qtyLabel);

        if (disabled) {
            root.setOpacity(0.50);
        }

        return root;
    }
}
