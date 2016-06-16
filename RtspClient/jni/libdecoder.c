#include<stdio.h>
#include<jni.h>
#include<stdlib.h>
#include "libavcodec/avcodec.h"
#include "libavutil/avutil.h"
#include "edu_tfnrc_rtp_codec_h264_NativeH264Decoder.h"
#include "libavutil/frame.h"
#include "libswscale/swscale.h"
#include<android/log.h>
#include<string.h>
#include<malloc.h>

#define LOG_TAG "libdecoder"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

//void convert (int width,int height, AVFrame *in_picture,uint32_t *out);
//Global variables
typedef struct NativeDecoder{
AVCodec * codec;			/*CODEC*/
AVCodecContext * c;		/*CODEC Context*/
AVDictionary * opts;	/*Dictionary*/
AVPacket * pkt;			/*AVPacket*/
int cnt;						/*解码计数*/
int  got_picture;		/*是否解码一帧图像*/
AVFrame * picture, *pictureARGB;		/*解码后的图像帧空间*/
//FILE * out_file;
//uint32_t * out;			/*转换后的rgb数据*/
struct SwsContext * img_convert_ctx;/*转换格式结构*/
} NativeDecoder;

NativeDecoder dec;
static int picture_fill(AVFrame * frame, uint8_t * ptr,
 			enum AVPixelFormat pix_fmt, int width, int height);

JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_initDecoder
  (JNIEnv * env, jclass clazz){

//  out_file = fopen("/sdcard/Pictures/output.rgb", "wb");
  /*CODEC的初始化，初始化一些常量表,在avcodec_register_all()中进行
	avcodec_init();*/

	/*注册CODEC*/
	avcodec_register_all();

	/*查找 H264 CODEC*/
	dec.codec = avcodec_find_decoder(AV_CODEC_ID_H264);

	if(!dec.codec) {
				LOGD("failed to create CODEC");
				return 101;
	}
	/*初始化CODEC的默认参数*/
	dec.c = avcodec_alloc_context3(dec.codec);
	if(!dec.c) {
		LOGD("failed to init CODEC Context");
		return 102;
	}
	/*设置dictionary*/
	/*if(av_dict_set(&dec.opts, "b", "2.5M", 0) < 0){
		LOGD("failed to set dictionary");
		return 103;
	}*/
	/*打开CODEC，这里初始化H.264解码器，调用decode_init本地函数*/
	if (avcodec_open2(dec.c, dec.codec, &(dec.opts)) < 0) 	{
		LOGD("failed to open CODEC");
		return 104;
	}
	/*为AVFrame申请空间，并清零*/
  dec.picture = av_frame_alloc();
  dec.pictureARGB = av_frame_alloc();
	if(!dec.picture || !dec.pictureARGB) 	{
		LOGD("failed to init AVFrame");
		return 105;
	}
	/*为AVPacket申请空间*/
	dec.pkt = (AVPacket*)malloc(sizeof(AVPacket));
	if(!dec.pkt){
		LOGD("failed to get AVPacket");
	}
	av_init_packet(dec.pkt);
  LOGD("init finish");
  return 0;
}

  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_DeinitDecoder
    (JNIEnv * env, jclass clazz){

//    if(out_file) fclose(out_file);
    /*关闭CODEC，释放资源,调用decode_end本地函数*/
	if(dec.c) {
		avcodec_close(dec.c);
		avcodec_free_context(&(dec.c));
		dec.c = NULL;
	}
	/*释放AVFrame空间*/
	if(dec.picture) {
		av_frame_free(&dec.picture);
	}
	if(dec.pictureARGB) {
    		av_frame_free(&dec.pictureARGB);
    	}
	dec.cnt = 0;
	sws_freeContext(dec.img_convert_ctx);
	dec.img_convert_ctx = NULL;
	return 0;

}

  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_DecodeAndConvert
    (JNIEnv *env, jclass clazz, jbyteArray ByteArray, jintArray IntArray){
    	int arrayLen = 0;	//输入进Buf数组字节数
    	int i;
    	uint8_t * Buf = NULL;
		jboolean isCopy = JNI_FALSE;
    	//java byte数组转换为 char 数组
    	arrayLen = (*env)->GetArrayLength(env, ByteArray);
    	Buf = (*env)->GetByteArrayElements(env, ByteArray, JNI_FALSE);
    	if(arrayLen > 0){
    		dec.pkt->data = (uint8_t *)av_malloc(arrayLen);
    		memcpy(dec.pkt->data, Buf, arrayLen);
    		dec.pkt->size = arrayLen;
    		dec.pkt->dts = AV_NOPTS_VALUE;
    		dec.pkt->pts = AV_NOPTS_VALUE;
    	}
    	//释放内存
    	(*env)->ReleaseByteArrayElements(env, ByteArray, Buf, 0);
    	//out 指向输出帧
		jint * out = (*env)->GetIntArrayElements(env, IntArray, &isCopy);

    	if(!dec.pkt->size){
    		LOGD("size is 0");
    		return 201;
    	}
    	//填充目标缓冲区
    	if(0 > picture_fill(dec.pictureARGB, (uint8_t *)out, AV_PIX_FMT_ARGB, 1280, 720))
    		LOGD("failed to fill pictureARGB");

    	//开始解码
    	//NAL 解码, 返回消耗的码流长度
    	int consumed_bytes= avcodec_decode_video2(dec.c, dec.picture, &dec.got_picture, dec.pkt);
    	dec.cnt++;



			/*返回<0 表示解码数据头，返回>0，表示解码一帧图像*/
			if(consumed_bytes > 0 && dec.got_picture)
			{

				/*输出当前的解码信息*/
                LOGI("No:=%4d, length=%4d, flags=%d\n", dec.cnt, consumed_bytes
                	, dec.picture->flags);
				/*从二维空间中提取解码后的图像*/
//				for(i=0; i<c->height; i++)
//					fwrite(picture->data[0] + i * picture->linesize[0], 1, c->width, out_file);
//				for(i=0; i<c->height/2; i++)
//					fwrite(picture->data[1] + i * picture->linesize[1], 1, c->width/2, out_file);
//				for(i=0; i<c->height/2; i++)
//					fwrite(picture->data[2] + i * picture->linesize[2], 1, c->width/2, out_file);
				if(!dec.img_convert_ctx){
                    LOGI("width: %d height: %d", dec.c->width, dec.c->height);
                    dec.img_convert_ctx = sws_getContext(dec.c->width, dec.c->height, dec.c->pix_fmt, dec.c->width, dec.c->height,
                        			AV_PIX_FMT_RGB32, SWS_BICUBIC, NULL, NULL, NULL);
                    if(!dec.img_convert_ctx)
                    	LOGD("failed to get SwsContext");

                }

				int sws_ret = sws_scale(dec.img_convert_ctx, (const uint8_t* const*)dec.picture->data, dec.picture->linesize,
				 	0, dec.c->height, dec.pictureARGB->data, dec.pictureARGB->linesize);
				if(0 > sws_ret)
				 	LOGD("failed to scale:%d", sws_ret);
//				convert(c->width, c->height, picture, out);
//				fwrite(out, 4, c->width * c->height, out_file);
				//rgb图像输出
				(*env)->ReleaseIntArrayElements(env, IntArray, out, 0);
		} else {
				LOGD("No.%d got no picture, length=%d", dec.cnt, consumed_bytes);
				(*env)->ReleaseIntArrayElements(env, IntArray, out, JNI_ABORT);
		}


			if(dec.pkt->data){
				free(dec.pkt->data);
			}
			if(out){
				free(out);
				}


    	return (consumed_bytes > 0 && dec.got_picture) ? 1 : 0;
    }


  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_getVideoWidth
    (JNIEnv *env, jclass clazz){
    	if(!dec.c)
    		return dec.c->width;
    	else
    		return 0;
    }


  JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_getVideoHeight
    (JNIEnv *env, jclass clazz){
    	if(!dec.c)
    		return dec.c->height;
    	else
    		return 0;
    }




//void convert (int width,int height, AVFrame *in_picture,uint32_t *out){
//	uint8_t *pY;
//	uint8_t *pU;
//	uint8_t *pV;
//	int Y,U,V;
//	int i,j;
//	int R,G,B,Cr,Cb;
//
//	/* Init */
//	pY = in_picture->data[0];
//	pU = in_picture->data[1];
//	pV = in_picture->data[2];
//
//	for(i=0;i<height;i++){
//		for(j=0;j<width;j++){
//			/* YUV values uint */
//			Y=*((pY)+ (i*picture->linesize[0]) + j);
//			U=*( pU + (j/2) + ((picture->linesize[1])*(i/2)));
//			V=*( pV + (j/2) + ((picture->linesize[2])*(i/2)));
//			/* RBG values */
//			Cr = V-128;
//			Cb = U-128;
//			R = Y + ((359*Cr)>>8);
//			G = Y - ((88*Cb+183*Cr)>>8);
//			B = Y + ((454*Cb)>>8);
//			if (R>255)R=255; else if (R<0)R=0;
//			if (G>255)G=255; else if (G<0)G=0;
//			if (B>255)B=255; else if (B<0)B=0;
//
//			/* Write data */
//			out[((i*width) + j)]=((((R & 0xFF) << 16) | ((G & 0xFF) << 8) | (B & 0xFF))& 0xFFFFFFFF);
//		}
//	}
//}

JNIEXPORT jint JNICALL Java_edu_tfnrc_rtp_codec_h264_NativeH264Decoder_findDecoder
    (JNIEnv *env, jclass clazz, jint codecId){

//    	avcodec_register_all();
//    	codec = avcodec_find_decoder(codecId);
//    	if(codec)
//    		return 0;
//    	else
//    		return -1;
    }
static int picture_fill(AVFrame * frame, uint8_t * ptr,
     			enum AVPixelFormat pix_fmt, int width, int height){
			int size, w2, h2, size2;
            size = width * height;
            switch(pix_fmt) {
                case AV_PIX_FMT_YUV420P:
                case AV_PIX_FMT_YUV422P:
                case AV_PIX_FMT_YUV444P:
                case AV_PIX_FMT_YUV410P:
                case AV_PIX_FMT_YUV411P:
                case AV_PIX_FMT_YUVJ420P:
                case AV_PIX_FMT_YUVJ422P:
                case AV_PIX_FMT_YUVJ444P:
                	w2 = (width + 1) >> 1;
                	h2 = (height + 1) >> 1;
                	size2 = w2*h2;
                	frame->data[0] = ptr;
                    frame->data[1] = frame->data[0] + size;
                    frame->data[2] = frame->data[1] + size2;
                    frame->linesize[0] = width;
                    frame->linesize[1] = w2;
                    frame->linesize[2] = w2;
                    return size + 2 * size2;
				case AV_PIX_FMT_RGB24:
                case AV_PIX_FMT_BGR24:
                    frame->data[0] = ptr;
                    frame->data[1] = NULL;
                    frame->data[2] = NULL;
                    frame->linesize[0] = width * 3;
                    return size * 3;
                case AV_PIX_FMT_RGBA:
                case AV_PIX_FMT_ARGB:
                case AV_PIX_FMT_ABGR:
                case AV_PIX_FMT_BGRA:
                	frame->data[0] = ptr;
                    frame->data[1] = NULL;
                    frame->data[2] = NULL;
                    frame->linesize[0] = width * 4;
                    return size * 4;
                case AV_PIX_FMT_RGB555:
                case AV_PIX_FMT_RGB565:
                	frame->data[0] = ptr;
                	frame->data[1] = NULL;
                	frame->data[2] = NULL;
                	frame->linesize[0] = width * 2;
                	return size * 2;
                case AV_PIX_FMT_GRAY8:
                	frame->data[0] = ptr;
                    frame->data[1] = NULL;
                    frame->data[2] = NULL;
                    frame->linesize[0] = width;
                    return size;
                case AV_PIX_FMT_MONOWHITE:
                case AV_PIX_FMT_MONOBLACK:
                    frame->data[0] = ptr;
                    frame->data[1] = NULL;
                    frame->data[2] = NULL;
                    frame->linesize[0] = (width + 7) >> 3;
                    return frame->linesize[0] * height;
				case AV_PIX_FMT_PAL8:
                    size2 = (size + 3) & ~3;
                    frame->data[0] = ptr;
                    frame->data[1] = ptr + size2; /* palette is stored here as 256 32 bit words */
                    frame->data[2] = NULL;
                    frame->linesize[0] = width;
                    frame->linesize[1] = 4;
                    return size2 + 256 * 4;
                default:
                    frame->data[0] = NULL;
                    frame->data[1] = NULL;
                    frame->data[2] = NULL;
                    frame->data[3] = NULL;
                    return -1;
                }

     	}
