package com.ModelTest;

import com.fxApplication.Model;
import com.sun.javafx.application.PlatformImpl;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ModelTest {
    public Path configPath = Paths.get(this.getClass().getResource("/testConfig.json").getPath());

    @BeforeAll
    public static void Statup(){
        PlatformImpl.startup(()->{});
    }

    @AfterAll
    public static void teardown(){
        System.out.println("Stopping!");
    }

    @Test
    public void testSettings() {
        Assertions.assertNotNull(configPath);
        Model tester = new Model(configPath);
        JSONObject settings = tester.getUserSettingsJSON();
        Assertions.assertTrue(settings.get("token") instanceof String);
        Assertions.assertNotNull(settings.get("token"));
    }

    @Test
    public void testDbInit(){
        Assertions.assertNotNull(configPath);
        Model testerModel = new Model(configPath);
        Assertions.assertTrue(testerModel.dBIntialized(), "DB not intialized");
    }

    @Test
    public void sss(){
        Assertions.assertFalse(false);
    }

}

