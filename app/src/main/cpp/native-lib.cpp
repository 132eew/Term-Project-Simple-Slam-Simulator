#include <jni.h>
#include <string.h>
#include <android/log.h>
#include <fcntl.h>
#include <unistd.h>

#define PUSH_SWITCH_DEVICE "/dev/fpga_push_switch"
#define MAX_BUTTON 9

#define FND_DEVICE "/dev/fpga_fnd"
#define MAX_DIGIT 4

#define BUZZER_DEVICE "/dev/fpga_buzzer"

///////////////////////////////////////////////////////////////////////
int gFd = -1;

int fpga_push_open(void){
    int dev;

    dev = open(PUSH_SWITCH_DEVICE,O_RDWR);
    if(dev < 0){
        return -1;
    } else{
        gFd = dev;
    }
    return 0;
}

int fpga_push_close(void){
    if(gFd<0){
        return 0;
    }else{
        close(gFd);
        return -1;
    }
}

int fpga_push_switch(void){
    int i;
    int dev;
    size_t buff_size;
    int retval;

    unsigned char push_sw_buff[MAX_BUTTON];

    if(gFd<0){
        return -1;
    }else{
        buff_size = sizeof(push_sw_buff);
        read(gFd,&push_sw_buff,buff_size);
        retval = 0;

        for(i=0;i<MAX_BUTTON;i++){
            if(push_sw_buff[i] != 0){
                retval |= 0x1 << i;
            }
        }
    }
    return retval;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_e_term_1proj_MainActivity_DeviceOpen(JNIEnv *env, jobject thiz) {
    int retval;
    retval = fpga_push_open();
    return retval;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_e_term_1proj_MainActivity_DeviceClose(JNIEnv *env, jobject thiz) {
    int retval;
    retval = fpga_push_close();
    return retval;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_e_term_1proj_MainActivity_ReceivePushSwitchValue(JNIEnv *env, jobject thiz) {
    int retval;
    retval = fpga_push_switch();
    return retval;

}
///////////////////////////////////////////////////////////////////////

int fpga_fnd(const char* str) {

    int dev;
    unsigned char data[4];
    unsigned char retval;
    int i;
    int str_size;

    memset(data,0,sizeof(data));

    str_size=(strlen(str));
    if(str_size>MAX_DIGIT){
        str_size = MAX_DIGIT;
    }

    for(i = 0; i<str_size;i++){
        if((str[i]<0x30)||(str[i]>0x39)){
            return 1;
        }
        data[i] = str[i] - 0x30;
    }

    dev = open(FND_DEVICE,O_RDWR);
    if(dev<0){
//        __android_log_print(ANDROID_LOG_INFO,"Device Open Error","Driver = %s",str);
    }else{
//        __android_log_print(ANDROID_LOG_INFO,"Device Open Success","Driver = %s",str);
        write(dev, &data,4);
        close(dev);
        return 0;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_e_term_1proj_SLAM_ReceiveFndValue(JNIEnv *env, jobject thiz, jstring val) {
    jint result;
    const char * str = (*env).GetStringUTFChars(val,NULL);
    result = fpga_fnd(str);
    (*env).ReleaseStringUTFChars(val, str);

    return result;
}
///////////////////////////////////////////////////////////////////////

int fpga_buzzer(int x){

    int dev;
    unsigned char data;
    unsigned char retval;

    data = (char)x;

    dev = open(BUZZER_DEVICE,O_RDWR);

    if(dev<0){
        return -1;
    } else{
        write(dev,&data,1);
        close(dev);
        return 0;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_e_term_1proj_SLAM_buzzvalue(JNIEnv *env, jobject thiz, jint x) {
    jint result;
    result = fpga_buzzer(x);

    return result;
}
