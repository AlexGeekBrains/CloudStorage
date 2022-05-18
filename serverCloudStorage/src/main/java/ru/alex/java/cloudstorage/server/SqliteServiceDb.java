package ru.alex.java.cloudstorage.server;

import org.sqlite.SQLiteException;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteServiceDb implements ServiceDb {
private final int MAX_NESTING = 5;
private final Long MAX_DISK_QUOTA= 1048576000L;

    @Override
    public Integer getMaxNesting(String login) {
        try (PreparedStatement psMaxNesting = DataSource.getConnectionDb()
                .prepareStatement("Select maxNesting from settingsDb WHERE id_client = (Select id from clients WHERE nickname = ?);")) {
            psMaxNesting.setString(1, login);
            ResultSet rs = psMaxNesting.executeQuery();
            if (rs.next()) {
                return rs.getInt("maxNesting");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Long getDiskQuota(String login) {
        try (PreparedStatement psDiskQuota = DataSource.getConnectionDb()
                .prepareStatement("SELECT diskQuota from settingsDb WHERE id_client = (Select id from clients WHERE nickname = ?);")) {
            psDiskQuota.setString(1, login);
            ResultSet rs = psDiskQuota.executeQuery();
            if (rs.next()) {
                return rs.getLong("diskQuota");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getLoginByLoginAndPassword(String nickname, String password) {
        try (PreparedStatement psLogin = DataSource.getConnectionDb().prepareStatement("SELECT nickname FROM clients WHERE nickname =? AND password = ?;")) {
            psLogin.setString(1, nickname);
            psLogin.setString(2, getCrypto(password));
            ResultSet rs = psLogin.executeQuery();
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public boolean isAuthentication(String nickname, String password) {
        try (PreparedStatement psLogin = DataSource.getConnectionDb().prepareStatement("SELECT nickname FROM clients WHERE nickname =? AND password = ?;")) {
            psLogin.setString(1, nickname);
            psLogin.setString(2, getCrypto(password));
            ResultSet rs = psLogin.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public boolean isRegistration(String nickname, String password) {
        try (PreparedStatement psRegistration = DataSource.getConnectionDb().prepareStatement("INSERT INTO clients (nickname, password) VALUES (?, ?);")) {
            psRegistration.setString(1, nickname);
            psRegistration.setString(2, getCrypto(password));
            try {
                if (psRegistration.executeUpdate() == 1) {
                    PreparedStatement psInsertSettingsDb = DataSource.getConnectionDb().prepareStatement("INSERT INTO settingsDb (id_client, maxNesting,diskQuota) VALUES ((SELECT id FROM clients WHERE nickname = ?), ?, ?);");
                    psInsertSettingsDb.setString(1, nickname);
                    psInsertSettingsDb.setInt(2, MAX_NESTING);
                    psInsertSettingsDb.setLong(3, MAX_DISK_QUOTA);
                    psInsertSettingsDb.executeUpdate();
                    psInsertSettingsDb.close();
                    return true;
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
                return false;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private String getCrypto(String str) throws NoSuchAlgorithmException {
        MessageDigest sh1 = MessageDigest.getInstance("SHA-256");
        byte[] digest = sh1.digest(str.getBytes());
        StringBuilder builder = new StringBuilder();
        for (byte b : digest) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }
}