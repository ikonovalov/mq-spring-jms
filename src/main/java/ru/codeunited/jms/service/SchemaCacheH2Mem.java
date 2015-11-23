package ru.codeunited.jms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by ikonovalov on 13.11.15.
 */
public class SchemaCacheH2Mem implements SchemaCache {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaCacheH2Mem.class);

    private static final String DB_DRIVER = "org.h2.Driver";

    private static final String DB_CONNECTION = "jdbc:h2:mem:schemacache;DB_CLOSE_DELAY=-1";

    private static final String DB_USER = "";

    private static final String DB_PASSWORD = "";

    public SchemaCacheH2Mem() {

    }

    public SchemaCacheH2Mem init() {
        String s = "CREATE TABLE scache (id CHAR(16), url char(1024))";
        try {
            Connection connection = getInMemoryDBConnection();
            Statement sst = connection.createStatement();
            sst.executeUpdate(s);
            return this;

        } catch (SQLException e) {
            LOG.error("Initialization of in-memory cache failed. {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static Connection getInMemoryDBConnection() {
        loadH2Driver();
        try {
            Connection dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
            return dbConnection;
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException("Connection failed", e);
        }
    }

    private static void loadH2Driver() {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException("Failed to load H2 driver", e);
        }
    }

    @Override
    public Source lookupByServiceTypeCode(String code) {
        return null;
    }
}
