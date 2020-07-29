package App;

import App.model.Model;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Main extends Application {
    private Stage window;

    public static void main(String[] args) {
        launch(args);
    }

    public Stage getMainWindow(){
        assert Platform.isFxApplicationThread();
        return this.window;
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

        JSONObject userSettings = userModel.getUserSettingsJSON();

        if(userSettings.get("token") != null && tokenIsValid(userSettings.getOrDefault("tokenDate","2000-01-01").toString())){ //and not expired
            //open main window without login page

            System.out.println("My token is : " + userSettings.get("token") );
            System.out.println("My token date : " + userSettings.get("tokenDate") );

            userModel.flipToMainPage(window);
        }
        else {
            System.out.println("Login first!!");
            userModel.flipToLoginPage(window);
        }
        window.setResizable(false);
        window.show();

    }

    private boolean tokenIsValid(final String tokenDate){ //Todo: Move to helper methods
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

            return diffDays < 30;

        } catch (Exception e) {
            System.out.println("Token Date exception");
            return false;
        }
    }

}
