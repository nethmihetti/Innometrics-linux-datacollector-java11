import App.LoginPage;
import App.model.Model;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;


public class blackBox {
    @Test
    public void test1() throws IOException {
        LoginPage tt = new LoginPage();
        Model aa = new Model("/config.json");
        String a = "hh";
        Assert.assertEquals("hh",a);
    }
}
