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

        // 우선순위:
        //  1) DATABASE_URL (Railway/Heroku 표준: postgres:// 또는 postgresql:// URL)
        //  2) DB_URL/DB_USER/DB_PASSWORD (개별 env var)
        //  3) db.properties (로컬 개발 기본값)
        String url, user, pass;
        String databaseUrl = sysOrEnv("DATABASE_URL", null);
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            String[] parsed = parseDatabaseUrl(databaseUrl);
            url  = parsed[0];
            user = parsed[1];
            pass = parsed[2];
        } else {
            url  = sysOrEnv("DB_URL",      p.getProperty("db.url"));
            user = sysOrEnv("DB_USER",     p.getProperty("db.user"));
            pass = sysOrEnv("DB_PASSWORD", p.getProperty("db.password"));
        }
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

    /**
     * postgres://user:pass@host:port/db (?param=...) 형식의 DATABASE_URL을
     * JDBC URL + user + password 로 분해.
     * Railway PostgreSQL은 외부 연결 시 SSL이 필요하므로 sslmode=require를 자동 추가.
     */
    static String[] parseDatabaseUrl(String raw) {
        try {
            String norm = raw.replaceFirst("^postgres://", "postgresql://");
            java.net.URI uri = new java.net.URI(norm);
            String userInfo = uri.getUserInfo();
            String u = "", pw = "";
            if (userInfo != null) {
                int i = userInfo.indexOf(':');
                if (i >= 0) { u = userInfo.substring(0, i); pw = userInfo.substring(i + 1); }
                else u = userInfo;
            }
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String path = (uri.getPath() == null || uri.getPath().isEmpty()) ? "/" : uri.getPath();
            String query = uri.getRawQuery();
            if (query == null || !query.contains("sslmode")) {
                query = (query == null ? "" : query + "&") + "sslmode=require";
            }
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + path + "?" + query;
            return new String[]{ jdbcUrl,
                    java.net.URLDecoder.decode(u,  "UTF-8"),
                    java.net.URLDecoder.decode(pw, "UTF-8") };
        } catch (Exception e) {
            throw new RuntimeException("DATABASE_URL 파싱 실패: " + e.getMessage(), e);
        }
    }
}
