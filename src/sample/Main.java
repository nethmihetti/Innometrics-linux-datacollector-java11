package sample;

import javafx.application.Application;
import javafx.stage.Stage;

import java.net.*;
import java.util.Enumeration;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) {
    }


    public static void main(String[] args) throws SocketException {
        System.out.println("Starting....");




        System.out.println(System.getProperty("os.name") + " : " +System.getProperty("os.version"));
        launch(Login.class, args);
    }
}
