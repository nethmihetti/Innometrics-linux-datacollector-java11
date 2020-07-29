package App;

import App.model.Model;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.util.List;

public class SystemProcess {
    private String collectedTime = "";
    private String ip_address = "";
    private String mac_address = "";
    private List<JSONObject> measurementReportList ;
    private String osversion = "";
    private int pid = -1;
    private String processName = "";
    private String userID;

    public SystemProcess(){}

    public JSONObject toJson() throws JSONException {
        JSONObject result = new JSONObject();

        result.put(mac_address,this.mac_address);
        result.put(ip_address,this.ip_address);
        result.put(osversion,this.osversion);

        return result;
    }

    public void setProcessValues(Model m) {
        //System.out.println("Setting activity values");
        this.mac_address = Model.currentMAC;
        this.ip_address = Model.currentIP;
        this.osversion = Model.currentOS;
        this.userID = m.getUsername();
    }

}
