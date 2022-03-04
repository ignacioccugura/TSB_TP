module com.example.tptsb {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens com.example.tptsb to javafx.fxml;
    exports com.example.tptsb;
}