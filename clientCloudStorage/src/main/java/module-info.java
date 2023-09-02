module ru.alex.java.cloudstorage.clientcloudstorage {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires io.netty.all;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires sqlite.jdbc;
    requires ru.alex.java.cloudstorage.common;
    requires lombok;
    requires org.apache.commons.io;
    opens ru.alex.java.cloudstorage.client to javafx.fxml;
    exports ru.alex.java.cloudstorage.client;
}