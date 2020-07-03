package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;

public class Login extends Application {

    private Stage window;
    String user = "Java";
    String real_pass = "pass";
    String checkUser, checkPw;

    @Override
    public void start(Stage primaryStage) throws Exception {

        window = primaryStage;

        //Set window title, width & height
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        //window.setScene(new Scene(root, 350, 275));
        window.setTitle("InnoMetrics Login");
        window.setWidth(360);
        window.setHeight(300);
        window.show();

        //Login Grid create
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(25, 25, 25, 25));

        //Set login scene title
        //Text scenetitle = new Text("Login to Data Collector");
        final Label scenetitle = new Label("Login to Data Collector");
        scenetitle.setMaxWidth(Double.MAX_VALUE);
        scenetitle.setAlignment(Pos.CENTER);
        scenetitle.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        scenetitle.setPadding(new Insets(25, 25, 10, 25));
        loginGrid.add(scenetitle, 0, 0, 3, 1);

        //Adding Nodes to loin GridPane layout
        Label userName = new Label("Login");
        final TextField txtUserName = new TextField();
        Label lblPassword = new Label("Password");
        final PasswordField passwordField = new PasswordField();

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

        Scene LoginScene = new Scene(loginGrid, 300, 275);

        //Welcome Grid and scene (After successful login)
        //About Tab
        GridPane aboutGrid = new GridPane();
        aboutGrid.setAlignment(Pos.CENTER);
        aboutGrid.setHgap(10);
        aboutGrid.setVgap(10);
        aboutGrid.setPadding(new Insets(25, 25, 25, 25));

        //Text aboutTemporalTitle = new Text("Data collector version \nnumber");
        final Label aboutTemporalTitle = new Label("Data collector version number");
        aboutTemporalTitle.setMaxWidth(Double.MAX_VALUE);
        aboutTemporalTitle.setAlignment(Pos.CENTER);
        aboutTemporalTitle.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
        aboutGrid.add(aboutTemporalTitle, 0, 3);

        //Add InnoMetrics Icon
        javafx.scene.image.Image image = new javafx.scene.image.Image(getClass().getResource("metrics-collector.png").toExternalForm());
        HBox hbimg = new HBox(10);
        hbimg.setAlignment(Pos.BOTTOM_CENTER);
        hbimg.getChildren().add(new ImageView(image));
        aboutGrid.add(hbimg, 0, 1,2,1);

        // add logout and update check
        HBox hboxLogInUpdate = new HBox(15);
        hboxLogInUpdate.setAlignment(Pos.BOTTOM_CENTER);
        Button updateBtn = new Button("Update");
        updateBtn.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        Button loginBtn = new Button("Logout");
        loginBtn.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        hboxLogInUpdate.setPadding(new Insets(20,0,5,0));
        hboxLogInUpdate.getChildren().addAll(updateBtn, loginBtn);
        aboutGrid.add(hboxLogInUpdate,0,4,3,3);

        //Main Tab contents
        GridPane mainGrid = new GridPane();
        mainGrid.setAlignment(Pos.CENTER);
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(25, 25, 25, 25));

        Text mainGridTemporalTitle = new Text("Main data collector \nprocesses status Show \n(activities, actions, etc..)");
        mainGridTemporalTitle.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
        mainGrid.add(mainGridTemporalTitle, 0, 0, 2, 1);

        //Settings tab contents
        GridPane settingsGrid = new GridPane();
        settingsGrid.setAlignment(Pos.CENTER);
        settingsGrid.setHgap(10);
        settingsGrid.setVgap(10);
        settingsGrid.setPadding(new Insets(25, 25, 25, 25));

        Text settingsGridTemporalTitle = new Text("Data collector settings");
        settingsGridTemporalTitle.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
        settingsGrid.add(settingsGridTemporalTitle, 0, 0, 2, 1);

        //save settings Button
        Button btnSaveSettings = new Button("Save Settings");
        btnSaveSettings.setFont(Font.font("Verdana",FontWeight.BOLD,20));
        HBox hbBtn1 = new HBox(10);
        hbBtn1.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn1.getChildren().add(btnSaveSettings);
        settingsGrid.add(hbBtn1, 0, 3,2,1);

        //create tabs group for Main GUI
        Group root = new Group();
        TabPane tabPane = new TabPane();

        BorderPane borderPane = new BorderPane();

        Tab tab1 = new Tab("Main");
        tab1.setContent(mainGrid);
        Tab tab2 = new Tab("Settings");
        tab2.setContent(settingsGrid);
        Tab tab3 = new Tab("About");
        tab3.setContent(aboutGrid);

        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        tabPane.getTabs().add(tab3);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.tabMinWidthProperty().set(100);//set the tabPane's tabs min and max widths to be the same.
        tabPane.tabMaxWidthProperty().set(100);

        //set the tabPane's minWidth and maybe max width to the tabs combined width + a padding value
        tabPane.setMinWidth((100 * tabPane.getTabs().size()) + 55);
        tabPane.setPrefWidth((100 * tabPane.getTabs().size()) + 55);

        borderPane.setCenter(tabPane);
        root.getChildren().add(borderPane);

        VBox vBox = new VBox(tabPane);
        Scene mainScene = new Scene(vBox,300, 275);

        //Login button action on press
        btnLogin.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                checkUser = txtUserName.getText().trim();
                checkPw = passwordField.getText().trim();
                if(checkUser.equals(user) && checkPw.equals(real_pass)){
                    lblMessage.setText("Success!");
                    lblMessage.setTextFill(Color.GREEN);
                    window.setTitle("InnoMetrics data collector");
                    window.setScene(mainScene);

                }
                else{
                    System.out.println(checkUser);
                    System.out.println(checkPw);
                    lblMessage.setText("Incorrect email or password\nTry again");
                    lblMessage.setTextFill(Color.RED);
                    passwordField.setText("");

                }
            }
        });

        //fix the size of window to constant/fixed
        window.setResizable(false);
        window.setScene(LoginScene);
    }
}
