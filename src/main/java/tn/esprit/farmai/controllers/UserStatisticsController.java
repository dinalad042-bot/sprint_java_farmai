package tn.esprit.farmai.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tooltip;
import tn.esprit.farmai.models.Role;
import tn.esprit.farmai.services.UserService;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for user statistics view.
 */
public class UserStatisticsController implements Initializable {

    @FXML
    private PieChart rolePieChart;

    private final UserService userService;

    public UserStatisticsController() {
        this.userService = new UserService();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadData();
    }

    @FXML
    private void loadData() {
        try {
            Map<Role, Integer> stats = userService.getUsersCountByRole();
            ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();

            stats.forEach((role, count) -> {
                PieChart.Data data = new PieChart.Data(role.getDisplayName() + " (" + count + ")", count);
                chartData.add(data);
            });

            rolePieChart.setData(chartData);

            // Add tooltips to slices
            for (PieChart.Data data : rolePieChart.getData()) {
                Tooltip tooltip = new Tooltip(String.format("%s: %d users", data.getName(), (int) data.getPieValue()));
                Tooltip.install(data.getNode(), tooltip);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
