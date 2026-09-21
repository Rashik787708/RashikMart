package com.rashik.rashikmart.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes a plain-language SQL script (a list of {@code ;}-terminated
 * statements) through JDBC.
 *
 * <p>The bundled {@code seed_catalog.sql} was historically run with H2's
 * {@code org.h2.tools.RunScript}. That tool is H2-specific, so PostgreSQL
 * deployments need a driver-native runner instead. The script itself is
 * written in standard SQL (no H2 extensions), so a simple statement splitter
 * that understands single-quoted string literals and {@code --}/{@code /*}
 * comments is sufficient. Never logs or prints any script content.</p>
 */
public final class SqlScriptRunner {

    private SqlScriptRunner() {
    }

    public static void execute(
            Connection connection,
            InputStream script
    ) throws IOException, SQLException {

        String content = readContent(script);

        List<String> statements = split(content);

        try (Statement statement = connection.createStatement()) {

            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }

    private static String readContent(
            InputStream in
    ) throws IOException {

        StringBuilder content = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     in,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
        }

        return content.toString();
    }

    /**
     * Splits SQL into statements on {@code ;} while respecting single-quoted
     * string literals (with {@code ''} escapes), {@code --} line comments and
     * {@code /* ... *&#47;} block comments. Blank/pure-comment statements are dropped.
     */
    private static List<String> split(String sql) {

        List<String> statements = new ArrayList<>();

        StringBuilder current = new StringBuilder();

        boolean inSingleQuote = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < sql.length(); i++) {

            char c = sql.charAt(i);
            char next = i + 1 < sql.length()
                    ? sql.charAt(i + 1)
                    : '\0';

            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                }
                continue;
            }

            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }

            if (inSingleQuote) {
                current.append(c);
                if (c == '\'' && next == '\'') {
                    current.append(next);
                    i++;
                } else if (c == '\'') {
                    inSingleQuote = false;
                }
                continue;
            }

            if (c == '-' && next == '-') {
                inLineComment = true;
                i++;
                continue;
            }

            if (c == '/' && next == '*') {
                inBlockComment = true;
                i++;
                continue;
            }

            if (c == '\'') {
                inSingleQuote = true;
                current.append(c);
                continue;
            }

            if (c == ';') {
                flush(current, statements);
                continue;
            }

            current.append(c);
        }

        if (inSingleQuote || inBlockComment) {
            throw new IllegalStateException(
                    "Malformed/truncated SQL script: unterminated string or comment."
            );
        }

        flush(current, statements);

        return statements;
    }

    private static void flush(
            StringBuilder current,
            List<String> statements
    ) {

        String statement = current.toString().trim();

        if (!statement.isEmpty()) {
            statements.add(statement);
        }

        current.setLength(0);
    }
}