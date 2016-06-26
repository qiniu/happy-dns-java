package qiniu.happydns.local;

import qiniu.happydns.Domain;
import qiniu.happydns.IResolver;
import qiniu.happydns.Record;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by bailong on 16/6/25.
 */

// this is invalid on android
public final class SystemDnsServer {
    private static final boolean isWindows = System.getProperty("os.name", "").toLowerCase().startsWith("windows");
    private static final String confName = "/etc/resolv.conf";
    private static long lastMondifyTime = 0;
    private static String[] curAddresses = null;

    private SystemDnsServer() {
    }

    // has 300s latency when system resolv change https://community.oracle.com/thread/1148912
    public static String[] getByJNDI() {
        try {
            Class<?> dirContext =
                    Class.forName("javax.naming.directory.DirContext");
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            Class<?> initialDirContext =
                    Class.forName("javax.naming.directory.InitialDirContext");
            Constructor<?> constructor = initialDirContext.getConstructor(Hashtable.class);

            Object obj = constructor.newInstance(env);
            Method m = dirContext.getMethod("getEnvironment");
            Hashtable newEnv = (Hashtable) m.invoke(obj);
            String dns = (String) newEnv.get("java.naming.provider.url");
            String dnsTrim = dns.replace("dns://", "");
            return dnsTrim.split(" ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // has 300s latency when system resolv change
    public static String[] getByInternalAPI() {
        try {
            Class<?> resolverConfiguration =
                    Class.forName("sun.net.dns.ResolverConfiguration");
            Method open = resolverConfiguration.getMethod("open");
            Method getNameservers = resolverConfiguration.getMethod("nameservers");
            Object instance = open.invoke(null);
            List nameservers = (List) getNameservers.invoke(instance);
            if (nameservers == null || nameservers.size() == 0) {
                return null;
            }
            String[] ret = new String[nameservers.size()];
            int i = 0;
            for (Object dns : nameservers) {
                ret[i++] = (String) dns;
            }
            return ret;
        } catch (Exception e) {
            // we might trigger some problems this way
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getByUnixConf() {
        ArrayList<String> lines = new ArrayList<String>();
        try {
            FileReader r = new FileReader(confName);
            BufferedReader input = new BufferedReader(r);
            String currentLine = null;
            while ((currentLine = input.readLine()) != null) {
                // Take care of potential comments
                currentLine = currentLine.substring(0, currentLine.indexOf("#") == -1
                        ? currentLine.length() : currentLine.indexOf("#"));

                if (currentLine.contains("nameserver")) {
                    lines.add(currentLine.replace("nameserver", "").trim());
                }
            }
            if (lines.size() == 0) {
                return null;
            }
            String[] ret = new String[lines.size()];
            return lines.toArray(ret);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] defaultServer() {
        String[] addresses = null;
        File f = new File(confName);

        if (!isWindows && f.exists()) {
            if (f.lastModified() != lastMondifyTime) {
                curAddresses = getByUnixConf();
            }
            addresses = curAddresses;
        }
        if (addresses == null) {
            addresses = getByInternalAPI();
            if (addresses == null) {
                addresses = getByJNDI();
            }
        }
        return addresses;
    }

    //system ip would change
    public static IResolver defaultResolver() {
        return new IResolver() {
            @Override
            public Record[] resolve(Domain domain) throws IOException {
                String[] addresses = defaultServer();
                if (addresses == null) {
                    throw new IOException("no dns server");
                }
                IResolver resolver = new Resolver(InetAddress.getByName(addresses[0]));
                return resolver.resolve(domain);
            }
        };
    }
}
