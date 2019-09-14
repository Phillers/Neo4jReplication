package client;

import neo4jreplication.MyNodeRelation;
import neo4jreplication.Neo4jReplicationStub;
import neo4jreplication.RequestParameter;
import neo4jreplication.RequestResult;
import org.neo4j.driver.v1.Value;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

public class RMIClient {
    public static void main(String[] args) {
        try {
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(12345);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(12345);
            }
            Neo4jReplicationStub replication = (Neo4jReplicationStub) registry.lookup("Neo4jReplication");
            RequestResult res = replication.write(new RequestParameter("merge (a:Person {name: 'New Person'}) set a.age=20"));
            res = replication.read(new RequestParameter("match (a:Person)-[b]->(c:Person) return a,b,c", RequestParameter.RYW, res));
            List<Map<String, Object>> l = res.records;
            for (Map<String, Object> r : l) {
                Map<String, Object> lp = r;
                for (Map.Entry<String, Object> p : lp.entrySet()) {
                    Object val = p.getValue();
                    System.out.println(p.getValue());
                    if (val instanceof MyNodeRelation) {
                        MyNodeRelation my = (MyNodeRelation) val;

                        if (my.isRelationship()) {
                            System.out.println(p.getKey() + ": " + my.id() + ' ' + my.startNodeId() + my.type() + my.endNodeId() + my.asMap());
                        } else if (my.isNode()) {
                            System.out.println(p.getKey() + ": " + my.id() + my.labels() + my.asMap());
                        }
                    } else {
                        System.out.println(p.getKey() + val.getClass() + val);
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
