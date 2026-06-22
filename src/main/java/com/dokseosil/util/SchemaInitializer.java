package com.dokseosil.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 앱 기동 시 schema.sql 을 자동 실행한다.
 * 스크립트는 CREATE TABLE IF NOT EXISTS + INSERT ... ON CONFLICT 로
 * 이미 작성되어 있어 매번 실행해도 안전(idempotent).
 *
 * 환경변수 SKIP_SCHEMA_INIT=true 로 비활성화 가능.
 */
@WebListener
public class SchemaInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if ("true".equalsIgnoreCase(System.getenv("SKIP_SCHEMA_INIT"))) {
            log("SKIP_SCHEMA_INIT=true → schema.sql 자동 실행 건너뜀");
            return;
        }
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) { log("schema.sql 리소스를 찾지 못함"); return; }
            String sql = readAll(in);
            List<String> stmts = splitStatements(sql);

            try (Connection c = DB.getConnection();
                 Statement st = c.createStatement()) {
                for (String s : stmts) {
                    String trimmed = s.trim();
                    if (trimmed.isEmpty()) continue;
                    try {
                        st.execute(trimmed);
                    } catch (Exception e) {
                        // 일부 statement(예: 권한 부족으로 인한 CREATE) 실패해도 계속 진행
                        log("schema 문장 실행 경고: " + e.getMessage() + " | SQL: " + abbreviate(trimmed));
                    }
                }
            }
            log("schema.sql 적용 완료 (" + stmts.size() + " statements)");
        } catch (Exception e) {
            // DB 연결 실패 시 앱이 뜨지 않게 막지는 않음 — 로그만 남기고 통과
            log("schema 초기화 중 예외: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DB.shutdown();
    }

    private static String readAll(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
        }
        return sb.toString();
    }

    /**
     * 매우 단순한 분리기: '--' 주석 라인 제거 후 세미콜론으로 split.
     * 본 프로젝트의 schema.sql은 문자열 리터럴에 세미콜론이 없으므로 안전하다.
     */
    private static List<String> splitStatements(String sql) {
        StringBuilder cleaned = new StringBuilder();
        for (String line : sql.split("\\R")) {
            String t = line.replaceAll("--.*$", "");
            cleaned.append(t).append('\n');
        }
        List<String> out = new ArrayList<>();
        for (String s : cleaned.toString().split(";")) {
            if (!s.trim().isEmpty()) out.add(s);
        }
        return out;
    }

    private static String abbreviate(String s) {
        s = s.replaceAll("\\s+", " ");
        return s.length() > 80 ? s.substring(0, 80) + "..." : s;
    }

    private static void log(String msg) {
        System.out.println("[SchemaInitializer] " + msg);
    }
}
