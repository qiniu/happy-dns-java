package happydns;

import qiniu.happydns.DnsClient;

import java.io.IOException;

/**
 * Created by bailong on 16/7/19.
 */
public final class JniBridge {
    private static JniBridge mInstance = new JniBridge();
    private IQuery callback;

    public static JniBridge instance(){
        return mInstance;
    }

    public String[]query(String host){
        if (callback == null){
            return new String[]{host};
        }
        return mInstance.callback.query(host);
    }

    public void setCallback(IQuery query){
        this.callback = query;
    }

    public void setDnsClient(DnsClient client){
        setCallback(new Query(client));
    }

    private static class Query implements IQuery{
        private final DnsClient dns;
        public Query(DnsClient client){
            dns = client;
        }

        @Override
        public String[] query(String host) {
            try {
                return dns.query(host);
            } catch (IOException e) {
                return null;
            }
        }
    }

    private native void setJniCallback();

    native boolean test();
}
