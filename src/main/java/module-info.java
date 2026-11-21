module com.dev09.kumaracoconutoil {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.naming;
    requires org.apache.pdfbox;
    requires openhtmltopdf.pdfbox;
    requires openhtmltopdf.core;
    requires de.rototor.pdfbox.graphics2d;
    requires java.desktop;
    requires java.logging;
    requires escpos.coffee;

    opens com.dev09.kumaracoconutoil.dto to javafx.base;
    opens com.dev09.kumaracoconutoil.controller to javafx.fxml;
    opens com.dev09.kumaracoconutoil.config to javafx.fxml, org.hibernate.orm.core;
    opens com.dev09.kumaracoconutoil.entity to javafx.fxml, javafx.base, org.hibernate.orm.core;
    opens com.dev09.kumaracoconutoil.view.tm to javafx.base;
    exports com.dev09.kumaracoconutoil;
}
