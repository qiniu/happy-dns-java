package qiniu.happydns;

import org.junit.Assert;
import org.junit.Test;
import qiniu.happydns.local.SystemDnsServer;

/**
 * Created by bailong on 16/6/25.
 */
public class SystemDnsServerTest {
    @Test
    public void testSame() {
        String[] jndi = SystemDnsServer.getByJNDI();
        String[] api = SystemDnsServer.getByInternalAPI();
        String[] conf = SystemDnsServer.getByUnixConf();
        Assert.assertNotNull(jndi);
        Assert.assertArrayEquals(jndi, api);
        Assert.assertArrayEquals(api, conf);
    }
}
