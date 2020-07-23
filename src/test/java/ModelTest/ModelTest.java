package ModelTest;

import App.model.Model;
import com.sun.javafx.application.PlatformImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.runners.Parameterized.*;

@RunWith(Parameterized.class)
public class ModelTest {

    @Before
    public void Statup(){
        PlatformImpl.startup(()->{});
    }

    // fields used together with @Parameter must be public
    @Parameter(0)
    public Boolean result;
    @Parameter(1)
    public int m1;
    @Parameter(2)
    public int m2;


    // creates the test data
    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] { { false , 2 , 2 }, { false, 1, 10 }, { false, -1, 5 } , { true, 20, 1 } };
        return Arrays.asList(data);
    }

    @Test
    public void testSettings() {
        Model tester = new Model("/testConfig.json");
        tester.setDataColSendIntvl(m1,m2);
        assertEquals("Result", result, tester.validSettings(tester.getDataSendInvl(), tester.getDataCollectInvl()));
    }
}

