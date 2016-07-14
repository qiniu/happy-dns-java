package qiniu.happydns.local;

import qiniu.happydns.http.IHosts;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

/**
 * Created by bailong on 15/6/18.
 */
public final class Hosts implements IHosts {

    private final Hashtable<String, ArrayList<String>> hosts = new Hashtable<String, ArrayList<String>>();
    private final long keyIndex = System.currentTimeMillis();

    @Override
    public String[] query(String domain) {
        ArrayList<String> values = hosts.get(domain);
        if (values == null || values.isEmpty()) {
            return null;
        }
        if (values.size() > 1) {
            String first = values.get(0);
            values.remove(0);
            values.add(first);
        }
        String[] r = new String[values.size()];
        return values.toArray(r);
    }

    @Override
    public Hosts put(String domain, String ip) {
        ArrayList<String> ips = hosts.get(domain);
        if (ips == null) {
            ips = new ArrayList<String>();
        }
        ips.add(ip);
        hosts.put(domain, ips);
        return this;
    }

    /**
     * avoid many server visit first ip in same time.s
     *
     * @param domain 域名
     * @param ips    IP 列表
     * @return 当前host 实例
     */
    public Hosts putAllInRandomOrder(String domain, String[] ips) {
        Random random = new Random();
        int index = (int) (random.nextLong() % ips.length);
        if (index < 0) {
            index += ips.length;
        }
        ArrayList<String> ipList = new ArrayList<String>();
        for (int i = 0; i < ips.length; i++) {
            ipList.add(ips[(i + index) % ips.length]);
        }
        hosts.put(domain, ipList);
        return this;
    }
}
