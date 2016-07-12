package qiniu.happydns;

import org.junit.Assert;
import org.junit.Test;
import qiniu.happydns.local.Resolver;
import qiniu.happydns.local.SystemDnsServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by bailong on 15/6/17.
 */
public class LocalResolverTest {

    private void template(String ip) throws UnknownHostException {
        Resolver resolver = new Resolver(InetAddress.getByName(ip));
        template(resolver);
    }

    private void template(IResolver resolver) {
        try {
            Record[] records = resolver.resolve(new Domain("baidu.com"));
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length > 0);
            records = resolver.resolve(new Domain("www.qiniu.com"));
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length >= 3);

            for (Record r : records) {
                Assert.assertTrue(r.value, r.ttl >= 600);
                Assert.assertTrue(r.value, r.isA() || r.isCname());
                Assert.assertFalse(r.value, r.isExpired());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testLocal() throws UnknownHostException {
        template(SystemDnsServer.defaultResolver());
    }

    //    http://www.alidns.com/
//    public void testAli() throws UnknownHostException {
//        template("223.5.5.5");
//    }

    //    https://www.114dns.com/

    @Test
    public void test114() throws UnknownHostException {
        template("114.114.115.115");
    }

    //    http://dudns.baidu.com/
//    public void testBaidu() throws UnknownHostException {
//        template("180.76.76.76");
//    }

    //    http://www.sdns.cn/ cnnic
//    public void testCnnic() throws UnknownHostException {
//        template("1.2.4.8");
//    }

    @Test
    public void testGoogle() throws UnknownHostException {
        template("8.8.4.4");
    }

    @Test
    public void testDnspod() throws UnknownHostException {
        template("119.29.29.29");
    }

    //    http://www.dnspai.com/ 360dns
//    public void testDnspai() throws UnknownHostException {
//        template("101.226.4.6");
//    }
    @Test
    public void testTimeout() throws UnknownHostException {
        Resolver resolver = new Resolver(InetAddress.getByName("8.1.1.1"), 5);
        try {
            Record[] records = resolver.resolve(new Domain("baidu.com"));
            Assert.fail("no timeout");
        } catch (IOException e) {
            Assert.assertTrue(e instanceof SocketTimeoutException);
        }
    }
}
