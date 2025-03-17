module org.example.cititorscriitor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.example.cititorscriitor to javafx.fxml;
    exports org.example.cititorscriitor;
}