package imagescalerfx;

import imagescalerfx.utils.DataTiempoPorImagen;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ChartController {

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Button btnBack;

    @FXML
    public void initialize() {
        xAxis.setLabel("Imagen");
        yAxis.setLabel("Tiempo de procesado en ms");
        barChart.setTitle("Tiempo de procesado");

        // Vuelvo a la ventana principal
        btnBack.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
                Scene scene = new Scene(loader.load(), 900, 600);
                Stage stage = (Stage) btnBack.getScene().getWindow();
                stage.setScene(scene);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // Cargo al gráfico los datos desde una lista de DataTiempoPorImagen
    public void loadData(List<DataTiempoPorImagen> dataportiempo) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tiempo de procesado");

        for (DataTiempoPorImagen data : dataportiempo) {
            series.getData().add(new XYChart.Data<>(data.getImageName(), data.getProcessingTimeMs()));
        }

        barChart.getData().clear();
        barChart.getData().add(series);
    }
}
