package tn.esprit.farmai.controllers;

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
import tn.esprit.farmai.services.VoiceRecorder; // Assure-toi que l'import est correct
import tn.esprit.farmai.utils.NavigationUtil;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Function;

public class AnimauxController implements Initializable {

    // Éléments FXML
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
    @FXML private Button btnStopVoice; // Assure-toi d'ajouter ce bouton dans ton FXML

    // Services (Instances non-statiques)
    private final ServiceAnimaux sa = new ServiceAnimaux();
    private final ServiceFerme sf = new ServiceFerme();
    private final VoiceRecorder recorder = new VoiceRecorder();
    private final ExpertChatbotService chatbot = new ExpertChatbotService();
    private final ExpertVoiceService tts = new ExpertVoiceService(); // Instance unique pour le contrôleur

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colEspece.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat_sante"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_naissance"));
        colIdFerme.setCellValueFactory(new PropertyValueFactory<>("id_ferme"));

        // Masquer le bouton stop au démarrage
        if (btnStopVoice != null) btnStopVoice.setVisible(false);

        chargerFermes();
        rafraichir();

        tvAnimaux.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tfEspece.setText(newVal.getEspece());
                tfEtat.setText(newVal.getEtat_sante());
                if (newVal.getDate_naissance() != null) {
                    dpDate.setValue(newVal.getDate_naissance().toLocalDate());
                }
                cbFerme.getItems().stream()
                        .filter(f -> f.getId_ferme() == newVal.getId_ferme())
                        .findFirst().ifPresent(f -> cbFerme.setValue(f));
            }
        });
    }

    // --- ASSISTANT VOCAL AVEC IA REELLE (GROQ/LLAMA3.3) ---

    @FXML
    private void handleVoiceQuery() {
        recorder.startRecording("question_fermier.wav");
        if (lblStatusVocale != null) {
            lblStatusVocale.setText("🎙️ L'expert vous écoute...");
        }

        new Thread(() -> {
            try {
                Thread.sleep(4000); // Enregistrement de 4 secondes
                recorder.stopRecording();

                javafx.application.Platform.runLater(() -> {
                    lblStatusVocale.setText("⏳ Réflexion de l'expert IA...");
                });

                // Question simulée (en attendant STT)
                String questionFermier = "Quels conseils pour un mouton qui ne mange pas ?";
                String reponseIA = chatbot.genererReponseAI(questionFermier);

                javafx.application.Platform.runLater(() -> {
                    if (lblStatusVocale != null) {
                        lblStatusVocale.setText("🤖 IA: " + reponseIA);
                    }

                    // On affiche le bouton stop avant de parler
                    if (btnStopVoice != null) btnStopVoice.setVisible(true);

                    tts.repondre(reponseIA);
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    lblStatusVocale.setText("❌ Erreur de l'IA.");
                });
            }
        }).start();
    }

    @FXML
    private void onStopButtonClicked() {
        // APPEL CORRECT : utilise l'instance 'tts' au lieu de la classe
        tts.arreterLecture();

        if (btnStopVoice != null) btnStopVoice.setVisible(false);
        System.out.println("Lecture vocale interrompue.");
    }

    // --- LOGIQUE CRUD & PDF ---

    private void chargerFermes() {
        try {
            cbFerme.setItems(FXCollections.observableArrayList(sf.selectALL()));
            cbFerme.setConverter(new StringConverter<Ferme>() {
                @Override public String toString(Ferme f) { return (f != null) ? f.getNom_ferme() : ""; }
                @Override public Ferme fromString(String s) { return null; }
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void ajouter() {
        if (!validerChamps()) return;
        try {
            Animaux a = new Animaux(0, tfEspece.getText(), tfEtat.getText(),
                    Date.valueOf(dpDate.getValue()), cbFerme.getValue().getId_ferme());
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
                selected.setEtat_sante(tfEtat.getText());
                selected.setDate_naissance(Date.valueOf(dpDate.getValue()));
                selected.setId_ferme(cbFerme.getValue().getId_ferme());
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
                (Function<Animaux, String>) Animaux::getEtat_sante,
                (Function<Animaux, String>) a -> (a.getDate_naissance() != null) ? a.getDate_naissance().toString() : "N/A",
                (Function<Animaux, String>) a -> String.valueOf(a.getId_ferme())
        };

        try {
            PdfGenerator.generatePdf("Rapport_Animaux.pdf", "Rapport du Bétail - FarmAI",
                    tvAnimaux.getItems(), headers, extractors);
            new Alert(Alert.AlertType.INFORMATION, "PDF généré !").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur PDF : " + e.getMessage()).show();
        }
    }

    private void rafraichir() {
        try { tvAnimaux.setItems(FXCollections.observableArrayList(sa.selectALL())); }
        catch (SQLException e) { e.printStackTrace(); }
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
        // Arrêter la voix si on quitte la page
        tts.arreterLecture();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/selection-gestion.fxml", "Gestion");
    }
}