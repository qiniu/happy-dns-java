package qiniu.happydns;

import org.junit.Assert;
import org.junit.Test;
import qiniu.happydns.util.LruCache;

/**
 * Created by bailong on 15/6/20.
 */
public class LruCacheTest {

    @Test
    public void testPut() {
        LruCache<String, String> x = new LruCache<String, String>(2);
        x.put("1", "1");
        x.put("2", "2");
        x.put("3", "3");
        Assert.assertNull(x.get("1"));
        Assert.assertEquals("2", x.get("2"));
        Assert.assertEquals("3", x.get("3"));
        x.delete("2");
        x.put("1", "1");
        Assert.assertEquals("1", x.get("1"));
        Assert.assertNull(x.get("2"));
    }

    @Test
    public void testOut() {
        LruCache<String, String> x = new LruCache<String, String>(2);
        x.put("1", "1");
        x.put("2", "2");
        x.get("1");
        x.put("3", "3");
        Assert.assertNull(x.get("2"));
        Assert.assertEquals("1", x.get("1"));
        Assert.assertEquals("3", x.get("3"));
        x.delete("2");
        x.put("1", "1");
        Assert.assertEquals("1", x.get("1"));
        Assert.assertNull(x.get("2"));
    }

    @Test
    public void testClear() {
        LruCache<String, String> x = new LruCache<String, String>(2);
        x.put("1", "1");
        x.put("2", "2");
        x.put("3", "3");
        x.clear();
        Assert.assertNull(x.get("3"));
        x.put("1", "1");
        x.put("2", "2");
        x.put("3", "3");
        Assert.assertNull(x.get("1"));
        Assert.assertEquals("2", x.get("2"));
        Assert.assertEquals("3", x.get("3"));
    }
}
