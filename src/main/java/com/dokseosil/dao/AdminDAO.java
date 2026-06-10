package com.dokseosil.dao;

import com.dokseosil.util.DB;
import com.dokseosil.util.PasswordUtil;

import java.sql.*;

public class AdminDAO {

    public boolean authenticate(String username, String password) throws SQLException {
        String hash = PasswordUtil.sha256(password);
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT 1 FROM admin_user WHERE username = ? AND pw_hash = ?")) {
            ps.setString(1, username);
            ps.setString(2, hash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean changePassword(String username, String newPassword) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE admin_user SET pw_hash = ? WHERE username = ?")) {
            ps.setString(1, PasswordUtil.sha256(newPassword));
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }
}
