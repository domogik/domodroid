package Event;

/**
 * Created by tiki on 29/03/17.
 */

public class Event_base_message {
    private final String message;

    //Message will be :
    // "domogik_error" ex 8001 handler
    // "stats_error" ex 8002 handler
    // "cache_ready" ex 8999 handler
    // "refresh" to refresh the current view
    // TODO "device_list_error" (not handle fo the moment
    // TODO "sensor_list_error" (not handle fo the moment
    // TODO "datatype" (not handle fo the moment
    // more to come...
    public Event_base_message(String val) {
        message = val;
    }

    public String getmessage() {
        return message;
    }

}
