package com.newx.ezapi.core.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DatabaseType {
    MYSQL("mysql", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/database?useSSL=false&serverTimezone=UTC"),
    ORACLE("oracle", "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@127.0.0.1:1521:database"),
    SQLSERVER("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://127.0.0.1:1433;databaseName=database"),
    POSTGRESQL("postgresql", "org.postgresql.Driver", "jdbc:postgresql://127.0.0.1:5432/database");

    private final String type;
    private final String driverClass;
    private final String connectionTemplate;

    DatabaseType(String type, String driverClass, String connectionTemplate) {
        this.type = type;
        this.driverClass = driverClass;
        this.connectionTemplate = connectionTemplate;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("driverClass")
    public String getDriverClass() {
        return driverClass;
    }

    @JsonProperty("connectionTemplate")
    public String getConnectionTemplate() {
        return connectionTemplate;
    }

    public static DatabaseType fromType(String type) {
        for (DatabaseType databaseType : DatabaseType.values()) {
            if (databaseType.type.equalsIgnoreCase(type)) {
                return databaseType;
            }
        }
        throw new IllegalArgumentException("Unsupported database type: " + type);
    }
}