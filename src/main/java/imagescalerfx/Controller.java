package imagescalerfx;

import javafx.event.EventHandler;
import imagescalerfx.utils.IOUtils;
import imagescalerfx.utils.ImageData;
import imagescalerfx.utils.DataTiempoPorImagen;
import imagescalerfx.utils.MessageUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javafx.scene.text.Text;
import javafx.util.Duration;

public class Controller {

    @FXML
    private Button btnChart;

    @FXML
    private Button btnStart;

    @FXML
    private ImageView imagen;

    @FXML
    private ListView<ImageData> listViewImages;

    @FXML
    private ListView<ImageData> listViewScaled;

    @FXML
    private Text status;

    @FXML
    public void initialize() {
        btnChart.setDisable(true); // Empieza desactivado y cuando se ha ejecutado ya se activa
        btnStart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                procesarImagenes();
            }
        });

        listViewImages.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<ImageData>() {
                    @Override
                    public void changed(ObservableValue<? extends ImageData> obs, ImageData oldValue,
                            ImageData newValue) {
                        mostrarImagenesEscaladas(newValue);
                    }
                });

        listViewScaled.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<ImageData>() {
                    @Override
                    public void changed(ObservableValue<? extends ImageData> obs, ImageData oldValue,
                            ImageData imagedata) {
                        if (imagedata != null && imagen != null) {
                            Image image = new Image(imagedata.getImagePath().toUri().toString());
                            imagen.setImage(image);
                        }
                    }
                });

        btnChart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("chart.fxml"));
                    Scene scene = new Scene(loader.load(), 900, 600);
                    ChartController controller = loader.getController();
                    controller.loadData(listaTiempoProcesado);
                    Stage stage = (Stage) btnChart.getScene().getWindow();
                    stage.setScene(scene);
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageUtils.showError("Error", "No se pudo cargar el gráfico");
                }
            }
        });
    }

    private ThreadPoolExecutor executor;
    private ScheduledService<Boolean> schedServ;
    private List<DataTiempoPorImagen> listaTiempoProcesado;

    private void procesarImagenes() {
        File fotosDir = new File("images");
        // Si no existe, doy error y no ejecuto nada
        if (!fotosDir.exists()) {
            MessageUtils.showError("Error", "No se ha encontrado la carpeta de imagenes!");
            return;
        }

        File[] fotos = fotosDir.listFiles(File::isFile); // Solo cojo archivos ya que también habrá directorios

        if (fotos == null || fotos.length == 0) {
            MessageUtils.showError("Error", "No hay imagenes en la cacrpeta!");
            return;
        }

        // Desactivo el boton y clear a la lista y ImageView
        btnStart.setDisable(true);
        listViewImages.getItems().clear();
        imagen.setImage(null);
        // Synchronizedlist para que se pueda acceder desde mas de un hilo a la vez sin
        // que de error
        listaTiempoProcesado = Collections.synchronizedList(new ArrayList<>());

        int tareasTotales = fotos.length;
        status.setText("0 of " + tareasTotales + " task finished");

        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());

        for (File file : fotos) {
            executor.execute(() -> processImage(file));
        }

        schedServ = new ScheduledService<Boolean>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        long completed = executor.getCompletedTaskCount();
                        Platform.runLater(() -> status.setText(completed + " of " + tareasTotales + " task finished"));
                        return completed == tareasTotales;
                    }
                };
            }
        };
        schedServ.setDelay(Duration.millis(500));
        schedServ.setPeriod(Duration.seconds(1));
        schedServ.setOnSucceeded(e -> {
            if (schedServ.getValue()) {
                schedServ.cancel();
                executor.shutdown();
                MessageUtils.showMessage("Finalizado", "Todas las imagenes procesadas");
                btnChart.setDisable(false);
                btnStart.setDisable(false);
            }
        });
        schedServ.start();
    }

    // Metodo que se ejecutará a la vez en los diferentes hilos, uno por imagen
    private void processImage(File imageFile) {
        long inicio = System.currentTimeMillis();
        try {
            String fileName = imageFile.getName();
            String nombreSinExt = fileName.split("\\.")[0]; // Divido por el . y cojo la primera parte
            Path pathImagenes = Paths.get("images");
            Path carpetaImagen = pathImagenes.resolve(nombreSinExt);

            if (Files.exists(carpetaImagen)) {
                imagescalerfx.utils.IOUtils.deleteDirectory(carpetaImagen); // Si ya existe la borro
            }
            java.nio.file.Files.createDirectory(carpetaImagen); // Creo una vacía

            for (int i = 10; i < 100; i += 10) { // Del 10 al 100%
                String nombreNuevoArchivo = i + "_" + fileName;
                Path salidaPath = carpetaImagen.resolve(nombreNuevoArchivo);

                double factor = i / 100.0;
                IOUtils.resize(
                        imageFile.getAbsolutePath(),
                        salidaPath.toAbsolutePath().toString(),
                        factor);
            }

            long fin = System.currentTimeMillis();
            long tiempoProcesado = fin - inicio;
            listaTiempoProcesado.add(new DataTiempoPorImagen(fileName, tiempoProcesado));

            // Tiene que ser ejecutado con runlater porque no se puede cambiar la UI
            // Desde un hilo que no sea el principal
            Platform.runLater(() -> listViewImages.getItems().add(new ImageData(fileName, imageFile.toPath())));

        } catch (java.io.IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> MessageUtils
                    .showError("Error procesando la imagen " + imageFile.getName(), e.getMessage()));
        }
    }

    private void mostrarImagenesEscaladas(ImageData imagenData) {
        listViewScaled.getItems().clear(); // Borro los que ya pueda haber
        String fileName = imagenData.getFileName();
        // Misma logica que antes
        String nombreSinExt = fileName.split("\\.")[0];

        File carpeta = new File("images", nombreSinExt);
        if (carpeta.exists()) {
            File[] archivos = carpeta.listFiles();
            if (archivos != null) {
                for (File f : archivos) {
                    listViewScaled.getItems().add(new ImageData(f.getName(), f.toPath()));
                }
            }
        }
    }
}