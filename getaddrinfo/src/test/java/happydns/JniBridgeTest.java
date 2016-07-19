package happydns;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.FileSystems;
import java.nio.file.Paths;

/**
 * Created by bailong on 16/7/19.
 */
public class JniBridgeTest {

    @BeforeClass
    public static void loadLibrary(){
        try {
            String workingdirectory = System.getProperty("user.dir");
            String os = System.getProperty("os.name");
            String extend = ".so";
            if (os.startsWith("Mac")){
                extend = ".dylib";
            }
            System.load(workingdirectory + "/build/libs/bridge/shared/debug/libbridge" + extend);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void notsetCallback(){
        boolean x = JniBridge.instance().test();
        Assert.assertTrue(x);
    }
}
