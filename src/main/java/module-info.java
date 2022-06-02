module dev.kanka.kankavideomanager {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.javafx;
    requires org.apache.logging.log4j;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.materialdesign2;

    opens dev.kanka.kankavideomanager to javafx.fxml;
    opens dev.kanka.kankavideomanager.ui.controller to javafx.fxml;
    opens dev.kanka.kankavideomanager.pojo to javafx.base;
    exports dev.kanka.kankavideomanager;
}
