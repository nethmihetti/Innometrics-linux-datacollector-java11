package App.model;

import App.LoginPage;
import App.MainPage;
import App.SettingsPersister;
import App.nativeimpl.ActiveWindowInfo;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONException;


import java.awt.*;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;


public class Model {

	private final SettingsPersister settings;
	private TrayIcon trayIcon;
	public volatile Label windowName = new Label("None");
	private String loginUsername, username;
	private PasswordField loginPassword;
	private final Map<String, Long> timeCount;
	private long lastSwitchTime;
	public String process = new String("1111");
	private int dataCollectInvl, dataSendInvl;
	private String token;
	public static String currentIP, currentMAC, currentOS;

	//constructor
	public Model(String settingsFile) {
		settings = new SettingsPersister(settingsFile);
		this.windowName.setPrefWidth(200);
		this.windowName.setWrapText(true);
		this.windowName.setAlignment(Pos.CENTER);
		this.loginUsername = "";
		timeCount = new HashMap<String, Long>();

	}

	public void setWindowName(final String newName){
		windowName.setText(newName);
	}
	public Label getWindowName() {
		return windowName;
	}

	public void beginWatching() {
		assert Platform.isFxApplicationThread(); //make sure its the main thread
		//Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);
		//watchStopper = ActiveWindowInfo.INSTANCE.startListening(this);
		System.out.println("Starting watch thread!!");
		ActiveWindowInfo.INSTANCE.startListening(this);
	}
	public void endWatching(boolean cleanup) throws IOException {
		if (cleanup) {
			//Reset the settings (delete settings)
			settings.cleanup();
		}
		System.out.println("Stopping watch thread!!");
	}
	public void shutdown(){
		System.out.println("Data Collector is shutting down!");
		Platform.exit();
	}

	public void flipToMainPage(Stage window) throws SocketException {
		assert Platform.isFxApplicationThread();

		MainPage mainPage = new MainPage();
		this.currentIP = MainPage.getLocalIP();
		this.currentMAC = MainPage.getLocalMac();
		this.currentOS = MainPage.getLocalOSVersion().split(" ")[0];

		this.loginUsername = settings.get("username");
		this.username = settings.get("loginUsername");

		window.setTitle("InnoMetrics Data Collector");
		window.setScene(mainPage.constructMainPage(this));
		beginWatching();
	}
	public void setLoginPageComponents(String loginUsername, PasswordField loginPassword) {
		assert Platform.isFxApplicationThread();

		this.loginUsername = loginUsername;
		this.username = loginUsername.split("@")[0];
		this.loginPassword = loginPassword;
	}
	public void flipToLoginPage(Stage window) throws IOException {
		assert Platform.isFxApplicationThread();
		endWatching(false);
		LoginPage startPage = new LoginPage();
		window.setScene(startPage.constructLoginPage(this,window));
	}

	public String getLoggedInSessionToken() {
		return this.token;
	}

	public String getUsername() {
		return this.username;
	}
	public void saveUsername(final TextField UsernameField) throws JSONException {
		String username = UsernameField.getText().trim();
		if (username != null && !username.isEmpty())
			System.out.println("Saving user name!! ");
		settings.putSetting("username",username);
		settings.putSetting("loginUsername",username.split("@")[0]);
	}

	public String getLoginUsername() {
		return this.loginUsername;
	}
	public void setToken(String token){this.token = token;}

	public void setDataColSendIntvl(final int sendIntvl,final int dataCollectInvl ){
		if (validSettings(sendIntvl,dataCollectInvl)) {
			this.dataSendInvl = sendIntvl;
			this.dataCollectInvl = dataCollectInvl;
			this.settings.setSendcollectIntvl(this.dataSendInvl,this.dataCollectInvl);
		}
	}
	public int getDataCollectInvl(){
		return this.dataCollectInvl;
	}
	public int getDataSendInvl(){
		return dataSendInvl;
	}
	public boolean validSettings(final int sendIntvl,final int dataCollectInvl ){
		return sendIntvl > dataCollectInvl;
	}

	public void updateLoinSettings(String loginRes, String loginUsername, PasswordField loginPassword){
		this.setLoginPageComponents(loginUsername,loginPassword);
		this.setToken(loginRes);
		settings.updateSettings(this);
	}

	public void setTrayIcon(TrayIcon trayIcon) {
		this.trayIcon = trayIcon;
	} //todo: Implement trayIcon
	public void setProcess(String process) {

		this.process = process;
		long now = System.currentTimeMillis();
		Long runningTime = timeCount.get(process);
		if (process != null)
			timeCount.put(process, (runningTime != null ? runningTime.longValue() : 0) + now - lastSwitchTime);
		this.lastSwitchTime = now;
	}
}
