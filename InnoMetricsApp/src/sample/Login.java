package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Locale;

import static javafx.scene.text.TextAlignment.CENTER;

public class Login extends Application {

    private Stage window;
    String user = "Java";
    String real_pass = "pass";
    String checkUser, checkPw = "";

    @Override
    public void stop(){
        System.out.println("Stage is closing");
        // Save file
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        window = primaryStage;

        //Set window title, width & height
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        //window.setScene(new Scene(root, 350, 275));
        window.setTitle("InnoMetrics Login");
        window.setWidth(360);
        window.setHeight(350);
        window.show();

        //Login Grid create
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(5, 10, 5, 10));

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
        //aboutGrid.setGridLinesVisible(true);
        aboutGrid.setHgap(10);
        aboutGrid.setVgap(10);
        aboutGrid.setPadding(new Insets(10, 5, 10, 5));

        //Add InnoMetrics Icon
        javafx.scene.image.Image image = new javafx.scene.image.Image(getClass().getResource("metrics-collector.png").toExternalForm());
        HBox hbimg = new HBox(10);
        hbimg.setAlignment(Pos.BOTTOM_CENTER);
        hbimg.getChildren().add(new ImageView(image));
        aboutGrid.add(hbimg, 0, 0);


        VBox aboutVbox = new VBox(10);
        aboutVbox.setAlignment(Pos.CENTER);
        final Label collectorVersion = new Label("Version : 1.0.1");
        collectorVersion.setMaxWidth(Double.MAX_VALUE);
        collectorVersion.setAlignment(Pos.CENTER);
        collectorVersion.setTextAlignment(CENTER);
        collectorVersion.setFont(Font.font(collectorVersion.getFont().toString(), FontWeight.NORMAL, 15));
        aboutVbox.getChildren().add(collectorVersion);

        //User account
        final Label usern = new Label("Logged in as");
        usern.setFont(Font.font( usern.getFont().toString(),FontWeight.BOLD,15 ));
        usern.setTextAlignment(CENTER);
        final Label versionNumber = new Label("g.dlamini@innopolis.university");
        versionNumber.setMaxWidth(300);
        versionNumber.setTextAlignment(CENTER);
        versionNumber.setWrapText(true);
        aboutVbox.getChildren().add(usern);
        aboutVbox.getChildren().add(versionNumber);
        aboutGrid.add(aboutVbox,0,1);

        // add logout and update check
        HBox hboxLogInUpdate = new HBox(15);
        hboxLogInUpdate.setAlignment(Pos.BOTTOM_CENTER);
        Button updateBtn = new Button("Update");
        updateBtn.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        Button loginBtn = new Button("Logout");
        loginBtn.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        hboxLogInUpdate.setPadding(new Insets(20,0,5,0));
        hboxLogInUpdate.getChildren().addAll(updateBtn, loginBtn);
        aboutGrid.add(hboxLogInUpdate,0,2);
//        aboutGrid.add(hboxLogInUpdate,0,5,2,3);

        //Main Tab contents
        GridPane mainGrid = new GridPane();
        mainGrid.setAlignment(Pos.TOP_CENTER);
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(30, 15, 5, 15));
        //mainGrid.setGridLinesVisible(true);


        Text currentSess = new Text("Current session");
        currentSess.setFont(Font.font(currentSess.getFont().toString(), FontWeight.BOLD, 15));
        HBox mainTitleBox = new HBox();
        mainTitleBox.setAlignment(Pos.CENTER);
        //mainTitleBox.setPadding(new Insets(5,0,5,0));
        mainTitleBox.getChildren().add(currentSess);
        mainGrid.add(mainTitleBox, 0, 0, 2, 1);

        //Get IP-address
        String HostIp = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            HostIp = socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //Get MAC-address
        String macAdrs = "";
        Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
        NetworkInterface inter;
        while (networks.hasMoreElements()) {
            inter = networks.nextElement();
            byte[] mac = inter.getHardwareAddress();
            if (mac != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
                macAdrs = sb.toString();
            }
        }

        final Label UserName = new Label("User Name:");
        final Label HostIpAdrs = new Label("IP-address:");
        final Label HostMacAdrs = new Label("Mac-address:");
        final Label HostOS = new Label("Operating System:");

        String osName, osVersion = "";
        try {
            osName = System.getProperty("os.name");
            if (osName == null) {
                throw new IOException("os.name not found");
            }
            osName = osName.toLowerCase(Locale.ENGLISH);
        }
        finally {
            osVersion = System.getProperty("os.version");
        }

        Label HostOsVal = new Label(osName+" "+osVersion);
        Label HostIpAdrsVal = new Label(HostIp);
        Label UserNameVal = new Label("g.dlamini");
        Label HostMacAdrsVal = new Label(macAdrs);

        VBox vBox1 = new VBox(10);
        vBox1.setAlignment(Pos.CENTER_LEFT);
        vBox1.getChildren().addAll(HostOsVal,UserNameVal,HostIpAdrsVal,HostMacAdrsVal);
        mainGrid.add(vBox1,1,1);


        VBox vBox2 = new VBox(10);
        vBox2.setMinWidth(130);
        vBox2.setAlignment(Pos.CENTER_LEFT);
        vBox2.getChildren().addAll(HostOS,UserName,HostIpAdrs,HostMacAdrs);
        mainGrid.add(vBox2,0,1);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(10,0,5,0));
        mainGrid.add(separator,0,2,2,1);

        //Focus Application
        Text activeapp = new Text("Current Focused Window");
        activeapp.setFont(Font.font(activeapp.getFont().toString(), FontWeight.BOLD, 15));
        HBox activeappTitleBox = new HBox();
        activeappTitleBox.setAlignment(Pos.CENTER);
        activeappTitleBox.getChildren().add(activeapp);
        mainGrid.add(activeappTitleBox, 0, 3, 2, 1);

        //focused window icon
        javafx.scene.image.Image activeAppIcon = new javafx.scene.image.Image(getClass().getResource("metrics-collector.png").toExternalForm());
        HBox activeAppBox = new HBox(10);
        activeAppBox.setAlignment(Pos.BOTTOM_CENTER);
        activeAppBox.getChildren().add(new ImageView(activeAppIcon));
        mainGrid.add(activeAppBox, 0, 4,1,1);

        //focused window process name
        TextFlow flow = new TextFlow();
        //flow.setTextAlignment(CENTER);
        Text windowName = new Text("InelliJIDEA Communiity edition 2020"); //TODO: return from data collector module
        Text focusTime = new Text("00:00:05"); //TODO: Create a timer for each focused window
        flow.getChildren().add(windowName);

        VBox focusedVBox = new VBox(10);
        focusedVBox.setAlignment(Pos.CENTER_LEFT);
        focusedVBox.getChildren().add(flow);
        focusedVBox.getChildren().add(focusTime);
        mainGrid.add(focusedVBox,1,4); //add to main grid

        //Settings tab contents
        GridPane settingsGrid = new GridPane();
        settingsGrid.setAlignment(Pos.CENTER);
        settingsGrid.setHgap(10);
        settingsGrid.setVgap(10);
        settingsGrid.setPadding(new Insets(25, 25, 25, 25));

        // Settings tab title
        final Text settingsGridTitle = new Text("Data collector settings");
        settingsGridTitle.setFont(Font.font(settingsGridTitle.getFont().toString(), FontWeight.NORMAL, 15));
        HBox titleHbox = new HBox(10);
        titleHbox.setAlignment(Pos.CENTER);
        titleHbox.getChildren().add(settingsGridTitle);
        settingsGrid.add(titleHbox, 0, 0,2,1);

        final int initialValue = 5;
        final Label dataCollectIntvlLabel  = new Label("Data Collection interval (min)");
        final Spinner<Integer> dataCollectIntvl = new Spinner<Integer>();
        SpinnerValueFactory<Integer> VdataCollectValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, initialValue);
        dataCollectIntvl.setValueFactory(VdataCollectValueFactory);
        dataCollectIntvl.setMaxWidth(70.0);

        final Label dataSendIntvLabel = new Label("Data send interval (min)");
        final Spinner<Integer> dataSendIntv = new Spinner<Integer>();
        SpinnerValueFactory<Integer> dataSendValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, initialValue);
        dataSendIntv.setValueFactory(dataSendValueFactory);
        dataSendIntv.setMaxWidth(70.0);

        settingsGrid.add(dataCollectIntvlLabel,0,2);
        settingsGrid.add(dataCollectIntvl,1,2);
        settingsGrid.add(dataSendIntvLabel,0,3);
        settingsGrid.add(dataSendIntv,1,3);

        //save settings Button
        Button btnSaveSettings = new Button("Save Settings");
        btnSaveSettings.setFont(Font.font("Verdana",FontWeight.BOLD,15));
        HBox hbBtn1 = new HBox(10);
        hbBtn1.setPadding(new Insets(10, 0, 10, 0));
        hbBtn1.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn1.getChildren().add(btnSaveSettings);
        settingsGrid.add(hbBtn1, 0, 4,2,1);

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
                    windowName.setText(windowName.getText().toUpperCase());

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
