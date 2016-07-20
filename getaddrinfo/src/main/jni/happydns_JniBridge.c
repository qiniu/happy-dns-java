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
//    (*env)->(env, g_obj, dns_callback_method_id, domain);

    if( ips == NULL){
        return NULL;
    }
    jsize stringCount = (*env)->GetArrayLength(env, ips);
    qn_ips_ret *ret = (qn_ips_ret *)calloc(sizeof(char *), stringCount + 1);
    for (int i = 0; i < stringCount; i++) {
       jstring string = (jstring) (*env)->GetObjectArrayElement(env, ips, i);
       jboolean isCopy = JNI_FALSE;
       const char* c = (*env)->GetStringUTFChars(env, string, &isCopy);
       char* ip2 = strdup(c);
       if(isCopy == JNI_TRUE){
           (*env)->ReleaseStringUTFChars(env, string, c);
       }
       ret->ips[i] = ip2;
    }
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
    dns_callback_method_id = (*env)->GetMethodID(env, cls, "query", "(I)V");
    qn_set_dns_callback(jni_dns_callback);
}

/*
 * Class:     happydns_JniBridge
 * Method:    test
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_happydns_JniBridge_test
  (JNIEnv *env, jobject obj) {

  return JNI_TRUE;
}

