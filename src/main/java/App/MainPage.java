package App;

import App.model.Model;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import static javafx.scene.text.TextAlignment.CENTER;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Optional;

public class MainPage {
    public MainPage(){}

    public static String getLocalIP(){
        //Get IP-address
        String HostIp = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            HostIp = socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        return HostIp;
    }
    public static String getLocalMac() throws SocketException {
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

        return macAdrs;
    }

    public static String getLocalOSVersion(){
        String osName = "";
        String osVersion = "";
        try {
            osName = System.getProperty("os.name");
            if (osName == null) {
                throw new IOException("os.name not found");
            }
            osName = osName.toLowerCase(Locale.ENGLISH);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            osVersion = System.getProperty("os.version");
        }
        return osName+" "+osVersion;
    }

    private GridPane getMainTab(Model m) throws SocketException {
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
        mainTitleBox.getChildren().add(currentSess);
        mainGrid.add(mainTitleBox, 0, 0, 2, 1);

        final Label UserName = new Label("User Name:");
        final Label HostIpAdrs = new Label("IP-address:");
        final Label HostMacAdrs = new Label("Mac-address:");
        final Label HostOS = new Label("Operating System:");

        Label HostOsVal = new Label(this.getLocalOSVersion());
        Label HostIpAdrsVal = new Label(this.getLocalIP());
        Label UserNameVal = new Label(m.getUsername());
        Label HostMacAdrsVal = new Label(this.getLocalMac());


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
        /*javafx.scene.image.Image activeAppIcon = new javafx.scene.image.Image(this.getClass().getResource("/metrics-collector.png").toExternalForm());
        HBox activeAppBox = new HBox(10);
        activeAppBox.setAlignment(Pos.BOTTOM_CENTER);
        activeAppBox.getChildren().add(new ImageView(activeAppIcon));
        mainGrid.add(activeAppBox, 0, 4,1,1);*/

        //focused window process name
        TextFlow flow = new TextFlow();
        flow.setTextAlignment(CENTER);
        Label windowName = m.getWindowName();
        //Text focusTime = new Text("00:00:05"); //TODO: Create a timer for each focused window
        flow.getChildren().add(windowName);

        VBox focusedVBox = new VBox(10);
        focusedVBox.setAlignment(Pos.CENTER);
        focusedVBox.getChildren().add(flow);
        //focusedVBox.getChildren().add(focusTime);
        mainGrid.add(focusedVBox,0,4,2,1); //add to main grid

        Button stopCloseButton = new Button();
        stopCloseButton.setStyle("-fx-background-color: #399cbd; -fx-text-fill: white");
        stopCloseButton.setText("Stop and Quit");
        stopCloseButton.setFont(Font.font("Verdana",FontWeight.BOLD,15));
        stopCloseButton.setPadding(new Insets(10));

        stopCloseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                m.shutdown();
            }
        });

        HBox stopCloseHbox = new HBox(10);
        stopCloseHbox.setAlignment(Pos.CENTER);
        stopCloseHbox.setPadding(new Insets(10));
        stopCloseHbox.getChildren().add(stopCloseButton);

        mainGrid.add(stopCloseHbox,0,5,2,1);

        return mainGrid;
    }

    private GridPane getSettingsTab(Model m){
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
        SpinnerValueFactory<Integer> dataCollectValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 60, initialValue);
        dataCollectIntvl.setValueFactory(dataCollectValueFactory);
        dataCollectIntvl.setMaxWidth(70.0);

        final Label dataSendIntvLabel = new Label("Data send interval (min)");
        final Spinner<Integer> dataSendIntv = new Spinner<Integer>();
        SpinnerValueFactory<Integer> dataSendValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 60, 10);
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

        btnSaveSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                if(m.validSettings(dataSendIntv.getValue().intValue(),dataCollectIntvl.getValue().intValue())){
                    m.setDataColSendIntvl(dataSendIntv.getValue().intValue(),dataCollectIntvl.getValue().intValue());
                    System.out.println("Settings saved successful");
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Settings Save Error");
                    alert.setHeaderText("Settings save unsuccessful");
                    alert.setContentText("The data send interval is greater data collect interval ");

                    alert.showAndWait();
                }
            }
        });

        return settingsGrid;
    }

    private GridPane getAboutTab(Model m){
        //About Tab
        GridPane aboutGrid = new GridPane();
        aboutGrid.setAlignment(Pos.CENTER);
        aboutGrid.setHgap(10);
        aboutGrid.setVgap(10);
        aboutGrid.setPadding(new Insets(10, 5, 10, 5));

        //Add InnoMetrics Icon
        Image image = new Image(this.getClass().getResource("/metrics-collector.png").toExternalForm());
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
        final Label LoginUsername = new Label(m.getLoginUsername());
        LoginUsername.setMaxWidth(300);
        LoginUsername.setTextAlignment(CENTER);
        LoginUsername.setWrapText(true);
        aboutVbox.getChildren().add(usern);
        aboutVbox.getChildren().add(LoginUsername);
        aboutGrid.add(aboutVbox,0,1);

        // add logout and update check
        HBox hboxLogInUpdate = new HBox(15);
        hboxLogInUpdate.setAlignment(Pos.BOTTOM_CENTER);
        //Button updateBtn = new Button("Update");
        //updateBtn.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        Button logOutBtn = new Button("Logout");
        logOutBtn.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        hboxLogInUpdate.setPadding(new Insets(20,0,5,0));
        hboxLogInUpdate.getChildren().addAll(logOutBtn);
        aboutGrid.add(hboxLogInUpdate,0,2);

        logOutBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Sign out Confirmation");
                alert.setHeaderText("This action will log you out and reset your settings");
                alert.setContentText("Are you ok with this?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    try {
                        m.endWatching(true);
                        m.flipToLoginPage((Stage) logOutBtn.getScene().getWindow());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                }
            }
        });

        return aboutGrid;
    }

    public Scene constructMainPage(Model m) throws SocketException {

        TabPane tabPane = new TabPane();

        Tab tab1 = new Tab("Main");
        GridPane mainGrid = this.getMainTab(m);
        tab1.setContent(mainGrid);

        Tab tab2 = new Tab("Settings");
        GridPane settingsGrid = this.getSettingsTab(m);
        tab2.setContent(settingsGrid);

        Tab tab3 = new Tab("About");
        GridPane aboutGrid = this.getAboutTab(m);
        tab3.setContent(aboutGrid);

        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        tabPane.getTabs().add(tab3);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.tabMinWidthProperty().set(100);//set the tabPane's tabs min and max widths to be the same.
        tabPane.tabMaxWidthProperty().set(100);

        //set the tabPane's minWidth and maybe max width to the tabs combined width + a padding value
        tabPane.setMinWidth((100 * tabPane.getTabs().size()) + 55);
        tabPane.setPrefWidth((100 * tabPane.getTabs().size()) + 55 );

        VBox vBox = new VBox(tabPane);
        vBox.setAlignment(Pos.TOP_CENTER);
        Scene mainScene = new Scene(vBox,360, 350);

        return mainScene;

    }
}
