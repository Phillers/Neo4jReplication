package client;

import neo4jreplication.Neo4jReplication;
import neo4jreplication.RequestParameter;
import neo4jreplication.RequestResult;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.exceptions.value.Uncoercible;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.driver.v1.util.Pair;

import java.util.List;

public class Client {


    public static void main(String[] Args) {
        String[] addresses = {"tcp://localhost:1234", "tcp://localhost:4321"};
        Neo4jReplication replication = new Neo4jReplication("bolt://localhost:7687", "neo4j", "qwazerty2", addresses, 0);
        Neo4jReplication replication2 = new Neo4jReplication("bolt://localhost:7677", "neo4j", "qwazerty2", addresses, 1);
        RequestResult res = replication.write(new RequestParameter("merge (a:Person {name: 'New Person'})"));
        res = replication2.read(new RequestParameter("match (a:Person {name: 'New Person'}) return a", RequestParameter.Level.ReadYourWrites, res));
        List<Record> l = res.records;
        for (Record r : l) {
            List<Pair<String, Value>> lp = r.fields();
            for (Pair<String, Value> p : lp) {
                System.out.println(p.value().type().name());

                try {
                    Relationship rel = p.value().asRelationship();
                    System.out.println(p.key() + ": " + rel.startNodeId() + rel.type() + rel.endNodeId());
                } catch (Uncoercible e) {
                    try {
                        Node n = p.value().asNode();
                        System.out.println(p.key() + ": " + n.labels() + n.asMap());
                    } catch (Uncoercible e2) {
                        System.out.println(p.key() + ": " + p.value());
                    }
                }
            }
        }
        replication.close();
    }
}