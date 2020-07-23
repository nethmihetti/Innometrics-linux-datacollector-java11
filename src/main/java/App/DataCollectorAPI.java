package App;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class DataCollectorAPI {

    private String activityPostUrl;
    private String processPostUrl;
    private String token = "";

    public DataCollectorAPI(){
        this.activityPostUrl = "http://10.90.138.244:9091/V1/activity";
        this.processPostUrl = "http://10.90.138.244:9091/V1/process";
    }
    public DataCollectorAPI(String activityPostUrl,String processPostUrl,String token){
        this.activityPostUrl = activityPostUrl;
        this.processPostUrl = processPostUrl;
        this.token = token;
    }

    void postActivities(Activity[] activities) {

        HttpClient client = HttpClient.newBuilder().build();
        JSONArray allActivities = new JSONArray();

        for (Activity activity : activities) { allActivities.put(activity); }

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .header("Token", "")
                .uri(URI.create(activityPostUrl))
                .POST(HttpRequest.BodyPublishers.ofString(allActivities.toString()))
                .build();

        try {
            HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Data post Status code : "+response.statusCode());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    void postProcessReport(Process[] processes) {

        HttpClient client = HttpClient.newBuilder().build();
        JSONArray allProcesses = new JSONArray();

        for (Process p : processes) { allProcesses.put(p); }

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .header("Token", "")
                .uri(URI.create(processPostUrl))
                .POST(HttpRequest.BodyPublishers.ofString(allProcesses.toString()))
                .build();

        try {
            HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Data post Status code : "+response.statusCode());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
