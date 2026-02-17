package tn.esprit.farmai.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDBConnexion {
    // 1. Attributs de connexion
    private String url = "jdbc:mysql://localhost:3306/farmai";
    private String user = "root";
    private String pwd = "";

    // 2. Attribut statique pour l'instance unique
    private Connection cnx;
    private static MyDBConnexion instance;

    // 3. Constructeur privé
    private MyDBConnexion() {
        try {
            cnx = DriverManager.getConnection(url, user, pwd);
            System.out.println("Connexion à la base farmai établie avec succès !");
        } catch (SQLException ex) {
            System.err.println("Erreur de connexion : " + ex.getMessage());
        }
    }

    // 4. Méthode pour obtenir l'instance (Singleton)
    public static MyDBConnexion getInstance() {
        if (instance == null) {
            instance = new MyDBConnexion();
        }
        return instance;
    }

    // 5. Getter pour la connexion
    public Connection getCnx() {
        return cnx;
    }
    
    // 6. Méthode pour tester la connexion
    public boolean testConnection() {
        try {
            if (cnx == null || cnx.isClosed()) {
                return false;
            }
            // Simple test query to verify connection
            try (Statement stmt = cnx.createStatement()) {
                stmt.execute("SELECT 1");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
