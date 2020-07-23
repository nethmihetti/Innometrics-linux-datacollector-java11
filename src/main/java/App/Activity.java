package App;
import App.model.Model;
import org.json.JSONException;
import org.json.simple.JSONObject;

public class Activity {
    private int activityID = -1;
    private String activityType = "";
    private String browser_title = "";
    private String browser_url = "";
    private String end_time = "";
    private String executable_name = "";
    private Boolean idle_activity= null;
    private String ip_address= "";
    private String mac_address= "";
    private String osversion= "";
    private String pid= "";
    private String start_time= "";
    private String userID= "123";

    public Activity(){}
    public JSONObject toJson() throws JSONException {
        JSONObject result = new JSONObject();

        result.put("activityID",activityID);
        result.put("activityType",activityType);
        result.put("browser_title",browser_title);
        result.put("browser_url",browser_url);
        result.put("end_time",end_time);
        result.put("idle_activity",idle_activity);
        result.put("ip_address",ip_address);
        result.put("mac_address",mac_address);
        result.put("osversion",osversion);
        result.put("pid",pid);
        result.put("start_time",start_time);
        result.put("userID",userID);

        return result;
    }
    public void setActivityValues(Model m){
        System.out.println("Setting activity values");
        this.mac_address = Model.currentMAC;
        this.ip_address = Model.currentIP;
        this.osversion = Model.currentOS;
        this.userID = m.getUsername();
    }
}
