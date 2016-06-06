package Entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tiki on 06/06/2016.
 */
public class Entity_AreaTest {

    @Test
    public void testSetGetDescription() throws Exception {
        Entity_Area area = new Entity_Area(null, null, null, null, 0, null);
        area.setDescription("Description");
        String description = area.getDescription();
        Assert.assertEquals("Description", description);
    }

    @Test
    public void testSetGetId() throws Exception {
        Entity_Area area = new Entity_Area(null, null, null, null, 0, null);
        area.setId(125);
        Integer id = area.getId();
        Assert.assertEquals(125, id, 0);
    }

    @Test
    public void testSetGetName() throws Exception {
        Entity_Area area = new Entity_Area(null, null, null, null, 0, null);
        area.setName("Name");
        String name = area.getName();
        Assert.assertEquals("Name", name);
    }

    @Test
    public void testGetIcon_name() throws Exception {
        //// TODO: 06/06/2016
    }
}