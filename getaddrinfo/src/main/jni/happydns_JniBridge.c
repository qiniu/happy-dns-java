#include "happydns_JniBridge.h"
#include "qn_getaddrinfo.h"

static jobject dnsHolder = NULL;

static qn_ips_ret* jni_dns_callback(const char *host){
    return NULL;
}

JNIEXPORT void JNICALL Java_happydns_JniBridge_setJniCallback
  (JNIEnv *env, jobject obj) {
    dnsHolder = obj;
    qn_set_dns_callback(jni_dns_callback);
}

/*
 * Class:     happydns_JniBridge
 * Method:    test
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_happydns_JniBridge_test
  (JNIEnv *env, jobject obj) {

  return true;
}