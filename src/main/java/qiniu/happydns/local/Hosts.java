package qiniu.happydns.local;

import qiniu.happydns.http.IHosts;

import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Random;

/**
 * Created by bailong on 15/6/18.
 */
public final class Hosts implements IHosts {

    private final Hashtable<String, LinkedList<String>> hosts = new Hashtable<String, LinkedList<String>>();

    @Override
    public synchronized String[] query(String domain) {
        LinkedList<String> values = hosts.get(domain);
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
    public synchronized Hosts put(String domain, String ip) {
        LinkedList<String> ips = hosts.get(domain);
        if (ips == null) {
            ips = new LinkedList<String>();
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
    public synchronized Hosts putAllInRandomOrder(String domain, String[] ips) {
        Random random = new Random();
        int index = (int) (random.nextLong() % ips.length);
        if (index < 0) {
            index += ips.length;
        }
        LinkedList<String> ipList = new LinkedList<String>();
        for (int i = 0; i < ips.length; i++) {
            ipList.add(ips[(i + index) % ips.length]);
        }
        hosts.put(domain, ipList);
        return this;
    }
}
