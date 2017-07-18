package Event;

/**
 * Created by tiki on 24/03/2017.
 */

public class Entity_client_event_value {
    private final String Value;
    private final String Timestamp;
    private final int id;

    public Entity_client_event_value(String val, String valtimestamp, int featureid) {
        Value = val;
        Timestamp = valtimestamp;
        id = featureid;
    }

    public String Entity_client_event_get_val() {
        return Value;
    }

    public String Entity_client_event_get_timestamp() {
        return Timestamp;
    }

    public int Entity_client_event_get_id() {
        return id;
    }

}
