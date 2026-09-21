package com.dev09.kumaracoconutoil.util;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import org.hibernate.Session;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseBackupUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int ROWS_PER_INSERT = 200;

    public static BackupResult backupDatabase() throws Exception {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String schema = String.valueOf(session.createNativeQuery("SELECT DATABASE()").uniqueResult());

            Object[] schemaInfo = (Object[]) session.createNativeQuery(
                    "SELECT default_character_set_name, default_collation_name "
                    + "FROM information_schema.schemata WHERE schema_name = :schema")
                    .setParameter("schema", schema)
                    .uniqueResult();
            String charset = schemaInfo != null ? String.valueOf(schemaInfo[0]) : "utf8mb4";
            String collation = schemaInfo != null && schemaInfo.length > 1 ? String.valueOf(schemaInfo[1]) : "utf8mb4_unicode_ci";

            List<Object> tableNames = session.createNativeQuery(
                    "SELECT table_name FROM information_schema.tables "
                    + "WHERE table_schema = :schema AND table_type = 'BASE TABLE' ORDER BY table_name")
                    .setParameter("schema", schema)
                    .list();

            StringBuilder sb = new StringBuilder();
            sb.append("-- ==========================================================\n");
            sb.append("-- Dilshan Oil Mill - Full Database Backup (SQL)\n");
            sb.append("-- Schema   : ").append(schema).append("\n");
            sb.append("-- Generated: ").append(LocalDateTime.now().format(DATETIME_FMT)).append("\n");
            sb.append("-- This file is READ-ONLY. Edit disabled after creation.\n");
            sb.append("-- ==========================================================\n\n");

            sb.append("CREATE DATABASE IF NOT EXISTS `").append(schema).append("`")
              .append(" CHARACTER SET ").append(charset)
              .append(" COLLATE ").append(collation).append(";\n");
            sb.append("USE `").append(schema).append("`;\n\n");

            sb.append("SET FOREIGN_KEY_CHECKS = 0;\n\n");

            long totalRows = 0;
            for (Object tableObj : tableNames) {
                String table = String.valueOf(tableObj);

                sb.append("-- ------------------------------------------------------\n");
                sb.append("-- Table: ").append(table).append("\n");
                sb.append("-- ------------------------------------------------------\n");

                sb.append("DROP TABLE IF EXISTS `").append(table).append("`;\n");

                List<Object[]> createRows = session.createNativeQuery(
                        "SHOW CREATE TABLE `" + schema + "`.`" + table + "`").list();
                if (!createRows.isEmpty() && createRows.get(0).length > 1) {
                    sb.append(String.valueOf(createRows.get(0)[1])).append(";\n\n");
                }

                List<Object[]> columns = session.createNativeQuery(
                        "SELECT column_name, data_type, column_type FROM information_schema.columns "
                        + "WHERE table_schema = :schema AND table_name = :table ORDER BY ordinal_position")
                        .setParameter("schema", schema)
                        .setParameter("table", table)
                        .list();

                List<String> colNames = new ArrayList<>();
                List<String> colTypes = new ArrayList<>();
                for (Object[] col : columns) {
                    colNames.add(String.valueOf(col[0]));
                    colTypes.add(String.valueOf(col[1]).toLowerCase());
                }

                if (!colNames.isEmpty()) {
                    List<Object[]> data = session.createNativeQuery(
                            "SELECT * FROM `" + schema + "`.`" + table + "`").list();
                    totalRows += data.size();
                    appendInserts(sb, table, colNames, colTypes, data);
                }
                sb.append("\n");
            }

            sb.append("SET FOREIGN_KEY_CHECKS = 1;\n");
            sb.append("-- Backup complete. Tables exported: ").append(tableNames.size())
              .append(", total rows exported: ").append(totalRows).append(".\n");

            Path dir = Paths.get(System.getProperty("user.home"), "OilMillBackups");
            Files.createDirectories(dir);
            String fileName = "backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";
            Path file = dir.resolve(fileName);
            Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8));

            File result = file.toFile();
            result.setWritable(false, true);
            result.setReadOnly();
            return new BackupResult(result, tableNames.size(), totalRows);
        } finally {
            session.close();
        }
    }

    private static void appendInserts(StringBuilder sb, String table, List<String> colNames,
                                      List<String> colTypes, List<Object[]> data) {
        if (data.isEmpty()) return;

        StringBuilder cols = new StringBuilder();
        for (int i = 0; i < colNames.size(); i++) {
            if (i > 0) cols.append(", ");
            cols.append('`').append(colNames.get(i)).append('`');
        }

        int batch = 0;
        for (int i = 0; i < data.size(); i += ROWS_PER_INSERT) {
            sb.append("INSERT INTO `").append(table).append("` (").append(cols).append(") VALUES\n");
            int end = Math.min(i + ROWS_PER_INSERT, data.size());
            for (int r = i; r < end; r++) {
                Object[] row = data.get(r);
                StringBuilder values = new StringBuilder("(");
                for (int c = 0; c < row.length; c++) {
                    if (c > 0) values.append(", ");
                    values.append(formatValue(row[c], colTypes.get(c)));
                }
                values.append(')');
                sb.append(values);
                sb.append(r == end - 1 ? ";\n" : ",\n");
            }
            sb.append("\n");
            batch++;
        }
    }

    private static String formatValue(Object value, String columnType) {
        if (value == null) return "NULL";

        if (value instanceof java.sql.Date) {
            return "'" + value.toString() + "'";
        }
        if (value instanceof Timestamp) {
            return "'" + ((Timestamp) value).toLocalDateTime().format(DATETIME_FMT) + "'";
        }
        if (value instanceof java.sql.Time) {
            return "'" + value.toString() + "'";
        }
        if (value instanceof LocalDate) {
            return "'" + ((LocalDate) value).format(DATE_FMT) + "'";
        }
        if (value instanceof LocalDateTime) {
            return "'" + ((LocalDateTime) value).format(DATETIME_FMT) + "'";
        }
        if (value instanceof LocalTime) {
            return "'" + ((LocalTime) value).format(TIME_FMT) + "'";
        }
        if (value instanceof byte[]) {
            return hexLiteral((byte[]) value);
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "1" : "0";
        }
        if (value instanceof UUID) {
            return "'" + value + "'";
        }
        if (value instanceof Number) {
            return value.toString();
        }

        String type = columnType == null ? "" : columnType;
        if (type.contains("binary") || type.contains("blob") || type.contains("geography") || type.contains("geometry")) {
            return hexLiteral(value.toString().getBytes(StandardCharsets.UTF_8));
        }

        return "'" + escapeString(String.valueOf(value)) + "'";
    }

    private static String escapeString(String value) {
        String escaped = value.replace("\\", "\\\\");
        return escaped.replace("'", "''");
    }

    private static String hexLiteral(byte[] bytes) {
        StringBuilder sb = new StringBuilder("X'");
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.append("'").toString();
    }

    public static class BackupResult {
        private final File file;
        private final int tableCount;
        private final long rowCount;

        public BackupResult(File file, int tableCount, long rowCount) {
            this.file = file;
            this.tableCount = tableCount;
            this.rowCount = rowCount;
        }

        public File getFile() { return file; }
        public int getTableCount() { return tableCount; }
        public long getRowCount() { return rowCount; }
    }
}