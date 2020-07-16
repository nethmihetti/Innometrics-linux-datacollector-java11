package App;

import App.model.Model;
import javafx.application.Application;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Main extends Application {
    private Stage window;
    //Controller loginController = new Controller();


    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        window = primaryStage;

        //Set window title, width & height
        window.setTitle("InnoMetrics Login");
        window.setMinWidth(360);
        window.setMinHeight(350);

        String settingsPath = "/config.json";
        Model userModel = new Model(settingsPath);

        JSONObject userSettings = readSettings(settingsPath);

        if(userSettings.get("token") != null){ //and not expired
            //open main window without login page
            if (tokenIsValid(userSettings.get("tokenDate").toString())){
                System.out.println("111111");
            }
            System.out.println("My token is : " + userSettings.get("token") );
            userModel.flipToMainPage(window);

        }
        else {
            System.out.println("Login first!!");
            userModel.flipToLoginPage(window);
        }
        window.setResizable(false);
        window.show();

    }
    private JSONObject readSettings(final String settingsPath){
        JSONObject results = null;
        try{
            Path p = Paths.get(this.getClass().getResource(settingsPath).getPath()).toAbsolutePath();
            System.out.println(Files.isReadable(p));
            FileReader reader = new FileReader(this.getClass().getResource("/config.json").getPath());
            JSONParser jsonParser = new JSONParser();

            results = (JSONObject) jsonParser.parse(reader);
        } catch (ParseException e) {
            System.out.println("settings JSON ParseException");
            //e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println(settingsPath + " : File not found");
            //create a new settings file
            return new JSONObject();
            //e.printStackTrace();
        } catch (IOException e) {
            System.out.println("File IO Error");
            return null;
            //e.printStackTrace();
        }

        return results;
    }
    private boolean tokenIsValid(final String tokenDate){
        LocalDate today = LocalDate.now();
        today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Date d1 = null;
        Date d2 = null;

        try {
            d2 = format.parse(today.toString());
            d1 = format.parse(tokenDate);

            //in days
            long diff = d2.getTime() - d1.getTime();

            long diffDays = diff / (24 * 60 * 60 * 1000);

            System.out.print(diffDays + " days, ");

            if (diffDays < 30){
                return true;
            }
            else {
                return false;
            }


        } catch (Exception e) {
            System.out.println("Token Date exception");
            return false;
            //e.printStackTrace();
        }
    }
}
