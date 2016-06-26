package qiniu.happydns;

import org.junit.Assert;
import org.junit.Test;
import qiniu.happydns.http.IHosts;
import qiniu.happydns.local.HijackingDetectWrapper;
import qiniu.happydns.local.Hosts;
import qiniu.happydns.local.Resolver;
import qiniu.happydns.local.SystemDnsServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by bailong on 15/6/21.
 */
public class DnsTest {
    private boolean flag = false;

    @Test
    public void testDns() throws IOException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = SystemDnsServer.defaultResolver();
        resolvers[1] = new Resolver(InetAddress.getByName("119.29.29.29"));
        DnsClient dns = new DnsClient(resolvers);
        String[] ips = dns.query("www.qiniu.com");
        Assert.assertNotNull(ips);
        Assert.assertTrue(ips.length > 0);
    }

    @Test
    public void testTtl() throws IOException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = new HijackingDetectWrapper(
                new Resolver(InetAddress.getByName(SystemDnsServer.defaultServer()[0])));
        resolvers[1] = new HijackingDetectWrapper(
                new Resolver(InetAddress.getByName("119.29.29.29")));

        IHosts h = new Hosts();
        h.put("hello.qiniu.com", "1.1.1.1");
        h.put("hello.qiniu.com", "2.2.2.2");
        h.put("qiniu.com", "3.3.3.3");

        DnsClient dns = new DnsClient(resolvers, h);

        Domain d = new Domain("qiniu.com", false, false, 10);
        String[] r = dns.query(d);
        Assert.assertEquals(1, r.length);
        Assert.assertEquals("3.3.3.3", r[0]);

        d = new Domain("qiniu.com", false, false, 1000);
        r = dns.query(d);
        Assert.assertEquals(1, r.length);
        Assert.assertTrue(!"3.3.3.3".equals(r[0]));
    }

    public void testCname() throws IOException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = new HijackingDetectWrapper(
                new Resolver(InetAddress.getByName(SystemDnsServer.defaultServer()[0])));
        resolvers[1] = new HijackingDetectWrapper(
                new Resolver(InetAddress.getByName("114.114.115.115")));

        IHosts h = new Hosts();
        h.put("hello.qiniu.com", "1.1.1.1");
        h.put("hello.qiniu.com", "2.2.2.2");
        h.put("qiniu.com", "3.3.3.3");

        DnsClient dns = new DnsClient(resolvers, h);

        Domain d = new Domain("qiniu.com", true);
        String[] r = dns.query(d);
        Assert.assertEquals(1, r.length);
        Assert.assertEquals("3.3.3.3", r[0]);

        d = new Domain("qiniu.com", false);
        r = dns.query(d);
        Assert.assertEquals(1, r.length);
        Assert.assertTrue(!"3.3.3.3".equals(r[0]));
    }

    public void testNull() throws UnknownHostException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = SystemDnsServer.defaultResolver();
        DnsClient dns = new DnsClient(resolvers);
        IOException e = null;
        try {
            dns.query((String) null);
        } catch (IOException ex) {
            ex.printStackTrace();
            e = ex;
        }

        Assert.assertNotNull(e);
        e = null;
        try {
            dns.query((Domain) null);
        } catch (IOException ex) {
            ex.printStackTrace();
            e = ex;
        }

        Assert.assertNotNull(e);
        e = null;
        try {
            dns.query("");
        } catch (IOException ex) {
            ex.printStackTrace();
            e = ex;
        }
        Assert.assertNotNull(e);
    }

    public void testIp() throws IOException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = SystemDnsServer.defaultResolver();
        DnsClient dns = new DnsClient(resolvers);
        String[] ips = dns.query("1.1.1.1");
        Assert.assertEquals(ips.length, 1);
        Assert.assertEquals(ips[0], "1.1.1.1");
    }
}
