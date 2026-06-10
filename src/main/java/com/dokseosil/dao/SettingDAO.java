package com.dokseosil.dao;

import com.dokseosil.util.DB;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SettingDAO {

    public Map<String,String> getAll() throws SQLException {
        Map<String,String> map = new HashMap<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT skey, svalue FROM app_setting");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString(1), rs.getString(2));
        }
        return map;
    }

    public String get(String key, String defVal) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT svalue FROM app_setting WHERE skey=?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : defVal;
            }
        }
    }

    public int getInt(String key, int defVal) throws SQLException {
        try { return Integer.parseInt(get(key, String.valueOf(defVal))); }
        catch (NumberFormatException e) { return defVal; }
    }

    public boolean getBool(String key, boolean defVal) throws SQLException {
        return "true".equalsIgnoreCase(get(key, String.valueOf(defVal)));
    }

    public void set(String key, String value) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO app_setting (skey, svalue) VALUES (?,?) " +
                 "ON CONFLICT (skey) DO UPDATE SET svalue = EXCLUDED.svalue")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }
}
