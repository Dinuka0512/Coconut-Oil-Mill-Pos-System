module com.dev09.kumaracoconutoil {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.dev09.kumaracoconutoil.controller to javafx.fxml;
    exports com.dev09.kumaracoconutoil;
}