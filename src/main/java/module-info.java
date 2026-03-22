module com.dev09.kumaracoconutoil {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.naming;


    opens com.dev09.kumaracoconutoil.controller to javafx.fxml;
    exports com.dev09.kumaracoconutoil;
}