package App.model;

import App.Activity;
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
import org.jnativehook.GlobalScreen;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;


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
	private Activity currentActivity = null;
	public Connection conn = null;
	public PreparedStatement insetStmt = null;
	ArrayList <Activity> listActivities = new ArrayList<Activity>();
	Queue <Activity> activitiesQueue = new LinkedList<>();

	//constructor
	public Model(String settingsFile) {
		settings = new SettingsPersister(settingsFile);
		this.windowName.setPrefWidth(200);
		this.windowName.setWrapText(true);
		this.windowName.setAlignment(Pos.CENTER);
		this.loginUsername = "";
		timeCount = new HashMap<String, Long>();
		initDatabase();
	}

	public void setWindowName(final String newName){
		windowName.setText(newName);
	}
	public Label getWindowName() {
		return windowName;
	}

	/**
	 * Start all the background threads (active window listener and data saving to local db and data post to remote server)
	 */
	public void beginWatching() {
		assert Platform.isFxApplicationThread(); //make sure its the main thread
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.ALL);
		//watchStopper = ActiveWindowInfo.INSTANCE.startListening(this);
		System.out.println("Starting watch thread!!");
		ActiveWindowInfo.INSTANCE.startListening(this); //Active window capture thread
		startPostActivitiesToDB(); //Post to local SQLlite local db thread
		StartPostingActivities(); //Post to remote DB thread
	}
	public void endWatching(boolean cleanup) throws IOException {
		if (cleanup) {
			settings.cleanup(); //Reset the settings (delete settings)
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
	public void saveUsername(final TextField UsernameField) {
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
	public JSONObject getUserSettingsJSON(){
		return settings.getAllSettingsJson();
	}

	public void setTrayIcon(TrayIcon trayIcon) {
		this.trayIcon = trayIcon;
	} //todo: Implement trayIcon
	public void setAddActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}
	public void setActivityEndTime() throws JSONException {
		if (this.currentActivity != null){
			Clock clock = Clock.systemDefaultZone();
			Instant t = clock.instant();
			this.currentActivity.setEndTime(t.toString());
			this.activitiesQueue.add(this.currentActivity);
		}
	}

	/**
	 * Initialize the local database buy connecting to local or creating a new instance if not already existing
	 */
	public void initDatabase() {
		assert Platform.isFxApplicationThread();
		String dbpath = "src/main/resources/userdb.db";
		File f = new File(dbpath);

		if(f.exists() && !f.isDirectory()){
			this.conn = ConnectToDB(dbpath);
		}else{
			System.out.println("No existing Database");
			this.conn = ConnectToDB(dbpath);
			createTable(conn);
		}
		try {
			insetStmt = this.conn.prepareStatement("INSERT INTO Activities(activityType, browser_title, browser_url, end_time, executable_name, idle_activity, ip_address, mac_address, osversion," +
					" pid, start_time, userID) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		}
		catch (SQLException ex){
			ex.printStackTrace();
		}
	}

	private void createTable(Connection conn){
		try{
			String sql = "CREATE TABLE IF NOT EXISTS Activities (\n"
					+ " activityID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
					+ " activityType text,\n"
					+ " browser_title text,\n"
					+ " browser_url text,\n"
					+ " end_time text,\n"
					+ " executable_name text,\n"
					+ " idle_activity text,\n"
					+ " ip_address text,\n"
					+ " mac_address text,\n"
					+ " osversion text,\n"
					+ " pid text,\n"
					+ " start_time text,\n"
					+ " userID text\n"
					+ ");";

			Statement createStmt = conn.createStatement();
			createStmt.execute(sql);
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

	}

	private Connection ConnectToDB(String dbPath) {
		//Registering the Driver
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:"+dbPath);
		} catch (ClassNotFoundException | SQLException eString) {
			System.err.println("Could not init JDBC driver - driver not found");
		}
		System.out.println("Connection established......");
		return conn;
	}

	/**
	 * This method adds a activity to local database
	 */
	public void addActivitiesToDb() {
		while(!activitiesQueue.isEmpty()) {
			Activity tempActivity = activitiesQueue.remove();
			if (this.conn != null && this.insetStmt != null) {
				try {
					JSONObject activityJson = tempActivity.toJson();
					insetStmt.setString(1, (String) activityJson.get("activityType"));
					insetStmt.setString(2, (String) activityJson.get("browser_title"));
					insetStmt.setString(3, (String) activityJson.get("browser_url"));
					insetStmt.setString(4, (String) activityJson.get("end_time"));

					insetStmt.setString(5, (String) activityJson.get("executable_name"));
					insetStmt.setString(6, (String) activityJson.get("idle_activity"));
					insetStmt.setString(7, (String) activityJson.get("ip_address"));
					insetStmt.setString(8, (String) activityJson.get("mac_address"));

					insetStmt.setString(9, (String) activityJson.get("osversion"));
					insetStmt.setString(10, (String) activityJson.get("pid"));
					insetStmt.setString(11, (String) activityJson.get("start_time"));
					insetStmt.setString(12, (String) activityJson.get("userID"));
					insetStmt.executeUpdate();

				} catch (SQLException | JSONException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private void startPostActivitiesToDB(){
		Runnable task = new Runnable() {
			@Override
			public void run() {
				final AtomicBoolean stop = new AtomicBoolean(false);
				while(!stop.get()){
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							//Post data to local DB
							addActivitiesToDb();
						}
					});
					try {
						Thread.sleep(60000); //1 min
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		// Run the task in a background thread
		Thread backgroundThread = new Thread(task);
		// Terminate the running thread if the application exits
		backgroundThread.setDaemon(true);
		// Start the thread
		backgroundThread.start();
	}

	/**
	 * This method adds a process to local database
	 */
	public void addProcessToDb(){

	}

	/**
	 * This method periodically (thread running in background) a posts processes collected data to remote database
	 */
	public void postProcesses(){

	}

	/**
	 * This method periodically (thread running in background) a posts activities from local database to remote database
	 */
	public void StartPostingActivities(){
		assert Platform.isFxApplicationThread();
		Runnable task = new Runnable() {
			@Override
			public void run() {
				final AtomicBoolean stop = new AtomicBoolean(false);
				while(!stop.get()){
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							//Post data to remote db
							try {
								sendData();
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
					try {
						Thread.sleep(120000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		Thread backgroundThread = new Thread(task);
		backgroundThread.setDaemon(true);
		backgroundThread.start();
	}

	/**
	 * read data from local database and send it to remote database
	 * @throws JSONException
	 */
	public void sendData() throws JSONException {
		String token = settings.get("token");
		String url = "http://10.90.138.244:9091/V1/activity"; //for dev server
		JSONArray result = new JSONArray();

		//read from bd and set field sent to true
		Statement stmt = null;
		try {
			stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM activities;" );

			while ( rs.next() ) {
				JSONObject temp = new JSONObject();
				String activityID = rs.getString("activityID");
				temp.put("activityID",activityID);
				String activityType = rs.getString("activityType");
				temp.put("activityType",activityType);
				String browser_title=  rs.getString("browser_title");
				temp.put("browser_title",browser_title);
				String browser_url = rs.getString("browser_url");
				temp.put("browser_url",browser_url);
				String end_time = rs.getString("end_time");
				temp.put("end_time",end_time);

				String executable_name = rs.getString("executable_name");
				temp.put("executable_name",executable_name);
				String idle_activity = rs.getString("idle_activity");
				temp.put("idle_activity",idle_activity);
				String ip_address = rs.getString("ip_address");
				temp.put("ip_address",ip_address);
				String mac_address = rs.getString("mac_address");
				temp.put("mac_address",mac_address);

				String osversion = rs.getString("osversion");
				temp.put("osversion",osversion);
				String pid =rs.getString("pid");
				temp.put("pid",pid);
				String start_time = rs.getString("start_time");
				temp.put("start_time",start_time);
				String userID = rs.getString("userID");
				temp.put("userID",userID);

				result.add(temp);

				//rs.update

			}
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
		System.out.println("Operation done successfully");
		if(result.isEmpty()){
			return;
		}

		JSONObject body = new JSONObject();

		body.put("activities",result);
		HttpClient client = HttpClient.newBuilder().build();

		HttpRequest request = HttpRequest.newBuilder()
				.header("Content-Type", "application/json")
				.header("accept", "application/json")
				.header("Token", token)
				.uri(URI.create(url))
				.POST(HttpRequest.BodyPublishers.ofString(body.toString()))
				.build();

		try {
			HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("Send data Status code : "+response.statusCode());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}

}
