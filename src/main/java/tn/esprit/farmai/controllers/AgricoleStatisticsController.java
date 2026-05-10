package tn.esprit.farmai.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;
import tn.esprit.farmai.services.FermeService;
import tn.esprit.farmai.utils.AvatarUtil;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Agricole Statistics View.
 * Read-only statistics display for agricole users.
 * Simplified version of StatisticsController without expert features.
 */
public class AgricoleStatisticsController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AgricoleStatisticsController.class.getName());

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Circle userAvatarCircle;
    @FXML
    private Circle headerAvatarCircle;

    @FXML
    private Label totalAnalysesLabel;
    @FXML
    private Label totalConseilsLabel;
    @FXML
    private Label totalFarmsLabel;
    @FXML
    private Label priorityLegendLabel;

    @FXML
    private PieChart priorityPieChart;
    @FXML
    private BarChart<String, Number> farmBarChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private final AnalyseService analyseService;
    private final ConseilService conseilService;
    private final FermeService fermeService;

    public AgricoleStatisticsController() {
        this.analyseService = new AnalyseService();
        this.conseilService = new ConseilService();
        this.fermeService = new FermeService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeUserData();
        loadStatistics();
        loadCharts();
    }

    /**
     * Initialize user data from SessionManager - ensures consistent avatar display
     */
    private void initializeUserData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (welcomeLabel != null) {
                welcomeLabel.setText(currentUser.getFullName());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(ProfileManager.getStandardizedRoleLabel(currentUser));
            }
            AvatarUtil.loadUserImageIntoCircle(userAvatarCircle, currentUser);
            AvatarUtil.loadUserImageIntoCircle(headerAvatarCircle, currentUser);
        }

        SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                javafx.application.Platform.runLater(() -> {
                    if (welcomeLabel != null) welcomeLabel.setText(newUser.getFullName());
                    if (userRoleLabel != null) {
                        userRoleLabel.setText(ProfileManager.getStandardizedRoleLabel(newUser));
                    }
                    AvatarUtil.loadUserImageIntoCircle(userAvatarCircle, newUser);
                    AvatarUtil.loadUserImageIntoCircle(headerAvatarCircle, newUser);
                });
            }
        });
    }

    /**
     * Load statistics from database - filtered by current user's farms
     */
    private void loadStatistics() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                setDefaultStatistics();
                return;
            }
            int userId = currentUser.getIdUser();

            // Get user's own ferme IDs
            List<Integer> userFermeIds = fermeService.getFermeIdsByFermier(userId);

            // Total farms for this user
            int totalFarms = userFermeIds.size();

            // Total analyses for user's farms only
            int totalAnalyses = 0;
            if (!userFermeIds.isEmpty()) {
                totalAnalyses = analyseService.findByFermes(userFermeIds).size();
            }

            // Total conseils (global - would need more complex filtering by analyse IDs)
            int totalConseils = conseilService.selectALL().size();

            totalAnalysesLabel.setText(String.valueOf(totalAnalyses));
            totalConseilsLabel.setText(String.valueOf(totalConseils));
            totalFarmsLabel.setText(String.valueOf(totalFarms));

            LOGGER.log(Level.INFO, "Agricole Statistics loaded: {0} analyses, {1} conseils, {2} farms",
                    new Object[]{totalAnalyses, totalConseils, totalFarms});

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading statistics", e);
            setDefaultStatistics();
        }
    }

    private void setDefaultStatistics() {
        totalAnalysesLabel.setText("-");
        totalConseilsLabel.setText("-");
        totalFarmsLabel.setText("-");
    }

    /**
     * Load charts data
     */
    private void loadCharts() {
        loadPriorityPieChart();
        loadFarmBarChart();
    }

    /**
     * Load priority distribution pie chart
     */
    private void loadPriorityPieChart() {
        try {
            List<Object[]> priorityStats = analyseService.getConseilPriorityStats();
            priorityPieChart.getData().clear();

            for (Object[] stat : priorityStats) {
                String priority = (String) stat[0];
                int count = (Integer) stat[1];
                priorityPieChart.getData().add(new PieChart.Data(priority + " (" + count + ")", count));
            }

            LOGGER.log(Level.INFO, "Priority pie chart loaded with {0} categories", priorityStats.size());

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to load priority statistics", e);
        }
    }

    /**
     * Load farm analysis frequency bar chart - filtered by user's farms
     */
    private void loadFarmBarChart() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                return;
            }
            int userId = currentUser.getIdUser();
            List<Integer> userFermeIds = fermeService.getFermeIdsByFermier(userId);

            if (userFermeIds.isEmpty()) {
                farmBarChart.getData().clear();
                return;
            }

            List<Object[]> farmStats = analyseService.getAnalysisPerFarmStats(userFermeIds);
            farmBarChart.getData().clear();

            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Analyses par Ferme");

            for (Object[] stat : farmStats) {
                int farmId = (Integer) stat[0];
                int count = (Integer) stat[1];
                series.getData().add(new javafx.scene.chart.XYChart.Data<>("Farm " + farmId, count));
            }

            farmBarChart.getData().add(series);
            LOGGER.log(Level.INFO, "Farm bar chart loaded with {0} farms", farmStats.size());

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to load farm statistics", e);
        }
    }

    @FXML
    private void handleRefreshData() {
        loadStatistics();
        loadCharts();
        LOGGER.log(Level.INFO, "Statistics refreshed by agricole user");
    }

    @FXML
    private void handleBack() {
        NavigationUtil.navigateToAgricoleDashboard((Stage) welcomeLabel.getScene().getWindow());
    }

    @FXML
    private void handleLogout() {
        NavigationUtil.logout((Stage) welcomeLabel.getScene().getWindow());
    }
}
