module tn.esprit.farmai {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    // Commented out for simpler build - uncomment if needed
    // requires eu.hansolo.tilesfx;
    // requires com.almasb.fxgl.all;
    requires java.sql;
    requires mysql.connector.j;
    
    // PDF Generation - Apache PDFBox
    requires org.apache.pdfbox;
    requires java.desktop; // For ImageIO and BufferedImage

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

    // Test package - for JavaFX test applications
    opens tn.esprit.farmai.test to javafx.fxml, javafx.graphics;

    exports tn.esprit.farmai.test;
}
