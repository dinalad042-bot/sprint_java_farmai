package tn.esprit.farmai.models;

import java.util.Objects;

/**
 * User entity representing a user in the FarmAI application.
 */
public class User {
    
    private int idUser;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String cin;
    private String adresse;
    private String telephone;
    private String imageUrl;
    private Role role;

    // Default constructor
    public User() {}

    // Constructor without ID (for new users)
    public User(String nom, String prenom, String email, String password, 
                String cin, String adresse, String telephone, String imageUrl, Role role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.cin = cin;
        this.adresse = adresse;
        this.telephone = telephone;
        this.imageUrl = imageUrl;
        this.role = role;
    }

    // Full constructor
    public User(int idUser, String nom, String prenom, String email, String password, 
                String cin, String adresse, String telephone, String imageUrl, Role role) {
        this.idUser = idUser;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.cin = cin;
        this.adresse = adresse;
        this.telephone = telephone;
        this.imageUrl = imageUrl;
        this.role = role;
    }

    // Getters and Setters
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Get full name (prenom + nom)
     */
    public String getFullName() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return idUser == user.idUser && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "idUser=" + idUser +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", cin='" + cin + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", role=" + role +
                '}';
    }
}
