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
import java.util.Map;

public class Client {


    public static void main(String[] Args) {
        String[] addresses = {"tcp://localhost:1234", "tcp://localhost:4321"};
        Neo4jReplication replication = new Neo4jReplication("bolt://localhost:7687", "neo4j", "qwazerty2", addresses, 0);
        Neo4jReplication replication2 = new Neo4jReplication("bolt://localhost:7677", "neo4j", "qwazerty2", addresses, 1);
        RequestResult res = replication.write(new RequestParameter("merge (a:Person {name: 'New Person'})"));
        res = replication2.read(new RequestParameter("match (a:Person {name: 'New Person'}) return a", RequestParameter.RYW, res));
        List<Map<String, Object>> l = res.records;
        for (Map<String, Object> r : l) {
            Map<String, Object> lp = r;
            for (Map.Entry<String, Object> p : lp.entrySet()) {
                Value val = (Value)p.getValue();
                System.out.println((val).type().name());

                try {
                    Relationship rel = val.asRelationship();
                    System.out.println(p.getKey() + ": " + rel.startNodeId() + rel.type() + rel.endNodeId());
                } catch (Uncoercible e) {
                    try {
                        Node n = val.asNode();
                        System.out.println(p.getKey() + ": " + n.labels() + n.asMap());
                    } catch (Uncoercible e2) {
                        System.out.println(p.getKey() + ": " + val);
                    }
                }
            }
        }
        replication.close();
    }
}