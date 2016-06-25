package qiniu.happydns.http;

/**
 * Created by bailong on 16/6/25.
 */
public interface IHosts {
    String[] query(String domain);

    IHosts put(String domain, String ip);
}
