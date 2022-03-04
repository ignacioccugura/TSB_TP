package com.example.tptsb;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
    public void pri(ActionEvent event){
        System.out.println("aaa");
    }
    ObservableList<String> items = FXCollections.observableArrayList();
@FXML
private void initialize(){

}
public void dat(ActionEvent event){
    System.out.println("funca");
}
public void botonpressed(ActionEvent ja){
    System.out.println(ja);
}

}
