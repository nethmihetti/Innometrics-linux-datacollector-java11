package App;

import App.model.Model;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginPage {
    public LoginPage(){}

    private static final String uri = "https://innometric.guru:9091/login";
    public static String token = "";

    private static String login(String username, String password) throws JSONException {
        final String projectID = "1234";
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("email", username);
        jsonBody.put("password", password);
        jsonBody.put("projectID", projectID);

        System.out.println(jsonBody.toString());

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                .build();

        try {
            HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200 || response.statusCode() == 500) {
                JSONObject responseBody = new JSONObject(response.body().toString());
                token = responseBody.get("token").toString();
                System.out.println(token);
            } else {
                System.out.println(response.statusCode());
                throw new SecurityException(Integer.toString(response.statusCode()));
            }
        } catch (Exception ex) {
            System.out.println("GOT AN EXCEPTION!!");
            //throw new RuntimeException(ex);
        }
        return token;
    }

    public Scene constructLoginPage(Model m, Stage window){
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(5, 10, 5, 10));

        //Set login scene title
        final Label scenetitle = new Label("Login to Data Collector");
        scenetitle.setMaxWidth(Double.MAX_VALUE);
        scenetitle.setAlignment(Pos.CENTER);
        scenetitle.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        scenetitle.setPadding(new Insets(25, 25, 10, 25));
        loginGrid.add(scenetitle, 0, 0, 3, 1);

        //Adding Nodes to loin GridPane layout
        Label userName = new Label("Login");
        final TextField txtUserName = new TextField("g.dlamini@innopolis.university");
        Label lblPassword = new Label("Password");
        final PasswordField passwordField = new PasswordField();
        passwordField.setText("InnoMetrics$2020");

        //Login Button
        Button btnLogin = new Button("Login");
        btnLogin.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.setPadding(new Insets(10, 5, 5, 5));
        hbBtn.getChildren().add(btnLogin);

        //Optional auth status
        final Label lblMessage = new Label();

        // Adding nodes to Login grid
        loginGrid.add(userName, 0, 1);
        loginGrid.add(txtUserName, 1, 1);
        loginGrid.add(lblPassword, 0, 2);
        loginGrid.add(passwordField, 1, 2);
        loginGrid.add(hbBtn, 1, 3);
        loginGrid.add(lblMessage, 1, 4);

        btnLogin.setOnAction(new EventHandler<ActionEvent>() {
            private void loggedIn() throws JSONException {
                m.saveUsername(txtUserName);
                btnLogin.setDisable(true);
                //displayMainPage.run();
            }
            @Override
            public void handle(ActionEvent e) {
                btnLogin.setDisable(true);
                String username = txtUserName.getText();
                String password = passwordField.getText();
                try {
                    String loginRes = login(username, password);

                    if(!loginRes.equals("")) {
                        //lblMessage.setFill(Color.GREEN);
                        m.setUser(username);
                        m.setToken(loginRes);
                        lblMessage.setText("Login Success");
                        loggedIn();
                        m.flipToMainPage(window);

                    } else {
                        lblMessage.setText("Login failed. Try again");
                        btnLogin.setDisable(false);
                    }
                } catch (JSONException | SocketException ex) {
                    ex.printStackTrace();
                    lblMessage.setText("Login failed. Try again");
                }
            }
        });

        Scene LoginScene = new Scene(loginGrid, 300, 275);

        return LoginScene;
    }

}
