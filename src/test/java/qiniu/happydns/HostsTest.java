package qiniu.happydns;

import org.junit.Assert;
import org.junit.Test;
import qiniu.happydns.local.Hosts;

import java.io.IOException;

/**
 * Created by bailong on 15/6/18.
 */
public class HostsTest {
    @Test
    public void testQuery() throws IOException {
        Hosts hosts = new Hosts();
        hosts.put("hello.qiniu.com", "1.1.1.1");
        hosts.put("hello.qiniu.com", "2.2.2.2");
        hosts.put("qiniu.com", "3.3.3.3");
        String[] r = hosts.query("hello.qiniu.com");
        Assert.assertEquals(2, r.length);
        Assert.assertTrue(r[0].equals("2.2.2.2"));
        Assert.assertTrue(r[1].equals("1.1.1.1"));

        String[] r2 = hosts.query("hello.qiniu.com");
        Assert.assertEquals(2, r2.length);
        Assert.assertTrue(r2[1].equals("2.2.2.2"));
        Assert.assertTrue(r2[0].equals("1.1.1.1"));
    }

    @Test
    public void testQuery2() throws IOException {
        Hosts hosts = new Hosts();
        hosts.put("hello.qiniu.com", "1.1.1.1");
        hosts.put("hello.qiniu.com", "2.2.2.2");

        hosts.put("qiniu.com", "4.4.4.4");

        String[] r = hosts.query("qiniu.com");
        Assert.assertEquals(1, r.length);
        Assert.assertEquals("4.4.4.4", r[0]);
    }
}
