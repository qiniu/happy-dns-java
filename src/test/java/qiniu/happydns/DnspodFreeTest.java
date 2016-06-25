package qiniu.happydns;


import org.junit.Assert;
import org.junit.Test;
import qiniu.happydns.http.DnspodFree;

import java.io.IOException;

/**
 * Created by bailong on 15/6/17.
 */
public class DnspodFreeTest {
    @Test
    public void testFound() throws IOException {
        DnspodFree resolver = new DnspodFree();
        try {
            Record[] records = resolver.resolve(new Domain("baidu.com"));
            Assert.assertNotNull(records);
            Assert.assertTrue(records.length > 0);
            for (Record r : records) {
                Assert.assertTrue(r.ttl >= 600);
                Assert.assertTrue(r.isA());
                Assert.assertFalse(r.isExpired());
            }

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testNotFound() throws IOException {
        DnspodFree resolver = new DnspodFree();
        try {
            Record[] records = resolver.resolve(new Domain("7777777.qiniu.com"));
            Assert.assertNull(records);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
