package App;

import App.model.*;
import javafx.application.Platform;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SettingsPersister {
    private boolean prettyFile;
    private volatile JSONObject cache;
    private final Path p;

    public SettingsPersister(String settingsfile){
        this.prettyFile = true;
        this.p = Path.of("src/main/resources"+settingsfile);
        this.cache = create(this.p);
    }

    private synchronized JSONObject create(Path settingsFile) {
        try {
            //Path p = Paths.get(SettingsPersister.class.getResource(settingsFile).getPath()).toAbsolutePath();
            JSONObject json = null;
            if (Files.exists(settingsFile)) {
                FileReader reader = new FileReader(settingsFile.toString());
                JSONParser jsonParser = new JSONParser();
                json = (JSONObject) jsonParser.parse(reader);
            } else {
                Files.createFile(p);
                json = new JSONObject();
                Writer writer = new FileWriter(settingsFile.toString());
                json.writeJSONString(writer);
                writer.close();
            }
            if (!Files.isWritable(p)) {
                System.err.println("Non-writable settings file");
                return new JSONObject();
            }
            return json;
        } catch (IOException | ParseException e) {
            return new JSONObject();
        }
    }
    private void commit() {
        if (this.p == null)
            return;

        try {
            System.out.println("Saving Json at : "+this.p.toString());
            Writer writer = new FileWriter(this.p.toString());
            this.cache.writeJSONString(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized String get(String key) {
        return (String) this.cache.getOrDefault(key,"Null");
    }
    public synchronized void putSetting(String key, String value) {
        this.cache.put(key,value);
        commit();
    }

    public synchronized void updateSettings(Model m){

        if (this.cache.get("token") == null){
            LocalDate today = LocalDate.now();
            today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            today.toString();
            this.cache.put("tokenDate",today.toString());
            this.cache.put("token",m.getLoggedInSessionToken());
        }
        this.cache.put("username",m.getUsername());
        this.cache.put("loginUsername",m.getLoginUsername());
        System.out.println("saving : " + this.cache.toJSONString());
        this.commit();
    }
    public void setSendcollectIntvl(int send, int collect){
        this.cache.put("sendInterval",send);
        this.cache.put("collectInterval",collect);
        this.commit();
    }

    public void cleanup() throws IOException {
        try {
            JSONObject json = new JSONObject();
            Writer writer = new FileWriter(p.toString());
            json.writeJSONString(writer);
            writer.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
