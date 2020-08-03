package App.model;

import App.*;
import App.nativeimpl.ActiveWindowInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class Model {

	private final SettingsPersister settings;
	private TrayIcon trayIcon;
	public volatile Label windowName = new Label("Application");
	private String loginUsername, username;
	private PasswordField loginPassword;
	private String token;
	public static String currentIP, currentMAC, currentOS;
	private Activity currentActivity = null;
	private Connection conn = null;
	private PreparedStatement insetStmt, ProcsinsetStmt = null;
	Queue <Activity> activitiesQueue = new LinkedList<>();
	Queue <SystemProcess> processesQueue = new LinkedList<>();

	//constructor
	public Model(String settingsFile) {
		settings = new SettingsPersister(settingsFile);
		this.windowName.setPrefWidth(200);
		this.windowName.setWrapText(true);
		this.windowName.setAlignment(Pos.CENTER);
		this.loginUsername = "";
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
		ActiveWindowInfo.INSTANCE.startListening(this); //Active window capture thread
		startPostActivitiesToDB(); //Post to local SQLlite local db thread
		StartPostingActivities(); //Post activities to remote DB thread

		startWatchingProcesses(); //capture ps thread
		StartpostProcessesToDb(); //Post ps results to SQLlite DB (local) thread
		//TODO: Post Processes to remote DB (idea : to post together with activities)
	}
	public void endWatching(boolean cleanup) throws IOException {
		if (cleanup) {
			settings.cleanup(); //Reset the settings (delete settings)
		}
		System.out.println("Stopping watch thread!!");
	}
	public void shutdown(){
		System.out.println("Collector is shutting down!");
		try{
			cleanDb();
			this.conn.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		Platform.exit();
	}

	public void flipToMainPage(Stage window) throws SocketException {
		assert Platform.isFxApplicationThread();

		MainPage mainPage = new MainPage();
		this.currentIP = MainPage.getLocalIP();
		this.currentMAC = MainPage.getLocalMac();
		this.currentOS = MainPage.getLocalOSVersion();

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
			ZonedDateTime t = clock.instant().atZone(ZoneId.systemDefault());
			this.currentActivity.setEndTime(t.toLocalDateTime().toString());
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
					" pid, start_time, userID, posted) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ProcsinsetStmt = this.conn.prepareStatement("INSERT INTO processesReports(collectedTime, ip_address, mac_address, alternativeLabelCpu, capturedDateCpu, measurementTypeIdCpu, valueCpu, alternativeLabelMem, capturedDateMem, measurementTypeIdMem, valueMem, osversion," +
					" pid, userID, posted) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
					+ " userID text,\n"
					+ " posted INTEGER\n"
					+ ");";

			Statement createStmt = conn.createStatement();
			createStmt.execute(sql);

			String processesTable = "CREATE TABLE IF NOT EXISTS processesReports (\n"
					+ " ProcID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
					+ " collectedTime text,\n"
					+ " ip_address text,\n"
					+ " mac_address text,\n"
					+ " alternativeLabelCpu text,\n"
					+ " capturedDateCpu text,\n"
					+ " measurementTypeIdCpu text,\n"
					+ " valueCpu text,\n"
					+ " alternativeLabelMem text,\n"
					+ " capturedDateMem text,\n"
					+ " measurementTypeIdMem text,\n"
					+ " valueMem text,\n"
					+ " osversion text,\n"
					+ " pid text,\n"
					+ " userID text,\n"
					+ " posted INTEGER\n"
					+ ");";

			Statement createProcessTableStmt = conn.createStatement();
			createProcessTableStmt.execute(processesTable);
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
					insetStmt.setInt(13, 0);
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
					try {
						Thread.sleep(30000); //1 min
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					addActivitiesToDb();
				}
			}
		};
		Thread backgroundThread = new Thread(task);
		backgroundThread.setDaemon(true);
		backgroundThread.start();
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
					try {
						sendData();
						Thread.sleep(120000);
					} catch (InterruptedException | JSONException e) {
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
	 * read data from local database and send it to remote database (activities)
	 * @throws JSONException
	 */
	public void sendData() throws JSONException {
		System.out.println("Starting to Post");
		String token = settings.get("token");
		String ActivityPosturl = "http://10.90.138.244:9091/V1/activity"; //for dev server
		String ProcessPosturl = "http://10.90.138.244:9091/V1/process";
		JSONArray result = new JSONArray();

		//read from bd and set field sent to true
		Statement stmt = null;
		List <Integer>toUpdateIDs = new ArrayList<>();
		try {
			stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM activities WHERE posted = 0;" );

			while ( rs.next() ) {
				JSONObject temp = new JSONObject();
				int activityID = rs.getInt("activityID");
				temp.put("activityID",String.valueOf(activityID));
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
				toUpdateIDs.add(activityID);
			}
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}

		if(!result.isEmpty()){
			System.out.println("Posting Activities!!");
			JSONObject body = new JSONObject();

			body.put("activities",result);
			HttpClient client = HttpClient.newBuilder().build();

			HttpRequest request = HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.header("accept", "application/json")
					.header("Token", token)
					.uri(URI.create(ActivityPosturl))
					.POST(HttpRequest.BodyPublishers.ofString(body.toString()))
					.build();

			try {
				HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				System.out.println("Send data Status code : "+response.statusCode());
				if (response.statusCode() == 200){
					//update posted field to 1 -> sent
					String updateids = "("+ toUpdateIDs.stream().map(Object::toString)
							.collect(Collectors.joining(", ")) + ");";
					System.out.println(updateids);
					Statement updateStmt = this.conn.createStatement();
					String updateQuery = "UPDATE activities set posted=1 WHERE activityID IN "+updateids;
					updateStmt.executeUpdate(updateQuery);
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}


		//Post processes
		List <Integer> ProcUpdateIDs = new ArrayList<>();
		JSONArray resultPr = new JSONArray();
		Statement procSelectstmt = null;
		try{
			procSelectstmt = this.conn.createStatement();
			ResultSet rs = procSelectstmt.executeQuery( "SELECT * FROM processesReports WHERE posted = 0;" );

			while ( rs.next() ) {
				JSONObject temp = new JSONObject();

				String collectedTime = rs.getString("collectedTime");
				temp.put("collectedTime",collectedTime);
				String ip_address = rs.getString("ip_address");
				temp.put("ip_address",ip_address);
				String mac_address = rs.getString("mac_address");
				temp.put("mac_address",mac_address);

				JSONArray measurements = new JSONArray();

				JSONObject cpuObj = new JSONObject();
				String alternativeLabelCpu = rs.getString("alternativeLabelCpu");
				cpuObj.put("alternativeLabel",alternativeLabelCpu);
				String capturedDateCpu = rs.getString("capturedDateCpu");
				cpuObj.put("capturedDate",capturedDateCpu);
				String measurementTypeIdCpu = rs.getString("measurementTypeIdCpu");
				cpuObj.put("measurementTypeId",measurementTypeIdCpu);
				String valueCpu = rs.getString("valueCpu");
				cpuObj.put("value",valueCpu);
				measurements.add(cpuObj);

				JSONObject memObj = new JSONObject();
				String alternativeLabelMem = rs.getString("alternativeLabelMem");
				memObj.put("alternativeLabel",alternativeLabelCpu);
				String capturedDateMem = rs.getString("capturedDateMem");
				memObj.put("capturedDate",capturedDateMem);
				String measurementTypeIdMem = rs.getString("measurementTypeIdCpu");
				memObj.put("measurementTypeId",measurementTypeIdMem);
				String valueMem = rs.getString("valueCpu");
				memObj.put("value",valueMem);
				measurements.add(memObj);

				temp.put("measurementsReportList",measurements);

				String osversion = rs.getString("osversion");
				temp.put("osversion",osversion);
				String pid =rs.getString("pid");
				temp.put("pid",pid);
				String userID = rs.getString("userID");
				temp.put("userID",userID);

				int ProcID = rs.getInt("ProcID");
				ProcUpdateIDs.add(ProcID);
				resultPr.add(temp);
			}

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		if(!resultPr.isEmpty()){
			System.out.println("posting PS");
			JSONObject ProcBody = new JSONObject();


			ProcBody.put("processesReport",resultPr);
			HttpClient client2 = HttpClient.newBuilder().build();
			//System.out.println(ProcBody.toString());

			HttpRequest request2 = HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.header("accept", "application/json")
					.header("Token", token)
					.uri(URI.create(ProcessPosturl))
					.POST(HttpRequest.BodyPublishers.ofString(ProcBody.toString()))
					.build();

			try {
				HttpResponse<?> response = client2.send(request2, HttpResponse.BodyHandlers.ofString());
				System.out.println("PS Send data Status code : "+response.statusCode());
				if (response.statusCode() == 200){
					//update posted field to 1 -> sent
					String updateids = "("+ ProcUpdateIDs.stream().map(Object::toString)
							.collect(Collectors.joining(", ")) + ");";
					System.out.println("PS IDS : "+updateids);
					Statement updateStmt = this.conn.createStatement();
					String updateQuery = "UPDATE processesReports set posted=1 WHERE ProcID IN "+updateids;
					updateStmt.executeUpdate(updateQuery);
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * This method adds a process to local database
	 */
	public void addProcessesToDb(){
		while(!this.processesQueue.isEmpty()) {
			SystemProcess tempProcess = this.processesQueue.remove();
			if (this.conn != null && this.ProcsinsetStmt != null) {
				try {

					JSONObject processJson = tempProcess.toJson();
					ProcsinsetStmt.setString(1, (String) processJson.get("collectedTime"));
					ProcsinsetStmt.setString(2, (String) processJson.get("ip_address"));
					ProcsinsetStmt.setString(3, (String) processJson.get("mac_address"));

					ProcsinsetStmt.setString(4, (String) processJson.get("alternativeLabelCpu"));
					ProcsinsetStmt.setString(5, (String) processJson.get("capturedDateCpu"));
					ProcsinsetStmt.setString(6, (String) processJson.get("measurementTypeIdCpu"));
					ProcsinsetStmt.setString(7, (String) processJson.get("valueCpu"));

					ProcsinsetStmt.setString(8, (String) processJson.get("alternativeLabelMem"));
					ProcsinsetStmt.setString(9, (String) processJson.get("capturedDateMem"));
					ProcsinsetStmt.setString(10, (String) processJson.get("measurementTypeIdMem"));
					ProcsinsetStmt.setString(11, (String) processJson.get("valueMem"));

					ProcsinsetStmt.setString(12, (String) processJson.get("osversion"));
					ProcsinsetStmt.setString(13, (String) processJson.get("pid"));
					ProcsinsetStmt.setString(14, (String) processJson.get("userID"));
					ProcsinsetStmt.setInt(15, 0);
					ProcsinsetStmt.executeUpdate();

				} catch (SQLException | JSONException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method periodically (thread running in background) a posts processes collected data to remote database
	 */
	public void StartpostProcessesToDb(){
		Task <Void> task = new Task() {
			@Override
			public Void call() {
				final AtomicBoolean stop = new AtomicBoolean(false);
				while(!stop.get()){
					try {
						Thread.sleep(30000); //1 min
						addProcessesToDb();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		};

		Thread processtoDb = new Thread(task);
		processtoDb.setDaemon(true);
		processtoDb.start();
	}

	public void WatchProcesses(){
		assert !Platform.isFxApplicationThread();
		try{
			String[] args = new String[] {"/bin/bash", "-c", "ps -aux --no-header"};
			Process proc = new ProcessBuilder(args).start();

			Clock clock = Clock.systemDefaultZone();
			ZonedDateTime t = clock.instant().atZone(ZoneId.systemDefault());
			String captureTime = t.toLocalDateTime().toString();
			System.out.println("Capture Date : "+captureTime);

			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;

			while((line = reader.readLine())!= null ){
				String[] processLine = line.split("\\s+");
				String pid = processLine[1];
				SystemProcess tempProc = new SystemProcess();

				Map <String, JSONObject> measurements = new HashMap<>();

				JSONObject cpu = new JSONObject();
				cpu.put("alternativeLabel", "CPU%");
				cpu.put("capturedDate", captureTime);
				cpu.put("measurementTypeId", "5");
				cpu.put("value", processLine[2]);
				measurements.put("Cpu",cpu);

				JSONObject mem = new JSONObject();
				mem.put("alternativeLabel", "MEM%");
				mem.put("capturedDate", captureTime);
				mem.put("measurementTypeId", "3");
				mem.put("value", processLine[3]);
				measurements.put("Mem",mem);

				tempProc.setProcessValues(this, measurements, captureTime, pid);
				this.processesQueue.add(tempProc);
			}
			proc.waitFor();

		}catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void startWatchingProcesses(){
		Runnable task = new Runnable() {
			@Override
			public void run() {
				final AtomicBoolean stop = new AtomicBoolean(false);
				while(!stop.get()){
					try {
						WatchProcesses();
						Thread.sleep(30000); //1/2 min
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

	private void cleanDb(){
		if (this.conn != null){
			try {
				//clear the activities table
				Statement deleteStmt = this.conn.createStatement();
				String deleteQuery = "DELETE FROM activities WHERE posted = 1;";
				deleteStmt.executeUpdate(deleteQuery);

				//clear the processes table
				Statement deleteStmtprocs = this.conn.createStatement();
				String deleteQueryProcs = "DELETE FROM processesReports WHERE posted = 1;";
				deleteStmtprocs.executeUpdate(deleteQueryProcs);

			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		}
	}

}
