package Event;

/**
 * Created by tiki on 29/03/2017.
 */

public class Event_to_navigate_house {
    private final int id;
    private final String name;
    private final String type;


    public Event_to_navigate_house(int Id, String Type, String Name) {
        id = Id;
        name = Name;
        type = Type;
    }

    public int getid() {
        return id;
    }

    public String getname() {
        return name;
    }

    public String gettype() {
        return type;
    }

}
