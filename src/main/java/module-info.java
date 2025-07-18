module org.example.imageconverter {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.apache.pdfbox;


    opens org.example.imageconverter to javafx.fxml;
    exports org.example.imageconverter;
}