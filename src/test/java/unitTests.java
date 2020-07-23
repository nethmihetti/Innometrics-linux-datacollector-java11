import App.Activity;
import App.Main;
import App.model.Model;
import com.sun.javafx.application.PlatformImpl;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class unitTests extends Main {

    @Before
    public void StatUp(){
        PlatformImpl.startup(()->{});
    }

    @Test
    public void activity_class_constructor_test() throws JSONException, IOException {
        Activity activity1 = new Activity();
        JSONObject activityJson = activity1.toJson();
        Assert.assertTrue(activityJson.get("osversion").equals(""));
    }

    @Test
    public void activity_class_json_convert_test() throws JSONException, IOException {
        PlatformImpl.startup(()->{}); //to initialize javafx objects
        Activity activity1 = new Activity();
        Model m1 = new Model("/testConfig.json");
        Assert.assertTrue(m1.windowName.getText().equals("None"));
        JSONObject activityJson = activity1.toJson();
        Assert.assertNotNull(activityJson.get("osversion"));
        Assert.assertNotNull(activityJson.get("userID"));
    }

    @Test
    public void model_class_test() {
        PlatformImpl.startup(()->{});
        Model m1 = new Model("/testConfig.json");
        Assert.assertTrue(m1.windowName.getText().equals("None"));
    }

}
