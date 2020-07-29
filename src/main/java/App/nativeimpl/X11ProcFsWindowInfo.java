package App.nativeimpl;

import App.Activity;
import App.SystemProcess;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.unix.X11;
import App.model.Model;
import javafx.application.Platform;
import jnacontrib.x11.api.X;
import jnacontrib.x11.api.X.X11Exception;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class X11ProcFsWindowInfo extends ActiveWindowInfo {
	private interface Handle extends Library {
		Handle module = (Handle) Native.load("c", Handle.class);

		int readlink(String path, byte[] buffer, int size);
	}

	private static String readlink(String path) throws FileNotFoundException {
		byte[] buffer = new byte[300];
		int size = Handle.module.readlink(path, buffer, 300);
		if (size > 0)
			return new String(buffer, 0, size);
		else
			throw new FileNotFoundException(path);
	}

	private X.Display display;

	public String getActiveWindowProcess() {
		try {
			return readlink("/proc/" + display.getActiveWindow().getPID() + "/exe");
		} catch (FileNotFoundException | X11Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getActiveWindowCommand() {
		try {
			return new String(Files.readAllBytes(Paths.get("/proc/" + display.getActiveWindow().getPID() + "/cmdline"))).replaceAll("\0", " ");
		} catch (IOException | X11Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void getActivity(String pid, String title, String process, final Model m){
		try{
			String[] args = new String[] {"/bin/bash", "-c", "ps -o user= -o %mem= -o %cpu= -o stat= -o command= -p "+pid };
			Process proc = new ProcessBuilder(args).start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String line = reader.readLine();
			if (line != null){
				String[] a = line.split("\\s+");
				Clock clock = Clock.systemDefaultZone();
				Instant instant = clock.instant();
				HashMap hashMap = new HashMap();
				hashMap.put("start_time",instant.toString());
				hashMap.put("activityType",a[0]);
				hashMap.put("idle_activity",a[3]);
				hashMap.put("executable_name",process);
				hashMap.put("browser_title",title);
				hashMap.put("pid",pid);

				Activity currentActivity = new Activity();
				currentActivity.setActivityValues(m,hashMap);
				m.setAddActivity(currentActivity);
			}
			proc.waitFor();

		}catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getActiveWindowTitle() {
		try {
			return display.getActiveWindow().getTitle();
		} catch (X11Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void startListening(Model m){
		Runnable task = new Runnable() {
			@Override
			public void run() {
				runTask(m);
			}
		};
		// Run the task in a background thread
		Thread backgroundThread = new Thread(task);
		// Terminate the running thread if the application exits
		backgroundThread.setDaemon(true);
		// Start the thread
		backgroundThread.start();
	}
	public void runTask(Model m){
		display = new X.Display();
		final AtomicBoolean stop = new AtomicBoolean(false);

		X11.XEvent event = new X11.XEvent();
		display.getRootWindow().selectInput(X11.PropertyChangeMask);
		//TODO: http://www.linuxquestions.org/questions/showthread.php?p=2431345#post2431345
		try {
			int currentProcess = 0;
			while (!stop.get()) {
				display.getRootWindow().nextEvent(event);
				//handle the union type
				event.setType(X11.XPropertyEvent.class);
				event.read();
				switch (event.type) {
					case X11.PropertyNotify:
						if (X11.INSTANCE.XGetAtomName(display.getX11Display(), event.xproperty.atom).equals("_NET_ACTIVE_WINDOW") && display.getActiveWindow().getID() != 0) {
							int nowProcess = display.getActiveWindow().getPID().intValue();
							if (nowProcess != currentProcess ) {
								currentProcess = nowProcess;
								final String title = getActiveWindowTitle();
								final String process = getActiveWindowApplication();
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										getActivity(String.valueOf(nowProcess),title,process,m);
										System.out.println("TITLE: " + title);
										m.setWindowName(process);
									}
								});
								m.setActivityEndTime();
							}
						}
						break;
				}
			}
		} catch (X11Exception | JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public void startWatchigProcesses(Model m){
		try{
			String[] args = new String[] {"/bin/bash", "-c", "ps -aux --no-header"};
			Process proc = new ProcessBuilder(args).start();

			Clock clock = Clock.systemDefaultZone();
			Instant instant = clock.instant();

			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;

			while((line = reader.readLine())!= null ){
				String[] processLine = line.split("\\s+");
				HashMap hashMap = new HashMap();
				hashMap.put("start_time",instant.toString());
				SystemProcess tempProc = new SystemProcess();
			}
			proc.waitFor();

		}catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
