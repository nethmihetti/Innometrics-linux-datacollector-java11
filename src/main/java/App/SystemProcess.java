package App;

import App.model.Model;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemProcess {
    private String collectedTime = "";
    private String ip_address = "";
    private String mac_address = "";
    private JSONArray measurementReportList ;
    private String osversion = "";
    private String pid = "";
    private String processName = "";
    private String userID;

    public SystemProcess(){
        measurementReportList = new JSONArray();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject result = new JSONObject();

        result.put("collectedTime",this.collectedTime);
        result.put("ip_address",this.ip_address);
        result.put("mac_address",this.mac_address);

        result.put("measurementReportList", measurementReportList);

        result.put("osversion",this.osversion);
        result.put("pid",this.pid);
        result.put("processName",this.processName);
        result.put("userID",this.userID);

        return result;
    }

    public void setProcessValues(Model m, Map <String, JSONObject>measurements,String collectedTime, String pid) {

        this.mac_address = Model.currentMAC;
        this.ip_address = Model.currentIP;
        this.osversion = Model.currentOS;
        this.userID = m.getLoginUsername();
        this.collectedTime = collectedTime;
        this.pid = pid;

        //set Measurement Report List
        for (Map.Entry<String, JSONObject> entry : measurements.entrySet()){
            String key = entry.getKey();
            JSONObject value = entry.getValue();

            JSONObject tempObj = new JSONObject();
            tempObj.put("alternativeLabel",key);
            tempObj.put("capturedDate",value.getOrDefault("capturedDate","None"));
            tempObj.put("measurementTypeId",value.getOrDefault("measurementTypeId","None"));
            tempObj.put("value",value.getOrDefault("value","0.0"));

            measurementReportList.put(tempObj);
        }
    }

}
