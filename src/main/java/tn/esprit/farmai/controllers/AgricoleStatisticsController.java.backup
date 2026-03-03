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
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;
import tn.esprit.farmai.services.FermeService;
import tn.esprit.farmai.utils.NavigationUtil;

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
        loadStatistics();
        loadCharts();
    }

    /**
     * Load statistics from database
     */
    private void loadStatistics() {
        try {
            int totalAnalyses = analyseService.selectALL().size();
            int totalConseils = conseilService.selectALL().size();
            int totalFarms = fermeService.selectALL().size();

            totalAnalysesLabel.setText(String.valueOf(totalAnalyses));
            totalConseilsLabel.setText(String.valueOf(totalConseils));
            totalFarmsLabel.setText(String.valueOf(totalFarms));

            LOGGER.log(Level.INFO, "Agricole Statistics loaded: {0} analyses, {1} conseils, {2} farms",
                    new Object[]{totalAnalyses, totalConseils, totalFarms});

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading statistics", e);
            totalAnalysesLabel.setText("-");
            totalConseilsLabel.setText("-");
            totalFarmsLabel.setText("-");
        }
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
     * Load farm analysis frequency bar chart
     */
    private void loadFarmBarChart() {
        try {
            List<Object[]> farmStats = analyseService.getAnalysisPerFarmStats();
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