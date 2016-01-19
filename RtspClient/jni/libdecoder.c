#include<stdio.h>
#include<jni.h>
#include<stdlib.h>
#include "avcodec.h"
#include "edu_tfnrc_rtp_codec_h264_NativeH264Decoder.h"
#include "h264.h"
#include<android/log.h>
#include<string.h>
#include<malloc.h>

#define LOG_TAG "libdecoder"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

//Global variables

AVCodec * codec;			/*CODEC*/
AVCodecContext * c;		/*CODEC Context*/
int cnt;						//解码计数
int  got_picture;		/*是否解码一帧图像*/
AVFrame * picture;		/*解码后的图像帧空间*/
FILE * out_file;

JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_initDecoder
  (JNIEnv * env, jclass clazz){

  out_file = fopen("/sdcard/Pictures/output.rgb", "wb");
  /*CODEC的初始化，初始化一些常量表*/
	avcodec_init();

	/*注册CODEC*/
	avcodec_register_all();

	/*查找 H264 CODEC*/
	codec = avcodec_find_decoder(CODEC_ID_H264);

	if(!codec) {
				LOGD("failed to create CODEC");
				return 101;
	}
	/*初始化CODEC的默认参数*/
	c = avcodec_alloc_context();
	if(!c) {
		LOGD("failed to init CODEC Context");
		return 102;
	}

	/*打开CODEC，这里初始化H.264解码器，调用decode_init本地函数*/
	if (avcodec_open(c, codec) < 0) 	{
		LOGD("failed to open CODEC");
		return 103;
	}
	/*为AVFrame申请空间，并清零*/
  picture   = avcodec_alloc_frame();
	if(!picture) 	{
		LOGD("failed to init AVFrame");
		return 104;
	}
  LOGD("init finish");
  return 0;
}

  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_DeinitDecoder
    (JNIEnv * env, jclass clazz){

    if(out_file) fclose(out_file);
    /*关闭CODEC，释放资源,调用decode_end本地函数*/
	if(c) {
		avcodec_close(c);
		av_free(c);
		c = NULL;
	}
	/*释放AVFrame空间*/
	if(picture) {
		av_free(picture);
		picture = NULL;
	}
	cnt = 0;
	return 0;

}

  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_DecodeAndConvert
    (JNIEnv *env, jclass clazz, jbyteArray ByteArray, jintArray IntArray){
    	int size = 0;	//输入进Buf数组字节数
    	int i;
    	unsigned char * Buf = NULL;	/*input H264 stream*/
    	uint32_t * out = NULL;
    	//java byte数组转换为 char 数组
    	jsize arrayLen = (*env)->GetArrayLength(env, ByteArray);
    	jbyte * data = (*env)->GetByteArrayElements(env, ByteArray, JNI_FALSE);
    	if(arrayLen > 0){
    		Buf = (unsigned char*)av_malloc(arrayLen);
    		memcpy(Buf, data, arrayLen);
    		size = arrayLen;
    	}
    	//释放内存
    	(*env)->ReleaseByteArrayElements(env, ByteArray, data, 0);

    	if(!size){
    		LOGD("size is 0");
    		return 201;
    	}
    	//开始解码

    	//NAL 解码, 返回消耗的码流长度
    	int consumed_bytes= avcodec_decode_video(c, picture, &got_picture, Buf, size);
    	cnt++;

    	/*输出当前的解码信息*/
    	LOGI("No:=%4d, length=%4d\n",cnt,consumed_bytes);

			/*返回<0 表示解码数据头，返回>0，表示解码一帧图像*/
			if(consumed_bytes > 0)
			{
//				/*从二维空间中提取解码后的图像*/
//				for(i=0; i<c->height; i++)
//					fwrite(picture->data[0] + i * picture->linesize[0], 1, c->width, out_file);
//				for(i=0; i<c->height/2; i++)
//					fwrite(picture->data[1] + i * picture->linesize[1], 1, c->width/2, out_file);
//				for(i=0; i<c->height/2; i++)
//					fwrite(picture->data[2] + i * picture->linesize[2], 1, c->width/2, out_file);

				/*解码后得到YUV格式图像转换为RGB24格式*/
				out = (uint32_t*)av_malloc(c->width * c->height * 4);
				convert(c->width, c->height, picture, out);
				(*env)->SetIntArrayRegion(IntArray, 0, c->width, c->height, (const jint*)out);
//				fwrite(out, 4, c->width * c->height, out_file);

		}
			//释放缓存
			if(Buf){
				free(Buf);
			}
			if(out){
				free(out);
			}

    	return 0;
    }


  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_getVideoWidth
    (JNIEnv *env, jclass clazz){
    	if(!c)
    		return c->width;
    	else
    		return 0;
    }


  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_getVideoHeight
    (JNIEnv *env, jclass clazz){
    	if(!c)
    		return c->height;
    	else
    		return 0;
    }

void convert (int width,int height, AVFrame *in_picture,uint32_t *out){
	uint8_t *pY;
	uint8_t *pU;
	uint8_t *pV;
	int Y,U,V;
	int i,j;
	int R,G,B,Cr,Cb;

	/* Init */
	pY = in_picture->data[0];
	pU = in_picture->data[1];
	pV = in_picture->data[2];

	for(i=0;i<height;i++){
		for(j=0;j<width;j++){
			/* YUV values uint */
			Y=*((pY)+ (i*picture->linesize[0]) + j);
			U=*( pU + (j/2) + ((picture->linesize[1])*(i/2)));
			V=*( pV + (j/2) + ((picture->linesize[2])*(i/2)));
			/* RBG values */
			Cr = V-128;
			Cb = U-128;
			R = Y + ((359*Cr)>>8);
			G = Y - ((88*Cb+183*Cr)>>8);
			B = Y + ((454*Cb)>>8);
			if (R>255)R=255; else if (R<0)R=0;
			if (G>255)G=255; else if (G<0)G=0;
			if (B>255)B=255; else if (B<0)B=0;

			/* Write data */
			out[((i*width) + j)]=((((R & 0xFF) << 16) | ((G & 0xFF) << 8) | (B & 0xFF))& 0xFFFFFFFF);
		}
	}
}
