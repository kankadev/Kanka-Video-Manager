module dev.kanka.kankavideomanager {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires opencv;
    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.javafx;
    requires org.apache.logging.log4j;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.materialdesign2;
    requires com.dlsc.formsfx;
    requires com.dlsc.preferencesfx;

    opens dev.kanka.kankavideomanager to javafx.fxml;
    opens dev.kanka.kankavideomanager.ui.controller to javafx.fxml;
    opens dev.kanka.kankavideomanager.pojo to javafx.base;
    opens dev.kanka.kankavideomanager.enums to javafx.base;

    exports dev.kanka.kankavideomanager;

    opens dev.kanka.kankavideomanager.settings to javafx.fxml;
}
