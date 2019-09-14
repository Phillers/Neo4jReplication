package neo4jreplication;

import org.neo4j.driver.v1.*;

public class DatabaseDriver implements AutoCloseable {
    private final Driver driver;

    public DatabaseDriver(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    public StatementResult read(final String query) {
        try (Session session = driver.session()) {
            StatementResult sr =  session.readTransaction(new TransactionWork<StatementResult>() {
                @Override
                public StatementResult execute(Transaction tx) {
                    StatementResult result = tx.run(query);
                    return result;
                }
            });
            return sr;
        }
    }

    public StatementResult write(final String query) {
        try (Session session = driver.session()) {
            StatementResult sr =  session.writeTransaction(new TransactionWork<StatementResult>() {
                @Override
                public StatementResult execute(Transaction tx) {
                    StatementResult result = tx.run(query);
                    return result;
                }
            });

            return sr;
        }
    }
}

