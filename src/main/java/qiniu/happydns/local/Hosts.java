package qiniu.happydns.local;

import qiniu.happydns.http.IHosts;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by bailong on 15/6/18.
 */
public final class Hosts implements IHosts {

    private final Hashtable<String, ArrayList<String>> hosts = new Hashtable<String, ArrayList<String>>();

    @Override
    public String[] query(String domain) {
        ArrayList<String> values = hosts.get(domain);
        if (values == null || values.isEmpty()) {
            return null;
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
}
