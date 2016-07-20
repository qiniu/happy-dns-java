package happydns;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import qiniu.happydns.DnsClient;
import qiniu.happydns.IResolver;
import qiniu.happydns.http.DnspodFree;
import qiniu.happydns.local.Resolver;
import qiniu.happydns.local.SystemDnsServer;

/**
 * Created by bailong on 16/7/19.
 */
public class JniBridgeTest {

    @BeforeClass
    public static void loadLibrary() {
        try {
            String workingdirectory = System.getProperty("user.dir");
            String os = System.getProperty("os.name");
            String extend = ".so";
            if (os.startsWith("Mac")) {
                extend = ".dylib";
            }
            System.load(workingdirectory + "/build/libs/bridge/shared/debug/libbridge" + extend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void customDnsCallback() {
        int x = JniBridge.instance().test();
        Assert.assertEquals(0, x);
        System.out.printf("not set result " + x);
        DnsClient d = new DnsClient(new IResolver[]{SystemDnsServer.defaultResolver()});
        JniBridge.instance().setDnsClient(d);
        x = JniBridge.instance().test();
        System.out.printf("custom result " + x);
        Assert.assertEquals(0, x);
    }
}
