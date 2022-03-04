package com.example.tptsb;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;


public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {



       ComboBox<String> combobox= new ComboBox<String>();
       combobox.getItems().addAll("Todos los departamentos","Calamuchita","Capital","Colón","Cruz del Eje","General Roca","General San Martín","Ischilín","Juárez Celman","Marcos Juárez","Minas","Pocho","Presidente Roque Sáenz Peña","Punilla","Río Cuarto","Río Primero","Río Seco","Río Segundo","San Alberto","San Javier","San Justo","Santa María","Sobremonte","Tercero Arriba","Totoral","Tulumba","Unión");
       ComboBox<String> comboboxFiltro= new ComboBox<String>();
       comboboxFiltro.getItems().addAll("Cantidad de hombres y mujeres vacunadas", "Diferenciación por vacuna", "Diferenciación por orden de vacuna (primera o segunda dosis");
        Label label=new Label();
        ListView listView = new ListView();




        Label label1= new Label();
        Button botonInicio= new Button();
        botonInicio.setText("Iniciar carga de datos");
Label label2 =new Label();
Label label3=new Label();
label3.setText("Seleccione un filtro:");
label2.setText("Seleccione un departamento:");
       VBox layout = new VBox(10);
        layout.setPadding(new Insets(20,20,20,20));
        Button boton= new Button();
        boton.setText("Calcular");
        Dialog<String> dialog = new Dialog<>();
        dialog.getDialogPane();
        boton.setDisable(true);
        layout.getChildren().addAll(label1,botonInicio,boton,label2,combobox,label3,comboboxFiltro,listView);
        CargaDatos carga=new CargaDatos();

        Scene scene=new Scene(layout,600,400);
        stage.setScene(scene);
        stage.setTitle("Registros de vacunación COVID-19");

        stage.show();
        botonInicio.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Carga de datos");
                    alert.setHeaderText("Presione OK para iniciar la carga");
                    alert.setContentText("Aguarde por favor, puede demorar entre 1 y 5 minutos dependiendo de su dispositivo");
                    alert.showAndWait();
                    TSBHashTableDA tabla=carga.carga();
                    label1.setText("Datos cargados correctamente");
                    boton.setDisable(false);
                    botonInicio.setDisable(true);

                    boton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            listView.getItems().clear();
                            if (combobox.getValue()==null||comboboxFiltro.getValue()==null){
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Cuidado!");
                                alert.setHeaderText("Seleccione una opción de cada lista desplegable por favor");
                                alert.setContentText("O apruébenos, más fácil");
                                alert.showAndWait();
                                return;
                            }
                            int index=0;
                            for (int i = 0; i < comboboxFiltro.getItems().size(); i++) {
                                if (comboboxFiltro.getItems().toArray()[i]==comboboxFiltro.getValue()){
                                    index=i;
                                    break;

                                }
                            }
                            TSBHashTableDA<String,Acumulador> a = carga.buscarDatos(index,combobox.getValue(),tabla);
                            Set<Map.Entry<String,Acumulador>> recorrer = a.entrySet();
                            Iterator<Map.Entry<String,Acumulador>> it = recorrer.iterator();
                            String cadena="";
                            while(it.hasNext()){
                                Map.Entry<String,Acumulador> entry = it.next();
                                cadena+="total de " + entry.getValue().getId() + ": " + entry.getValue().getTotal()+"\n";


                            }
                            listView.getItems().add(cadena);

                        }
                    });

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });


        //FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        //Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        //stage.setTitle("Hello!");
        //stage.setScene(scene);
        //stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}