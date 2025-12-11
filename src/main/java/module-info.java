module course.reversi {
    requires javafx.controls;
    requires javafx.fxml;


    opens course.reversi to javafx.fxml;
    exports course.reversi;
}