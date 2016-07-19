#include "happydns_JniBridge.h"
#include "qn_getaddrinfo.h"

JNIEXPORT void JNICALL Java_happydns_JniBridge_setJniCallback
  (JNIEnv *env, jobject obj) {
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