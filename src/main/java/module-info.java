module tn.esprit.farmai {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.swing;
    requires jakarta.mail;
    requires jakarta.activation;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    // Commented out for simpler build - uncomment if needed
    // requires eu.hansolo.tilesfx;
    // requires com.almasb.fxgl.all;
    requires java.sql;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.platform.commons;
    requires org.junit.platform.engine;
    requires org.junit.platform.launcher;

    // Face Recognition Dependencies
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;
    requires transitive java.desktop; // For BufferedImage/Java2D
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    // Main package
    opens tn.esprit.farmai to javafx.fxml;

    exports tn.esprit.farmai;

    // Controllers package
    opens tn.esprit.farmai.controllers to javafx.fxml;

    // Test Package
    exports tn.esprit.farmai.test;

    opens tn.esprit.farmai.test to org.junit.platform.commons, org.junit.platform.engine;

    exports tn.esprit.farmai.controllers;

    // Models package
    opens tn.esprit.farmai.models to javafx.fxml, javafx.base;

    exports tn.esprit.farmai.models;

    // Services package
    exports tn.esprit.farmai.services;

    // Utils package
    exports tn.esprit.farmai.utils;

    opens tn.esprit.farmai.utils to javafx.fxml;

    // Interfaces package
    exports tn.esprit.farmai.interfaces;
}