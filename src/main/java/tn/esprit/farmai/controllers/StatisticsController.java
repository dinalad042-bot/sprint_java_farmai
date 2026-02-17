package tn.esprit.farmai.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;
import tn.esprit.farmai.utils.NavigationUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Statistics Dashboard - US10 Implementation
 * Handles data visualization and analytics for FarmAI system
 */
public class StatisticsController implements Initializable {

    @FXML
    private PieChart priorityPieChart;
    @FXML
    private BarChart<String, Number> farmBarChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    
    @FXML
    private Label totalAnalysesLabel;
    @FXML
    private Label totalConseilsLabel;
    @FXML
    private Label totalFarmsLabel;
    @FXML
    private Label priorityLegendLabel;
    
    @FXML
    private Button refreshChartsButton;
    @FXML
    private Button exportDataButton;
    @FXML
    private Button generateReportButton;

    private final AnalyseService analyseService;
    private final ConseilService conseilService;
    private ObservableList<PieChart.Data> pieChartData;
    private ObservableList<XYChart.Series<String, Number>> barChartData;

    public StatisticsController() {
        this.analyseService = new AnalyseService();
        this.conseilService = new ConseilService();
        this.pieChartData = FXCollections.observableArrayList();
        this.barChartData = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCharts();
        loadStatisticsData();
    }

    /**
     * Setup chart configurations and styling
     */
    private void setupCharts() {
        // Configure Pie Chart
        priorityPieChart.setTitle("Recommendation Priority Distribution");
        priorityPieChart.setData(pieChartData);
        
        // Configure Bar Chart
        farmBarChart.setTitle("Analysis Frequency per Farm");
        farmBarChart.setData(barChartData);
        xAxis.setLabel("Farm ID");
        yAxis.setLabel("Number of Analyses");
    }

    /**
     * Load statistics data from database
     * Uses Railway Track pattern with proper entity relationships
     */
    private void loadStatisticsData() {
        try {
            // Load priority statistics for Pie Chart
            loadPriorityStats();
            
            // Load farm analysis statistics for Bar Chart
            loadFarmAnalysisStats();
            
            // Load overview statistics
            loadOverviewStats();
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to load statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load priority distribution data for Pie Chart
     * SQL: SELECT priorite, COUNT(*) FROM conseil GROUP BY priorite
     */
    private void loadPriorityStats() throws SQLException {
        pieChartData.clear();
        
        List<Object[]> priorityStats = analyseService.getConseilPriorityStats();
        
        for (Object[] stat : priorityStats) {
            String priority = (String) stat[0];
            int count = (Integer) stat[1];
            pieChartData.add(new PieChart.Data(priority + " (" + count + ")", count));
        }
        
        // Update legend
        int total = priorityStats.stream().mapToInt(stat -> (Integer) stat[1]).sum();
        priorityLegendLabel.setText("Total Recommendations: " + total);
    }

    /**
     * Load analysis frequency per farm for Bar Chart
     * SQL: SELECT id_ferme, COUNT(*) FROM analyse GROUP BY id_ferme
     */
    private void loadFarmAnalysisStats() throws SQLException {
        barChartData.clear();
        
        List<Object[]> farmStats = analyseService.getAnalysisPerFarmStats();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Analyses per Farm");
        
        for (Object[] stat : farmStats) {
            int farmId = (Integer) stat[0];
            int count = (Integer) stat[1];
            series.getData().add(new XYChart.Data<>("Farm " + farmId, count));
        }
        
        barChartData.add(series);
    }

    /**
     * Load overview statistics for summary labels
     */
    private void loadOverviewStats() throws SQLException {
        // Total analyses
        List<Analyse> analyses = analyseService.selectAll();
        totalAnalysesLabel.setText(String.valueOf(analyses.size()));
        
        // Total conseils
        List<Conseil> conseils = conseilService.selectAll();
        totalConseilsLabel.setText(String.valueOf(conseils.size()));
        
        // Total farms (unique farm IDs)
        int uniqueFarms = (int) analyses.stream()
                .mapToInt(Analyse::getIdFerme)
                .distinct()
                .count();
        totalFarmsLabel.setText(String.valueOf(uniqueFarms));
    }

    @FXML
    private void handleRefreshData() {
        loadStatisticsData();
        showInfo("Data Refreshed", "Statistics data has been updated successfully.");
    }

    @FXML
    private void handleExportData() {
        try {
            String csvContent = generateCSVData();
            File tempFile = File.createTempFile("farmai_statistics_", ".csv");
            
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(csvContent);
            }
            
            showInfo("Export Complete", "Statistics exported to: " + tempFile.getAbsolutePath());
            
        } catch (IOException | SQLException e) {
            showError("Export Failed", "Failed to export data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate CSV data from current statistics
     */
    private String generateCSVData() throws SQLException {
        StringBuilder csv = new StringBuilder();
        csv.append("Metric,Value\n");
        
        // Overview stats
        csv.append("Total Analyses,").append(totalAnalysesLabel.getText()).append("\n");
        csv.append("Total Recommendations,").append(totalConseilsLabel.getText()).append("\n");
        csv.append("Total Farms,").append(totalFarmsLabel.getText()).append("\n");
        csv.append("\n");
        
        // Priority distribution
        csv.append("Priority Distribution\n");
        for (PieChart.Data data : pieChartData) {
            csv.append(data.getName()).append(",").append((int)data.getPieValue()).append("\n");
        }
        csv.append("\n");
        
        // Farm analysis distribution
        csv.append("Farm Analysis Distribution\n");
        for (XYChart.Series<String, Number> series : barChartData) {
            for (XYChart.Data<String, Number> item : series.getData()) {
                csv.append(item.getXValue()).append(",").append(item.getYValue()).append("\n");
            }
        }
        
        return csv.toString();
    }

    @FXML
    private void handleGenerateReport() {
        try {
            // Generate comprehensive report
            String reportContent = generateComprehensiveReport();
            File tempFile = File.createTempFile("farmai_comprehensive_report_", ".txt");
            
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(reportContent);
            }
            
            showInfo("Report Generated", "Comprehensive report saved to: " + tempFile.getAbsolutePath());
            
        } catch (IOException | SQLException e) {
            showError("Report Generation Failed", "Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate comprehensive text report
     */
    private String generateComprehensiveReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        report.append("FARMIA COMPREHENSIVE ANALYTICS REPORT\n");
        report.append("Generated: ").append(java.time.LocalDateTime.now().format(formatter)).append("\n");
        report.append("=".repeat(50)).append("\n\n");
        
        // Overview
        report.append("OVERVIEW STATISTICS\n");
        report.append("Total Analyses: ").append(totalAnalysesLabel.getText()).append("\n");
        report.append("Total Recommendations: ").append(totalConseilsLabel.getText()).append("\n");
        report.append("Total Active Farms: ").append(totalFarmsLabel.getText()).append("\n\n");
        
        // Priority analysis
        report.append("RECOMMENDATION PRIORITY ANALYSIS\n");
        for (PieChart.Data data : pieChartData) {
            report.append(data.getName()).append(": ").append((int)data.getPieValue()).append("\n");
        }
        report.append("\n");
        
        // Farm analysis
        report.append("FARM ANALYSIS DISTRIBUTION\n");
        for (XYChart.Series<String, Number> series : barChartData) {
            for (XYChart.Data<String, Number> item : series.getData()) {
                report.append(item.getXValue()).append(": ").append(item.getYValue()).append(" analyses\n");
            }
        }
        
        return report.toString();
    }

    // Navigation handlers
    @FXML
    private void handleDashboard() {
        NavigationUtil.navigateToDashboard(getCurrentStage());
    }

    @FXML
    private void handleAnalyses() {
        NavigationUtil.navigateToGestionAnalyses(getCurrentStage());
    }

    @FXML
    private void handleConseils() {
        NavigationUtil.navigateToGestionConseils(getCurrentStage());
    }

    @FXML
    private void handleLogout() {
        NavigationUtil.navigateToLogin(getCurrentStage());
    }

    // Utility methods
    private Stage getCurrentStage() {
        return (Stage) priorityPieChart.getScene().getWindow();
    }

    // Utility methods
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}