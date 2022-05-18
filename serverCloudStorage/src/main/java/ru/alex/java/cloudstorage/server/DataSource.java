package ru.alex.java.cloudstorage.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private static Connection connectionDb;

    public static Connection connect() throws ClassNotFoundException, SQLException {
        if (connectionDb == null) {
            Class.forName("org.sqlite.JDBC");
            connectionDb = DriverManager.getConnection("jdbc:sqlite:clients.db");
            System.out.println("DataBase connect successfully");
            return connectionDb;
        }
        return connectionDb;
    }

    public static void disconnect() {
        try {
            if (connectionDb != null) {
                connectionDb.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnectionDb() {
        return connectionDb;
    }
}