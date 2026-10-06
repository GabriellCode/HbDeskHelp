module com.hb.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.hb.desktop to javafx.fxml;
    exports com.hb.desktop;
}
