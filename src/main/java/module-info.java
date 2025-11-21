module imagescalerfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.desktop;

    opens imagescalerfx to javafx.fxml;

    exports imagescalerfx;
    exports imagescalerfx.utils;
}