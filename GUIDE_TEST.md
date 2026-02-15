# Guide de Test - Module Expertise FarmAI

## 1. Test de la Connexion JDBC (MyDBConnexion)

### Test 1.1 : Vérifier le Singleton
```java
// Dans TestAnalyse.java ou une classe Main temporaire
MyDBConnexion conn1 = MyDBConnexion.getInstance();
MyDBConnexion conn2 = MyDBConnexion.getInstance();
System.out.println("Même instance ? " + (conn1 == conn2)); // Doit afficher true
System.out.println("Connexion active ? " + (conn1.getCnx() != null)); // Doit afficher true
```

### Test 1.2 : Vérifier la connexion à la base farmai
1. Assurez-vous que MySQL est démarré
2. Vérifiez que la base `farmai` existe
3. Exécutez TestAnalyse.java
4. Vous devriez voir : "Connexion à la base farmai établie avec succès !"

---

## 2. Test des Services (AnalyseService & ConseilService)

### Test 2.1 : Exécuter TestAnalyse.java
```bash
# Dans IntelliJ :
1. Ouvrir src/main/java/tn/esprit/farmai/test/TestAnalyse.java
2. Clic droit → Run 'TestAnalyse.main()'
```

**Résultats attendus :**
- ✓ Insertion d'une analyse avec succès
- ✓ Récupération de la liste des analyses
- ✓ Insertion d'un conseil lié à une analyse
- ✓ Récupération des conseils par priorité

### Test 2.2 : Vérifier en base de données
```sql
-- Connectez-vous à MySQL et vérifiez :
USE farmai;
SELECT * FROM analyse ORDER BY id_analyse DESC LIMIT 5;
SELECT * FROM conseil ORDER BY id_conseil DESC LIMIT 5;
```

---

## 3. Test des Interfaces Graphiques

### Test 3.1 : Lancer l'application principale
```bash
# Dans IntelliJ :
1. Ouvrir src/main/java/tn/esprit/farmai/HelloApplication.java
2. Clic droit → Run 'HelloApplication.main()'
```

### Test 3.2 : Tester GestionAnalyses.fxml
**Option A : Intégration dans le dashboard Expert**
Ajoutez un bouton dans le dashboard expert qui ouvre GestionAnalyses :

```java
// Dans ExpertDashboardController.java
@FXML
private void handleGestionAnalyses() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
            "/tn/esprit/farmai/views/gestion-analyses.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Gestion des Analyses");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

**Option B : Test direct avec une classe Main**
Créez une classe de test rapide :

```java
package tn.esprit.farmai.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestGestionAnalyses extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(
            "/tn/esprit/farmai/views/gestion-analyses.fxml"));
        primaryStage.setTitle("Gestion des Analyses - Test");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### Test 3.3 : Tester AjoutConseil.fxml
**Test direct :**
```java
package tn.esprit.farmai.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestAjoutConseil extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(
            "/tn/esprit/farmai/views/ajout-conseil.fxml"));
        primaryStage.setTitle("Ajouter un Conseil - Test");
        primaryStage.setScene(new Scene(root, 600, 700));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## 4. Test du Mécanisme 1:N (Analyse → Conseils)

### Scénario de test complet :

```java
// Dans TestAnalyse.java ou une nouvelle classe
public class TestMechanisme1N {
    public static void main(String[] args) {
        AnalyseService analyseService = new AnalyseService();
        ConseilService conseilService = new ConseilService();

        try {
            // 1. Créer une analyse
            Analyse analyse = new Analyse();
            analyse.setDateAnalyse(LocalDateTime.now());
            analyse.setResultatTechnique("Test mécanisme 1:N");
            analyse.setIdTechnicien(1);
            analyse.setIdFerme(1);
            analyse.setImageUrl("/test/image.jpg");
            
            analyseService.insertOne(analyse);
            System.out.println("✓ Analyse créée avec ID: " + analyse.getIdAnalyse());

            // 2. Créer plusieurs conseils liés à cette analyse
            Conseil conseil1 = new Conseil();
            conseil1.setDescriptionConseil("Premier conseil pour cette analyse");
            conseil1.setPriorite(Priorite.HAUTE);
            conseil1.setIdAnalyse(analyse.getIdAnalyse());
            
            Conseil conseil2 = new Conseil();
            conseil2.setDescriptionConseil("Deuxième conseil pour cette analyse");
            conseil2.setPriorite(Priorite.MOYENNE);
            conseil2.setIdAnalyse(analyse.getIdAnalyse());
            
            conseilService.insertOne(conseil1);
            conseilService.insertOne(conseil2);
            
            System.out.println("✓ Conseils créés avec IDs: " + 
                conseil1.getIdConseil() + ", " + conseil2.getIdConseil());

            // 3. Vérifier la récupération des conseils par analyse
            List<Conseil> conseils = conseilService.findByAnalyse(analyse.getIdAnalyse());
            System.out.println("✓ Nombre de conseils pour l'analyse " + 
                analyse.getIdAnalyse() + ": " + conseils.size());

            // 4. Vérifier en SQL
            System.out.println("\nVérifiez en SQL :");
            System.out.println("SELECT * FROM conseil WHERE id_analyse = " + analyse.getIdAnalyse());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

---

## 5. Checklist de Validation

### Base de données
- [ ] MySQL est démarré
- [ ] Base `farmai` existe
- [ ] Tables `analyse` et `conseil` créées
- [ ] Connexion JDBC fonctionne (MyDBConnexion)

### Modèles
- [ ] Classe Analyse avec tous les attributs
- [ ] Classe Conseil avec relation 1:N
- [ ] Enum Priorite (BASSE, MOYENNE, HAUTE)

### Services
- [ ] AnalyseService implémente CRUD
- [ ] ConseilService implémente CRUD
- [ ] PreparedStatement utilisés partout
- [ ] Gestion des exceptions SQLException

### Interfaces
- [ ] GestionAnalyses.fxml s'affiche correctement
- [ ] TableView affiche les données
- [ ] AjoutConseil.fxml s'affiche correctement
- [ ] ComboBox Analyse fonctionne (relation 1:N)
- [ ] Validation des champs (US11)
- [ ] Image preview fonctionne (US6)

### Intégration
- [ ] Module-info.java configuré correctement
- [ ] Navigation entre les vues fonctionne
- [ ] Styles CSS cohérents

---

## 6. Commandes SQL pour vérifier

```sql
-- Vérifier les tables
SHOW TABLES;

-- Structure de la table analyse
DESCRIBE analyse;

-- Structure de la table conseil
DESCRIBE conseil;

-- Dernières analyses insérées
SELECT * FROM analyse ORDER BY id_analyse DESC LIMIT 5;

-- Derniers conseils avec leurs analyses
SELECT c.*, a.date_analyse, a.resultat_technique 
FROM conseil c 
JOIN analyse a ON c.id_analyse = a.id_analyse 
ORDER BY c.id_conseil DESC LIMIT 5;

-- Nombre de conseils par analyse
SELECT a.id_analyse, COUNT(c.id_conseil) as nb_conseils
FROM analyse a
LEFT JOIN conseil c ON a.id_analyse = c.id_analyse
GROUP BY a.id_analyse;
```

---

## 7. Résolution des Problèmes Courants

### Problème : "Cannot resolve symbol 'javafx'"
**Solution :** File → Invalidate Caches / Restart → Invalidate and Restart

### Problème : FXML ne se charge pas
**Solution :** Vérifier le chemin dans le FXMLLoader :
```java
"/tn/esprit/farmai/views/gestion-analyses.fxml"
```

### Problème : Connexion MySQL échoue
**Solution :** Vérifier dans MyDBConnexion.java :
- URL : `jdbc:mysql://localhost:3306/farmai`
- User : `root`
- Password : `` (vide)
- MySQL est bien démarré sur le port 3306

### Problème : TableView vide
**Solution :** 
1. Vérifier que des données existent en base
2. Vérifier que `analysesTableView.setItems(analysesList)` est appelé
3. Vérifier les logs pour les erreurs SQL
