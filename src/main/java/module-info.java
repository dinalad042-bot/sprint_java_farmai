module tn.esprit.farmai {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    // Commented out for simpler build - uncomment if needed
    // requires eu.hansolo.tilesfx;
    // requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.desktop;
    requires java.net.http;

    requires kernel;
    requires layout;
    requires io;
    requires org.json;

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
}