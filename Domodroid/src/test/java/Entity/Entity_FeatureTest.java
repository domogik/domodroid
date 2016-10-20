package Entity;

import android.test.AndroidTestCase;

import org.junit.Assert;
import org.junit.Test;

public class Entity_FeatureTest extends AndroidTestCase {
    Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);


    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testSetGetId() throws Exception {
        Integer Id = client.getId();
        Assert.assertEquals(0, Id, 0);

        client.setId(125);
        Id = client.getId();
        Assert.assertEquals(125, Id, 0);
    }

    /*Not used
        @Test
        public void testSetGetDevice() throws Exception {
            Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
            client.setDevice(new JSONObject(""));
            JSONObject device = client.getDevice();
            //// TODO: 06/06/2016
            Assert.assertEquals(null, device);
        }
    */
    @Test
    public void testSetGetNormalDescription() throws Exception {
        String description = client.getDescription();
        Assert.assertEquals(null, description);

        client.setName("Name");
        description = client.getDescription();
        Assert.assertEquals("Name", description);

        client.setDescription("Description");
        description = client.getDescription();
        Assert.assertEquals("Description", description);

        client.setName("Name");
        client.setDescription("");
        description = client.getDescription();
        Assert.assertEquals("Name", description);

        client.setDescription("Description");
        client.setId(125);
        /* TODO find a way to handle share params in test
        SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        SharedPreferences.Editor SP_prefEditor;
        SP_prefEditor = SP_params.edit();
        SP_prefEditor.putBoolean("DEV", true);
        SP_prefEditor.commit();

        description = client.getDescription();
        Assert.assertEquals("Description (125)", description);

        client.setName("Name");
        client.setDescription("");
        client.setId(125);
        client.Develop = true;
        description = client.getDescription();
        Assert.assertEquals("Name (125)", description);
        */
    }


    @Test
    public void testSetGetDevice_usage_id() throws Exception {
        String device_usage_id = client.getDevice_usage_id();
        Assert.assertEquals(null, device_usage_id);

        client.setDevice_usage_id("Device_usage_id");
        device_usage_id = client.getDevice_usage_id();
        Assert.assertEquals("Device_usage_id", device_usage_id);
    }

    @Test
    public void testGetAddress() throws Exception {
        String address = client.getAddress();
        Assert.assertEquals(null, address);

        client.setAddress("Address");
        address = client.getAddress();
        Assert.assertEquals("Address", address);
    }

    @Test
    public void testSetGetDevId() throws Exception {
        Integer DevId = client.getDevId();
        Assert.assertEquals(0, DevId, 0);

        client.setDevId(125);
        DevId = client.getDevId();
        Assert.assertEquals(125, DevId, 0);
    }

    @Test
    public void testSetGetName() throws Exception {
        String name = client.getName();
        Assert.assertEquals(null, name);

        client.setName("Name");
        name = client.getName();
        Assert.assertEquals("Name", name);
    }

    @Test
    public void testGetDevice_feature_model_id() throws Exception {
        String device_feature_model_id = client.getDevice_feature_model_id();
        Assert.assertEquals(null, device_feature_model_id);

        client.setDevice_feature_model_id("Device_feature_model_id");
        device_feature_model_id = client.getDevice_feature_model_id();
        Assert.assertEquals("Device_feature_model_id", device_feature_model_id);
    }

    @Test
    public void testGetState_key() throws Exception {
        String state_key = client.getState_key();
        Assert.assertEquals(null, state_key);

        client.setState_key("State_key");
        state_key = client.getState_key();
        Assert.assertEquals("State_key", state_key);
    }

    @Test
    public void testGetParameters() throws Exception {
        String parameters = client.getParameters();
        Assert.assertEquals(null, parameters);

        client.setParameters("Parameters");
        parameters = client.getParameters();
        Assert.assertEquals("Parameters", parameters);
    }

    @Test
    public void testGetValue_type() throws Exception {
        String value_type = client.getValue_type();
        Assert.assertEquals(null, value_type);

        client.setValue_type("Value_Type");
        value_type = client.getValue_type();
        Assert.assertEquals("Value_Type", value_type);
    }
/*
//todo disabled until find a good way to handle test
//on graphics as they change constantly
    @Test
    public void testGeRessources() throws Exception {
        Integer resources = client.getRessources();
        Assert.assertEquals(2.130837669E9, resources, 0);

        client.setDevice_usage_id("door");
        client.setState(0);
        resources = client.getRessources();
        Assert.assertEquals(2.130837684E9, resources, 0);

        client.setState(1);
        resources = client.getRessources();
        Assert.assertEquals(2.130837685E9, resources, 0);
    }
*/

    @Test
    public void testGetDevice_type() throws Exception {
        String device_type = client.getDevice_type();
        Assert.assertEquals(null, device_type);

        client.setDevice_type_id("device_type");
        device_type = client.getDevice_type();
        Assert.assertEquals("device_type", device_type);

        client.setDevice_type_id("device_type.devicepart2");
        device_type = client.getDevice_type();
        Assert.assertEquals("devicepart2", device_type);
    }

    @Test
    public void testGetDevice_type_id() throws Exception {
        String device_type_id = client.getDevice_type_id();
        Assert.assertEquals(null, device_type_id);

        client.setDevice_type_id("Device_Type_ID");
        device_type_id = client.getDevice_type_id();
        Assert.assertEquals("Device_Type_ID", device_type_id);
    }

    @Test
    public void testGetIcon_name() throws Exception {
        //// TODO: 06/06/2016
    }
}