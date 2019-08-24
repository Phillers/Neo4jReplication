package neo4jreplication;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.util.Pair;

import java.util.List;
import java.util.stream.Stream;

import static org.neo4j.driver.v1.Values.parameters;

public class DatabaseDriver implements AutoCloseable {
    private final Driver driver;

    public DatabaseDriver(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    public List<Record> read(final String query) {
        try (Session session = driver.session()) {
            return session.readTransaction(new TransactionWork<List<Record>>() {
                @Override
                public List<Record> execute(Transaction tx) {
                    StatementResult result = tx.run(query);
                    return result.list();
                }
            });
        }
    }

    public List<Record> write(final String query) {
        try (Session session = driver.session()) {
            return session.writeTransaction(new TransactionWork<List<Record>>() {
                @Override
                public List<Record> execute(Transaction tx) {
                    StatementResult result = tx.run(query);
                    return result.list();
                }
            });
        }
    }
}

