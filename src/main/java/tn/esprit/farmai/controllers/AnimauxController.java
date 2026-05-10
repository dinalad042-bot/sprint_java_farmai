package tn.esprit.farmai.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.farmai.models.Animaux;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.services.ServiceAnimaux;
import tn.esprit.farmai.services.ServiceFerme;
import tn.esprit.farmai.services.PdfGenerator;
import tn.esprit.farmai.services.ExpertChatbotService;
import tn.esprit.farmai.services.ExpertVoiceService;
import tn.esprit.farmai.utils.NavigationUtil;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

public class AnimauxController implements Initializable {

    @FXML private TextField tfEspece;
    @FXML private TextField tfEtat;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Ferme> cbFerme;
    @FXML private TextField tfRecherche;
    @FXML private TableView<Animaux> tvAnimaux;
    @FXML private TableColumn<Animaux, String> colEspece;
    @FXML private TableColumn<Animaux, String> colEtat;
    @FXML private TableColumn<Animaux, Date> colDate;
    @FXML private TableColumn<Animaux, Integer> colIdFerme;
    @FXML private Label lblStatusVocale;
    @FXML private Button btnMicro;
    @FXML private Button btnStopVoice;

    private final ServiceAnimaux sa = new ServiceAnimaux();
    private final ServiceFerme sf = new ServiceFerme();
    private final ExpertChatbotService chatbot = new ExpertChatbotService();
    private final ExpertVoiceService tts = new ExpertVoiceService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colEspece.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatSante"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        colIdFerme.setCellValueFactory(new PropertyValueFactory<>("idFerme"));

        if (btnStopVoice != null) btnStopVoice.setVisible(false);

        chargerFermes();
        rafraichir();

        tvAnimaux.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tfEspece.setText(newVal.getEspece());
                tfEtat.setText(newVal.getEtatSante());
                if (newVal.getDateNaissance() != null) {
                    dpDate.setValue(newVal.getDateNaissance().toLocalDate());
                }
                cbFerme.getItems().stream()
                    .filter(f -> f.getIdFerme() == newVal.getIdFerme())
                    .findFirst().ifPresent(f -> cbFerme.setValue(f));
            }
        });
    }

    // --- ASSISTANT VOCAL (Disabled - requires Google Cloud Speech) ---
    @FXML
    private void handleVoiceQuery() {
        if (lblStatusVocale != null) {
            lblStatusVocale.setText("🎙️ Assistant vocal non disponible (Google Cloud Speech non configuré)");
        }
    }

    @FXML
    private void onStopButtonClicked() {
        tts.arreterLecture();
        if (btnStopVoice != null) btnStopVoice.setVisible(false);
    }

    // --- LOGIQUE CRUD ---

    private void chargerFermes() {
try {
int userId = tn.esprit.farmai.utils.SessionManager.getInstance().getCurrentUser().getIdUser();
cbFerme.setItems(FXCollections.observableArrayList(sf.findByFermier(userId)));
cbFerme.setConverter(new StringConverter<Ferme>() {
@Override public String toString(Ferme f) { return (f != null) ? f.getNomFerme() : ""; }
@Override public Ferme fromString(String s) { return null; }
});
} catch (SQLException e) { e.printStackTrace(); }
}

private List<Integer> getUserFermeIds() {
try {
return sf.getFermeIdsByFermier(
tn.esprit.farmai.utils.SessionManager.getInstance().getCurrentUser().getIdUser()
);
} catch (SQLException e) { e.printStackTrace(); return new java.util.ArrayList<>(); }
}

private List<Animaux> getAnimauxByUserFermes() {
try {
List<Integer> ids = getUserFermeIds();
if (ids.isEmpty()) return new java.util.ArrayList<>();
return sa.findByFermes(ids);
} catch (SQLException e) { e.printStackTrace(); return new java.util.ArrayList<>(); }
}

private void rafraichir() {
try { tvAnimaux.setItems(FXCollections.observableArrayList(getAnimauxByUserFermes())); }
catch (Exception e) { e.printStackTrace(); }
}

    @FXML
    private void ajouter() {
        if (!validerChamps()) return;
        try {
            Animaux a = new Animaux(0, tfEspece.getText(), tfEtat.getText(),
                Date.valueOf(dpDate.getValue()), cbFerme.getValue().getIdFerme());
            sa.insertOne(a);
            rafraichir();
            viderChamps();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void modifier() {
        Animaux selected = tvAnimaux.getSelectionModel().getSelectedItem();
        if (selected != null && validerChamps()) {
            try {
                selected.setEspece(tfEspece.getText());
                selected.setEtatSante(tfEtat.getText());
                selected.setDateNaissance(Date.valueOf(dpDate.getValue()));
                selected.setIdFerme(cbFerme.getValue().getIdFerme());
                sa.updateOne(selected);
                rafraichir();
                viderChamps();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void supprimer() {
        Animaux selected = tvAnimaux.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                sa.deleteOne(selected);
                rafraichir();
                viderChamps();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handleRecherche() {
        String query = tfRecherche.getText().trim();
        try {
            if (query.isEmpty()) rafraichir();
            else tvAnimaux.setItems(FXCollections.observableArrayList(sa.chercherParEspece(query)));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void imprimerPdf() {
        String[] headers = {"Espèce", "Santé", "Date Naissance", "ID Ferme"};
        Function<Animaux, String>[] extractors = new Function[] {
            (Function<Animaux, String>) Animaux::getEspece,
            (Function<Animaux, String>) Animaux::getEtatSante,
            (Function<Animaux, String>) a -> (a.getDateNaissance() != null) ? a.getDateNaissance().toString() : "N/A",
            (Function<Animaux, String>) a -> String.valueOf(a.getIdFerme())
        };

        try {
            PdfGenerator.generatePdf("Rapport_Animaux.pdf", "Rapport du Bétail - FarmAI",
                tvAnimaux.getItems(), headers, extractors);
            new Alert(Alert.AlertType.INFORMATION, "PDF généré !").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur PDF : " + e.getMessage()).show();
        }
    }

    private boolean validerChamps() {
        return !tfEspece.getText().isEmpty() && dpDate.getValue() != null && cbFerme.getValue() != null;
    }

    private void viderChamps() {
        tfEspece.clear(); tfEtat.clear(); dpDate.setValue(null);
        cbFerme.getSelectionModel().clearSelection();
        tvAnimaux.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleReturnToSelection(ActionEvent event) {
        tts.arreterLecture();
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/agricole-dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("FarmAI - Espace Agricole");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NavigationUtil.navigateToDashboard(stage);
        }
    }
}
