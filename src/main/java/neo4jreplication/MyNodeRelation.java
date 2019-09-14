//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package neo4jreplication;

import org.neo4j.driver.internal.InternalEntity;
import org.neo4j.driver.internal.util.Extract;
import org.neo4j.driver.internal.util.Iterables;
import org.neo4j.driver.internal.value.MapValue;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.driver.v1.util.Function;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class MyNodeRelation implements Serializable {
    private long id;
    private Map<String, Object> properties;
    private Collection<String> labels;
    private long start;
    private long end;
    private String type;
    private boolean isNode;
    private boolean isRelationship;

    public MyNodeRelation(Relationship rel) {
        id = rel.id();
        type = rel.type();
        start = rel.startNodeId();
        end = rel.endNodeId();
        properties = rel.asMap();
        isRelationship = true;
    }

    public MyNodeRelation(Node node) {
        id = node.id();
        labels = (Collection<String>) node.labels();
        properties = node.asMap();
        isNode = true;
    }

    public MyNodeRelation() {

    }


    //Node
    public boolean isNode() {
        return isNode;
    }

    public Collection<String> labels() {
        return this.labels;
    }

    public boolean hasLabel(String label) {
        return this.labels.contains(label);
    }


    public String toString() {
        if (isNode)
            return String.format("node<%s>", this.id());

            //Relationship
        else
            return String.format("relationship<%s>", this.id());

    }

    public boolean isRelationship() {
        return isRelationship;
    }

    public boolean hasType(String relationshipType) {
        return this.type().equals(relationshipType);
    }

    public void setStartAndEnd(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long startNodeId() {
        return this.start;
    }

    public long endNodeId() {
        return this.end;
    }

    public String type() {
        return this.type;
    }

    //Entity
    public long id() {
        return this.id;
    }

    public int size() {
        return this.properties.size();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            MyNodeRelation that = (MyNodeRelation) o;
            return this.id == that.id;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return (int) (this.id ^ this.id >>> 32);
    }


    public boolean containsKey(String key) {
        return this.properties.containsKey(key);
    }

    public Iterable<String> keys() {
        return this.properties.keySet();
    }

    public Object get(String key) {
        Object value = this.properties.get(key);
        return value == null ? Values.NULL : value;
    }

    public Iterable<Object> values() {
        return this.properties.values();
    }

    public <T> Iterable<T> values(Function<Object, T> mapFunction) {
        return Iterables.map(this.properties.values(), mapFunction);
    }
    public Map<String, Object> asMap() {
        return this.properties;
    }
}