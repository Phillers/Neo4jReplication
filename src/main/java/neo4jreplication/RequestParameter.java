package neo4jreplication;

public class RequestParameter {
    public int[] write = {};
    public int[] read = {};
    public Level level = Level.Default;

    public String query;

    public RequestParameter(String s) {
        query = s;

    }

    public RequestParameter(String s, Level level, RequestResult res) {
        query = s;
        write = res.write.clone();
        read = res.read.clone();
        this.level = level;
    }

    public enum Level {
        Default, ReadYourWrites, MonotonicWrites, MonotonicReads, WritesFollowReads, PRAM, Sequential
    }

}
