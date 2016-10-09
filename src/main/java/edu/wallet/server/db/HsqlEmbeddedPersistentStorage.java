package edu.wallet.server.db;

import edu.wallet.config.IConfiguration;
import edu.wallet.log.ILogger;
import edu.wallet.server.ValueObject;
import org.hsqldb.Server;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * HyperSQL implementation of {@link IPersistentStorage}.
 * DDL: PLAYER(USERNAME, BALANCE_VERSION, BALANCE)
 * <p>
 * TODO: move to configuration: db url, db name, admin user name, table name, drop key.
 */
public class HsqlEmbeddedPersistentStorage implements IPersistentStorage, Closeable {
    private final IConfiguration configuration;
    private final ILogger logger;

    private static final String dbName = "wallet";
    private static final String playerTable = "player";

    private boolean drop = true;

    private boolean silent = true;

    private final Server hsqlServer;
    private final Connection connection;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private boolean closed;

    public HsqlEmbeddedPersistentStorage(IConfiguration c, ILogger l) {
        // this reference does not leak, so can be safe there.
        try {
            this.configuration = Objects.requireNonNull(c);
            this.logger = Objects.requireNonNull(l);

            hsqlServer = new Server();

            hsqlServer.setLogWriter(new PrintWriter(System.out));
            hsqlServer.setSilent(silent);

            hsqlServer.setDatabaseName(0, dbName);
            hsqlServer.setDatabasePath(0, "file:" + dbName + "db");

            hsqlServer.start();

            Class.forName("org.hsqldb.jdbcDriver");

            connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/" + dbName, "sa", ""); // can through sql exception

            prepareTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareTable() throws Exception {
        if (drop) {
            connection.prepareStatement("drop table " + playerTable + " if exists;").execute();
            // USERNAME, BALANCE_VERSION, BALANCE
            connection.prepareStatement("create table " + playerTable + " (username varchar(32) not null primary key, balance_version bigint, balance int);").execute();
        }
    }

    /*
     * NB: re-create statement for concurrency reasons:
     */
    private PreparedStatement createUpdateStatement() throws Exception {
        return connection.prepareStatement("merge into " + playerTable + " as t using " +
                "(values(?, ?, ?)) as vals(x,y,z)" + " on t.username=vals.x " + " when matched then update set t" +
                ".balance_version=vals.y, t.balance=vals.z " + " WHEN NOT MATCHED THEN INSERT VALUES vals.x, vals.y, " +
                "vals.z;");
    }

    /*
     * NB: re-create statement for concurrency reasons:
     */
    private PreparedStatement createRetrieveStatement() throws Exception {
        return connection.prepareStatement("select username, balance_version, balance from " + playerTable + " where username=? ;");
    }

    public void close() throws IOException {
        rwLock.writeLock().lock();

        try {
            try {
                final Connection conn = connection;
                if (conn != null)
                    conn.close();
            } finally {
                final Server srv = hsqlServer;
                if (srv != null)
                    srv.stop();
            }
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            rwLock.writeLock().unlock();

            closed = true;
        }
    }

    @Override
    public ValueObject retrieve(String userNameKey) {
        assert userNameKey != null;

        rwLock.readLock().lock();
        try {
            if (closed)
                throw new IllegalStateException("closed");

            PreparedStatement getStmnt = createRetrieveStatement();

            getStmnt.setString(1, userNameKey);

            ResultSet rs = getStmnt.executeQuery();

            if (rs.next()) {
                String name = rs.getString(1);

                assert userNameKey.equals(name);

                long balanceVer = rs.getLong(2);
                int bal = rs.getInt(3);

                return new ValueObject(name, bal, balanceVer);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // TODO: investigate if possible to use batch insert syntax (a,b),(c,d), ...
    @Override
    public int save(Collection<ValueObject> voCollection) throws IOException {
        rwLock.readLock().lock();
        try {
            if (closed)
                throw new IllegalStateException("closed");

            int counter = 0;

            PreparedStatement ps = createUpdateStatement();

            for (ValueObject vo : voCollection) {
                ps.setString(1, vo.userName);
                ps.setLong(2, vo.balanceVersion);
                ps.setInt(3, vo.currentBalance);

                ps.execute();

                counter += ps.getUpdateCount();
            }

            return counter;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        rwLock.writeLock().lock();
        try {
            if (closed)
                throw new IllegalStateException("closed");

            PreparedStatement ps = connection.prepareStatement("truncate table " + playerTable + ";");

            ps.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
