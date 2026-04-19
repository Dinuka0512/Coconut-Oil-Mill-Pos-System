module com.dev09.kumaracoconutoil {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.naming;
    requires lombok;
    requires org.apache.pdfbox;
    requires java.desktop;

    opens com.dev09.kumaracoconutoil.dto to javafx.base;
    opens com.dev09.kumaracoconutoil.controller to javafx.fxml;
    opens com.dev09.kumaracoconutoil.entity;
    exports com.dev09.kumaracoconutoil;
}