package tn.esprit.farmai.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDBConnexion {

    private static final String USERNAME = "2sXQZXGXGsoK1vx.root";
    private static final String PASSWORD = "qnysm2y695RfCJg9";
    private static final String URL = "jdbc:mysql://gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/farmia_new?sslMode=VERIFY_IDENTITY";

    private Connection cnx;

    //2nd Step = CREATE A STATIC VARIABLE AS THE SAME TYPE OF THE CLASS
    private static MyDBConnexion instance;

    //1st Step = MAKE CONSTRUCTOR PRIVATE
    private MyDBConnexion(){

        try {
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion etablie!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

    }

    //3nd Step = CREATE A STATIC METHOD TO RETURN THE INSTANCE
    public static MyDBConnexion getInstance(){
        if (instance == null) instance = new MyDBConnexion();
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}