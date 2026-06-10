package com.dokseosil.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * HikariCP 기반 PostgreSQL 커넥션 풀.
 * 환경변수(DB_URL/DB_USER/DB_PASSWORD)가 있으면 우선 사용,
 * 없으면 classpath 의 db.properties 사용.
 */
public final class DB {

    private static volatile HikariDataSource DS;

    private DB() {}

    public static synchronized DataSource getDataSource() {
        if (DS == null) init();
        return DS;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private static void init() {
        Properties p = loadProps();
        String url = sysOrEnv("DB_URL", p.getProperty("db.url"));
        String user = sysOrEnv("DB_USER", p.getProperty("db.user"));
        String pass = sysOrEnv("DB_PASSWORD", p.getProperty("db.password"));
        int pool = parseInt(p.getProperty("db.poolSize"), 10);

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setMaximumPoolSize(pool);
        cfg.setPoolName("dokseosil-pool");
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");

        DS = new HikariDataSource(cfg);
    }

    private static Properties loadProps() {
        Properties p = new Properties();
        try (InputStream in = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) p.load(in);
        } catch (Exception ignore) {}
        return p;
    }

    private static String sysOrEnv(String key, String fallback) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    public static void shutdown() {
        if (DS != null) { DS.close(); DS = null; }
    }
}
