package qiniu.happydns;

import qiniu.happydns.http.DomainNotOwn;
import qiniu.happydns.http.IHosts;
import qiniu.happydns.local.Hosts;
import qiniu.happydns.util.IP;
import qiniu.happydns.util.LruCache;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;


/**
 * DNS解析客户端，需要重复使用
 */
public final class DnsClient {

    public final IHosts hosts;
    private final IResolver[] resolvers;
    private final LruCache<String, Record[]> cache;
    private volatile int index = 0;


    /**
     * @param resolvers 具体的dns 解析示例，可以是local或者httpdns
     */
    public DnsClient(IResolver[] resolvers) {
        this(resolvers, new Hosts());
    }

    /**
     * @param resolvers 具体的dns 解析示例，可以是local或者httpdns
     * @param hosts     hosts 指定本地IP
     */
    public DnsClient(IResolver[] resolvers, IHosts hosts) {
        this.resolvers = resolvers.clone();
        cache = new LruCache<String, Record[]>(128);
        this.hosts = hosts;
    }

    private static Record[] trimCname(Record[] records) {
        ArrayList<Record> a = new ArrayList<Record>(records.length);
        for (Record r : records) {
            if (r != null && r.type == Record.TYPE_A) {
                a.add(r);
            }
        }
        return a.toArray(new Record[a.size()]);
    }

    private static void rotate(Record[] records) {
        if (records != null && records.length > 1) {
            Record first = records[0];
            System.arraycopy(records, 1, records, 0, records.length - 1);
            records[records.length - 1] = first;
        }
    }

    private static String[] records2Ip(Record[] records) {
        if (records == null || records.length == 0) {
            return null;
        }
        ArrayList<String> a = new ArrayList<String>(records.length);
        for (Record r : records) {
            a.add(r.value);
        }
        if (a.size() == 0) {
            return null;
        }
        return a.toArray(new String[a.size()]);
    }

    /**
     * 查询域名
     *
     * @param domain 域名
     * @return ip 列表
     * @throws IOException 网络异常或者无法解析抛出异常
     */
    public String[] query(String domain) throws IOException {
        return query(new Domain(domain));
    }

    public String[] query(Domain domain) throws IOException {
        if (domain == null) {
            throw new IOException("null domain");
        }
        if (domain.domain == null || domain.domain.trim().length() == 0) {
            throw new IOException("empty domain " + domain.domain);
        }

        if (IP.isValid(domain.domain)) {
            return new String[]{domain.domain};
        }

        String[] r = queryInternal(domain);
        if (r == null || r.length <= 1) {
            return r;
        }
        return r;
    }

    /**
     * 查询域名
     *
     * @param domain 域名参数
     * @return ip 列表
     * @throws IOException 网络异常或者无法解析抛出异常
     */

    private String[] queryInternal(Domain domain) throws IOException {
        Record[] records = null;
        if (domain.hostsFirst) {
            String[] ret = hosts.query(domain.domain);
            if (ret != null && ret.length != 0) {
                return ret;
            }
        }
        synchronized (cache) {
            if (Network.isNetworkChanged()) {
                cache.clear();
                synchronized (resolvers) {
                    index = 0;
                }
            } else {
                records = cache.get(domain.domain);
                if (records != null && records.length != 0) {
                    if (!records[0].isExpired()) {
                        rotate(records);
                        return records2Ip(records);
                    } else {
                        records = null;
                    }
                }
            }
        }

        IOException lastE = null;
        int firstOk = index;
        for (int i = 0; i < resolvers.length; i++) {
            int pos = (firstOk + i) % resolvers.length;
            String ip = Network.getIp();
            try {
                records = resolvers[pos].resolve(domain);
            } catch (DomainNotOwn e) {
                continue;
            } catch (IOException e) {
                lastE = e;
                e.printStackTrace();
            } catch (Exception e) {
                lastE = new IOException(e);
                e.printStackTrace();
            }
            String ip2 = Network.getIp();
            if ((records == null || records.length == 0) && ip.equals(ip2)) {
                synchronized (resolvers) {
                    if (index == firstOk) {
                        index++;
                        if (index == resolvers.length) {
                            index = 0;
                        }
                    }
                }
            } else {
                break;
            }
        }

        if (records == null || records.length == 0) {
            if (!domain.hostsFirst) {
                String[] rs = hosts.query(domain.domain);
                if (rs != null && rs.length != 0) {
                    return rs;
                }
            }
            if (lastE != null) {
                throw lastE;
            }
            throw new UnknownHostException(domain.domain);
        }
        records = trimCname(records);
        if (records.length == 0) {
            throw new UnknownHostException("no A records");
        }
        synchronized (cache) {
            cache.put(domain.domain, records);
        }
        return records2Ip(records);
    }

    public InetAddress[] queryInetAddress(Domain domain) throws IOException {
        String[] ips = query(domain);
        InetAddress[] addresses = new InetAddress[ips.length];
        for (int i = 0; i < ips.length; i++) {
            addresses[i] = InetAddress.getByName(ips[i]);
        }
        return addresses;
    }

    private void clearCache() {
        synchronized (cache) {
            cache.clear();
        }
    }

    /**
     * 当网络发生变化时，进行通知
     */
    public void onNetworkChange() {
        clearCache();
        synchronized (resolvers) {
            index = 0;
        }
    }
}
