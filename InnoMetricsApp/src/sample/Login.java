package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Login extends Application {

    private Stage window;

    @Override
    public void start(Stage primaryStage) throws Exception {

        window = primaryStage;

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        window.setTitle("Hello World");
        window.setScene(new Scene(root, 350, 275));
        window.show();

        // Login Grid
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        loginGrid.add(scenetitle, 0, 0, 2, 1);

        Label userName = new Label("User Name:");
        loginGrid.add(userName, 0, 1);

        TextField userTextField = new TextField();
        loginGrid.add(userTextField, 1, 1);

        Label pw = new Label("Password:");
        loginGrid.add(pw, 0, 2);

        PasswordField pwBox = new PasswordField();
        loginGrid.add(pwBox, 1, 2);

        Button btn = new Button("Login");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        loginGrid.add(hbBtn, 1, 4);

        Scene LoginScene = new Scene(loginGrid, 300, 275);

        // Welcome Grid
        GridPane welocmeGrid = new GridPane();
        welocmeGrid.setAlignment(Pos.CENTER);
        welocmeGrid.setHgap(10);
        welocmeGrid.setVgap(10);
        welocmeGrid.setPadding(new Insets(25, 25, 25, 25));

        Text welcomeTitle = new Text("Welcome to Innometrics Data Collector");
        welcomeTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        welocmeGrid.add(welcomeTitle, 0, 0, 2, 1);

        Scene welcomeScene = new Scene(welocmeGrid, 300, 275);

        final Text actiontarget = new Text();
        loginGrid.add(actiontarget, 1, 6);

        // handle button even for sign-in
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                actiontarget.setFill(Color.FIREBRICK);
                actiontarget.setText("Login button pressed");
                window.setScene(welcomeScene);
            }
        });


        window.setScene(LoginScene);
    }
}
