package Entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tiki on 06/06/2016.
 */
public class Entity_RoomTest {

    @Test
    public void testSetGetArea_id() throws Exception {
        Entity_Room room = new Entity_Room(null, null, null, 0, null, 0, null);
        room.setArea_id(125);
        Integer areaid = room.getArea_id();
        Assert.assertEquals(125, areaid, 0);
    }

    @Test
    public void testSetGetDescription() throws Exception {
        Entity_Room room = new Entity_Room(null, null, null, 0, null, 0, null);
        room.setDescription("Description");
        String description = room.getDescription();
        Assert.assertEquals("Description", description);
    }

    @Test
    public void testSetGetId() throws Exception {
        Entity_Room room = new Entity_Room(null, null, null, 0, null, 0, null);
        room.setId(125);
        Integer id = room.getId();
        Assert.assertEquals(125, id, 0);
    }

    @Test
    public void testSetGetName() throws Exception {
        Entity_Room room = new Entity_Room(null, null, null, 0, null, 0, null);
        room.setName("Name");
        String name = room.getName();
        Assert.assertEquals("Name", name);
    }

    @Test
    public void testSetGetIcon_name() throws Exception {
        //// TODO: 06/06/2016
    }
}