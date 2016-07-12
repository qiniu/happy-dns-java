package qiniu.happydns;

import java.io.IOException;

// 同步的域名查询接口，没有独立线程, 可以有local dns, httpdns 等实现

/**
 * 同步的域名查询接口，可以有local dns, httpdns 等实现
 */
public interface IResolver {
    int DNS_DEFAULT_TIMEOUT = 20;

    /**
     * 根据域名参数进行查询
     *
     * @param domain 域名参数
     * @return dns记录列表
     * @throws IOException 劫持或者网络异常
     */
    Record[] resolve(Domain domain) throws IOException;
}
