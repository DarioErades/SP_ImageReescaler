package imagescalerfx.utils;

// Esta clase será un objeto que tendrá el nombre de l aimagen y los ms que ha tardado en procesarse.
// También se podría haber hecho con un Mapa de ImageData, long. Pero he decidido hacerlo así
public class DataTiempoPorImagen {
    private String imageName;
    private long processingTimeMs;

    public DataTiempoPorImagen(String imageName, long processingTimeMs) {
        this.imageName = imageName;
        this.processingTimeMs = processingTimeMs;
    }

    public String getImageName() {
        return imageName;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
}
