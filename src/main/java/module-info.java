module com.dev09.kumaracoconutoil {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.naming;
    requires lombok;


    opens com.dev09.kumaracoconutoil.controller to javafx.fxml;
    opens com.dev09.kumaracoconutoil.entity;
    exports com.dev09.kumaracoconutoil;
}