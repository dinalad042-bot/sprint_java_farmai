module tn.esprit.farmai {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.swing;

    // UI Components
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    // Database
    requires java.sql;
    requires mysql.connector.j;

    // Security (Face Recognition)
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;
    requires transitive java.desktop; // For BufferedImage/Java2D
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    // Email
    requires jakarta.mail;
    requires jakarta.activation;

    // PDF - Apache PDFBox
    requires org.apache.pdfbox;

    // iText PDF
    requires kernel;
    requires layout;
    requires io;

    // JSON
    requires org.json;

    // JavaScript (for WebView)
    requires jdk.jsobject;

    // HTTP
    requires java.net.http;

    // Main package
    opens tn.esprit.farmai to javafx.fxml;
    exports tn.esprit.farmai;

    // Controllers package
    opens tn.esprit.farmai.controllers to javafx.fxml;
    exports tn.esprit.farmai.controllers;

    // Models package
    opens tn.esprit.farmai.models to javafx.fxml, javafx.base;
    exports tn.esprit.farmai.models;

    // Services package
    exports tn.esprit.farmai.services;

    // Utils package
    exports tn.esprit.farmai.utils;

    // Interfaces package
    exports tn.esprit.farmai.interfaces;

    // Test package (JUnit requires removed - test scope)
    opens tn.esprit.farmai.test to javafx.fxml, javafx.graphics;
    exports tn.esprit.farmai.test;
}
