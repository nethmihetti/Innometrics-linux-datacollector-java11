package App.nativeimpl;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.unix.X11;
import App.model.Model;
import javafx.application.Platform;
import jnacontrib.x11.api.X;
import jnacontrib.x11.api.X.X11Exception;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
		//m.setProcess(getActiveWindowApplication());
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
							if (nowProcess != currentProcess) {
								currentProcess = nowProcess;
								final String title = getActiveWindowTitle();
								final String process = getActiveWindowApplication();
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										System.out.println("SWITCHED TO " + title);
										System.out.println("PROCESS NAME : " + process);
										m.setWindowName(process);
										//m.setProcess(process);
									}
								});
							}
						}
						break;
				}
			}
		} catch (X11Exception e) {
			throw new RuntimeException(e);
		}
	}
}
