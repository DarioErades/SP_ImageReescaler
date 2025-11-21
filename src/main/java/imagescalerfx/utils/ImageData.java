package imagescalerfx.utils;

import java.nio.file.Path;

public class ImageData {
    private String fileName;
    private Path imagePath;

    public ImageData(String fileName, Path imagePath) {
        this.fileName = fileName;
        this.imagePath = imagePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Path getImagePath() {
        return imagePath;
    }

    public void setImagePath(Path imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return fileName;
    }
}
