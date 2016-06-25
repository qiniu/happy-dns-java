package qiniu.happydns.local;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by bailong on 16/6/25.
 */

// this is invalid on android
public final class SystemDnsServer {
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
            FileReader r = new FileReader("/etc/resolv.conf");
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
}
