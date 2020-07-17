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
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import javax.swing.*;
import java.awt.*;
import java.net.CookieManager;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Model {
	public static final String domain = "https://innometric.guru:9091/login";
	public static final String path = "";

	public static volatile boolean popupsDisabled = false;

	private static final ThreadLocal<SimpleDateFormat> rfc822 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz", Locale.US);
		}
	};


	private final SettingsPersister settings;
	private TrayIcon trayIcon;
	private JPanel root;
	public static Label windowName = new Label("None");
	private String loginUsername, username;
	private PasswordField loginPassword;
	private final Map<String, Long> timeCount;
	private long lastUpdateTime, lastSwitchTime;
	private Runnable watchStopper;
	private String process;
	private boolean updateLock;
	private int dataCollectInvl, dataSendInvl;

	private Model(SettingsPersister settingsImpl) {
		settings = settingsImpl;
		timeCount = new HashMap<String, Long>();
	}

	public Model(String settingsFile) {
		this(SettingsPersister.JsonFilePersister.create(settingsFile));
		assert Platform.isFxApplicationThread();
		windowName.setPrefWidth(200);
		windowName.setWrapText(true);
		windowName.setAlignment(Pos.CENTER);
	}
	public void setWindowName(final String newName){
		windowName.setText(newName);
	}

	public static Label getWindowName() {
		return windowName;
	}

	public void setTrayIcon(TrayIcon trayIcon) {
		this.trayIcon = trayIcon;
	} //todo: Implement trayIcon

	public void setLoginPageComponents(String loginUsername, PasswordField loginPassword) {
		assert Platform.isFxApplicationThread();

		this.loginUsername = loginUsername;
		this.username = loginUsername.split("@")[0];
		this.loginPassword = loginPassword;
	}

	public String getDefaultUsername() {
		String value = settings.get("userName");
		if (value == null)
			return "";
		return value;
	}

	public void saveUsername(final TextField UsernameField) throws JSONException {
		String username = UsernameField.getText();
		if (username != null && !username.trim().isEmpty())
			System.out.println("Saving user name!! ");
			//settings.put("loginUsername", username); //TODO: Update settings with username
	}

	private void update() throws JSONException {
		assert !Platform.isFxApplicationThread(); //Do db updates on separate thread
		// TODO: Create a thread lock
		if (updateLock) return;
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);

		Map<String, JSONObject> loaded = new HashMap<>();
		JSONObject json = new JSONObject();
		JSONArray apps = new JSONArray();

		json.put("user", username);
		json.put("apps", apps);
		System.out.println(json);
	}


	public void beginWatching() {
		assert Platform.isFxApplicationThread(); //make sure its the main thread
		//Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);
		//watchStopper = ActiveWindowInfo.INSTANCE.startListening(this);
		System.out.println("Starting watch thread!!");
		ActiveWindowInfo.INSTANCE.startListening(this);
	}

	public void endWatching(boolean cleanup) {
		/*if (watchStopper != null) {
			//watchStopper.run(); Todo stop watching thread
		}*/
		System.out.println("Stopping watch thread!!");
	}

	public void flipToMainPage(Stage window) throws SocketException {
		assert Platform.isFxApplicationThread();

		beginWatching();
		MainPage mainPage = new MainPage();
		window.setScene(mainPage.constructMainPage(this));
	}

	public void flipToLoginPage(Stage window) {
		assert Platform.isFxApplicationThread();

		endWatching(false);
		LoginPage startPage = new LoginPage();
		window.setScene(startPage.constructLoginPage(this,window));
	}

	public static void onWarning(Throwable error, String title) {
		System.err.println(title);
		error.printStackTrace();
	}

	public static void onError(final Component owner, Throwable error, final String title) {
		System.err.println(title);
		error.printStackTrace();
		//cause is usually more informative when we don't give the full callstack in the popup
		while (error.getCause() != null)
			error = error.getCause();
		final Throwable rootCause = error;
		//wait 100ms in case this error was made due to navigating away from page. by then, popupsDisabled will be set
	}

	public void onError(Throwable error, String title) {
		onError(root, error, title);
	}

	public String getLoggedInSessionToken() {
		return this.token;
	}

	public boolean isLoginUsernameFieldFilled() {
		return loginUsername != null && !loginUsername.equals("");
	}

	public void focusOnLoginField() {
		assert Platform.isFxApplicationThread();

		if (loginUsername != null)
			System.out.println("focusOnLoginField");
			//loginUsername.setEditable(true);//.requestFocusInWindow();
	}

	public void focusOnPasswordField() {
		assert Platform.isFxApplicationThread();

		if (loginPassword != null)
			System.out.println("Request requestFocusInWindow");
			//loginPassword.requestFocusInWindow();
	}

	public void setProcess(String process) {
		assert SwingUtilities.isEventDispatchThread();
		//assert Platform.isFxApplicationThread();

		this.process = process;
		long now = System.currentTimeMillis();
		Long runningTime = timeCount.get(process);
		if (process != null)
			timeCount.put(process, (runningTime != null ? runningTime.longValue() : 0) + now - lastSwitchTime);
		this.lastSwitchTime = now;
		if (trayIcon != null)
			if (process != null)
				trayIcon.setToolTip("Using " + process);
			else
				trayIcon.setToolTip(null);
	}

	public String getUsername() {
		return this.username;
	}
	public String getLoginUsername() {
		return this.loginUsername;
	}

	private String token;
	public void setToken(String token){this.token = token;}

	public void setDataColSendIntvl(final int sendIntvl,final int dataCollectInvl ){
		this.dataSendInvl = dataCollectInvl ;
		this.dataCollectInvl = sendIntvl;
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
}
