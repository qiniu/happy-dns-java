package qiniu.happydns.util;

/**
 * Created by bailong on 16/6/28.
 */
public final class IP {
    private IP() {
    }

    public static boolean isValid(String ip) {
        if (ip == null || ip.length() < 7 || ip.length() > 15) return false;
        if (ip.contains("-")) return false;

        try {
            int x = 0;
            int y = ip.indexOf('.');

            if (y != -1 && Integer.parseInt(ip.substring(x, y)) > 255) return false;

            x = ip.indexOf('.', ++y);
            if (x != -1 && Integer.parseInt(ip.substring(y, x)) > 255) return false;

            y = ip.indexOf('.', ++x);
            return !(y != -1 && Integer.parseInt(ip.substring(x, y)) > 255
                    && Integer.parseInt(ip.substring(++y, ip.length() - 1)) > 255
                    && ip.charAt(ip.length() - 1) != '.');

        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDomain(String domain) {
        int l = domain.length();
        if (l > 15 || l < 7) {
            return true;
        }

        byte[] str = domain.getBytes();
        if (str == null) {
            return false;
        }

        for (byte b : str) {
            if ((b < '0' || b > '9') && b != '.') {
                return true;
            }
        }

        return false;
    }
}
