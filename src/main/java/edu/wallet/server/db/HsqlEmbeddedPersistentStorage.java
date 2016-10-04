package edu.wallet.server.db;

import edu.wallet.config.*;
import edu.wallet.log.*;
import edu.wallet.server.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.hsqldb.*;

/**
 * HyperSQL implementation of {@link IPersistentStorage}.
 */
public class HsqlEmbeddedPersistentStorage implements IPersistentStorage {

    private final IConfiguration configuration;
    private final ILogger logger;

    public static void main(String[] args) {

        Server hsqlServer = null;
        Connection connection = null;
        ResultSet rs = null;

        hsqlServer = new Server();

        hsqlServer.setLogWriter(new PrintWriter(System.out));
        hsqlServer.setSilent(false);

        hsqlServer.setDatabaseName(0, "iva");
        hsqlServer.setDatabasePath(0, "file:ivadb");

        hsqlServer.start();

        // making a connection
        try {
            Class.forName("org.hsqldb.jdbcDriver");

            connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/iva", "sa", ""); // can through sql exception

            connection.prepareStatement("drop table barcodes if exists;").execute();
            connection.prepareStatement("create table barcodes (id integer, barcode varchar(20) not null);").execute();
            connection.prepareStatement("insert into barcodes (id, barcode)"
                + "values (1, '12345566');").execute();

            // query from the db
            rs = connection.prepareStatement("select id, barcode  from barcodes;").executeQuery();

            rs.next();

            System.out.println(String.format("ID: %1d, Name: %1s", rs.getInt(1), rs.getString(2)));

        } catch (SQLException e2) {
            e2.printStackTrace();
        } catch (ClassNotFoundException e2) {
            e2.printStackTrace();
        }

        hsqlServer.stop();
        hsqlServer = null;

        // end of stub code for in/out stub

    }

    public HsqlEmbeddedPersistentStorage(IConfiguration c, ILogger l) {
        this.configuration = Objects.requireNonNull(c);
        this.logger = Objects.requireNonNull(l);
    }

    @Override public ValueObject retrieve(String userNameKey) {
        // TODO
        return null;
    }

    @Override public int save(Collection<ValueObject> voCollection) throws IOException {
        // TODO
        return 0;
    }
}
