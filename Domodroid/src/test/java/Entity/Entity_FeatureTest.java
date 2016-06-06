package Entity;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tiki on 06/06/2016.
 */
public class Entity_FeatureTest {

    @Test
    public void testSetGetId() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setId(125);
        Integer Id = client.getId();
        Assert.assertEquals(125, Id, 0);
    }

    @Test
    public void testSetGetDevice() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setDevice(new JSONObject(""));
        JSONObject device = client.getDevice();
        //// TODO: 06/06/2016
//        Assert.assertEquals(null, device);
    }

    @Test
    public void testSetGetNormalDescription() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setDescription("Description");
//// TODO: 06/06/2016
//        String description = client.getDescription();
//        Assert.assertEquals("Description", description);
    }

    @Test
    public void testSetGetNulllDescription() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setName("Name");
//// TODO: 06/06/2016
//        client.setDescription("");
//        String description = client.getDescription();
//        Assert.assertEquals("Name1r", description);
    }


    @Test
    public void testSetGetDevice_usage_id() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setDevice_usage_id("Device_usage_id");
        String device_usage_id = client.getDevice_usage_id();
        Assert.assertEquals("Device_usage_id", device_usage_id);
    }

    @Test
    public void testGetAddress() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setAddress("Address");
        String address = client.getAddress();
        Assert.assertEquals("Address", address);
    }

    @Test
    public void testSetGetDevId() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setDevId(125);
        Integer DevId = client.getDevId();
        Assert.assertEquals(125, DevId, 0);
    }

    @Test
    public void testSetGetName() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setName("Name");
        String name = client.getName();
        Assert.assertEquals("Name", name);
    }

    @Test
    public void testGetDevice_feature_model_id() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setDevice_feature_model_id("Device_feature_model_id");
        String device_feature_model_id = client.getDevice_feature_model_id();
        Assert.assertEquals("Device_feature_model_id", device_feature_model_id);
    }

    @Test
    public void testGetState_key() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setState_key("State_key");
        String state_key = client.getState_key();
        Assert.assertEquals("State_key", state_key);
    }

    @Test
    public void testGetParameters() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setParameters("Parameters");
        String parameters = client.getParameters();
        Assert.assertEquals("Parameters", parameters);
    }

    @Test
    public void testGetValue_type() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setValue_type("Value_Type");
        String value_type = client.getValue_type();
        Assert.assertEquals("Value_Type", value_type);
    }

    @Test
    public void testGetRessources() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setDevice_usage_id("telephony");
        client.setState(1);
        Integer ressources = client.getRessources();
        Assert.assertEquals(2.130837676E9, ressources, 0);
    }

    @Test
    public void testGetDevice_type() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setDevice_type_id("device_type");
        String device_type = client.getDevice_type();
        Assert.assertEquals("device_type", device_type);
        client.setDevice_type_id("device_type.devicepart2");
        device_type = client.getDevice_type();
        Assert.assertEquals("devicepart2", device_type);
    }

    @Test
    public void testGetDevice_type_id() throws Exception {
        Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
        client.setDevice_type_id("Device_Type_ID");
        String device_type_id = client.getDevice_type_id();
        Assert.assertEquals("Device_Type_ID", device_type_id);
    }

    @Test
    public void testGetIcon_name() throws Exception {
        //// TODO: 06/06/2016
    }
}