#include <stdlib.h>
#include <string.h>


#include "happydns_JniBridge.h"
#include "qn_getaddrinfo.h"

#ifdef __cplusplus
extern "C" {
#endif

static jmethodID dns_callback_method_id = NULL;
static JavaVM* javaVM = NULL;
static jobject g_jni_bridge = NULL;

#ifdef __cplusplus
}
#endif

static qn_ips_ret* jni_dns_callback(const char *host){
   if (g_jni_bridge == NULL) {
        //only for compatible
        qn_ips_ret *ret = (qn_ips_ret *)calloc(sizeof(char *), 2);
        ret->ips[0] = strdup(host);
        return ret;
    }

    JNIEnv *env = NULL;
    (*javaVM)->AttachCurrentThread(javaVM, (void **)&env, NULL);
    jstring domain = (*env)->NewStringUTF(env, host);
    jobjectArray ips = NULL;
    ips = (jobjectArray)(*env)->CallObjectMethod(env, g_jni_bridge, dns_callback_method_id, domain);
    if( ips == NULL){
        (*env)->DeleteLocalRef(env, domain);
        (*javaVM)->DetachCurrentThread(javaVM);
        return NULL;
    }
    jsize stringCount = (*env)->GetArrayLength(env, ips);
    qn_ips_ret *ret = (qn_ips_ret *)calloc(sizeof(char *), stringCount + 1);
    for (int i = 0; i < stringCount; i++) {
       jstring string = (jstring) (*env)->GetObjectArrayElement(env, ips, i);
       jboolean isCopy = JNI_FALSE;
       const char* c = (*env)->GetStringUTFChars(env, string, &isCopy);
       char* ip2 = strdup(c);
       (*env)->DeleteLocalRef(env, string);
       if(isCopy == JNI_TRUE){
           (*env)->ReleaseStringUTFChars(env, string, c);
       }
       ret->ips[i] = ip2;
    }

     (*env)->DeleteLocalRef(env, domain);
     (*env)->DeleteLocalRef(env, ips);
     (*javaVM)->DetachCurrentThread(javaVM);
    return ret;
}

JNIEXPORT void JNICALL Java_happydns_JniBridge_setJniCallback
  (JNIEnv *env, jobject obj) {
    if(javaVM != NULL){
        return;
    }
    (*env)->GetJavaVM(env, &javaVM);
    jclass cls = (*env)->GetObjectClass(env, obj);
    g_jni_bridge = (*env)->NewGlobalRef(env, obj);
    dns_callback_method_id = (*env)->GetMethodID(env, cls, "query", "(Ljava/lang/String;)[Ljava/lang/String;");
    qn_set_dns_callback(jni_dns_callback);
}

static int count(struct addrinfo *ai) {
    int count = 0;
    while (ai != NULL) {
        count++;
        ai = ai->ai_next;
    }
    return count;
}

static struct addrinfo resetHints() {
    struct addrinfo hints = {0};
    hints.ai_family = PF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_DEFAULT;
    return hints;
}

jboolean testNotSet() {
    jboolean ret = 0;
    qn_set_dns_callback(NULL);
    javaVM = NULL;
    g_jni_bridge = NULL;
    struct addrinfo hints = resetHints();
    struct addrinfo *ai = NULL;
    int x = qn_getaddrinfo("baidu.com", "80", &hints, &ai);
    if(x != 0 || ai == NULL){
       ret++;
    }

    struct addrinfo hints2 = resetHints();
    struct addrinfo *ai2 = NULL;
    int x2 = getaddrinfo("baidu.com", "80", &hints2, &ai2);
    printf("not set return qn %d, ori %d\n", x, x2);
    printf("not set count qn %d, ori %d\n", count(ai), count(ai2));
    if(x2 != x){
        ret++;
    }
    freeaddrinfo(ai2);

    qn_freeaddrinfo(ai);
    return ret;
}

jint template(const char * host) {
    jint ret = 0;
    struct addrinfo hints = resetHints();
    struct addrinfo *ai = NULL;
    int x = qn_getaddrinfo(host, "80", &hints, &ai);
    if(x != 0 || ai == NULL){
           ret++;
    }

    struct addrinfo hints2 = resetHints();
    struct addrinfo *ai2 = NULL;
    int x2 = getaddrinfo(host, "80", &hints2, &ai2);
    if(x2 != x){
        ret++;
    }
    if(count(ai) > count(ai2)){
        ret++;
    }
    printf("%s return qn %d, ori %d\n", host, x, x2);
    printf("%s count qn %d ori %d\n", host, count(ai), count(ai2));
    freeaddrinfo(ai2);

    qn_freeaddrinfo(ai);
    return ret;
}

jint testCustomDns() {
    jint ret = 0;
    ret += template("baidu.com");
    ret += template("www.qiniu.com");
    ret += template("qq.com");
    ret += template("taobao.com");
    return ret;
}


/*
 * Class:     happydns_JniBridge
 * Method:    test
 * Signature: ()Z
 */
JNIEXPORT jint JNICALL Java_happydns_JniBridge_test
  (JNIEnv *env, jobject obj) {
  if(javaVM == NULL){
      return testNotSet();
  }

  return testCustomDns();
}

