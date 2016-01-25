#ifndef _MY_YUV2RGB_H_
#define _MY_YUV2RGB_H_

#if defined(__GNUC__)
#    define av_unused __attribute__((unused))
#else
#    define av_unused
#endif
#include "common.h"
#include "avcodec.h"
#define YUVRGB_TABLE_HEADROOM 256



typedef struct MySwsContext {
	
	int srcW;                     ///< Width  of source      luma/alpha planes.
    int srcH;                     ///< Height of source      luma/alpha planes.
    int dstH;                     ///< Height of destination luma/alpha planes.
    int dstW;                     ///< Width  of destination luma/alpha planes.
	
	enum PixelFormat dstFormat; ///< Destination pixel format.
    enum PixelFormat srcFormat; ///< Source      pixel format.
    
    int srcRange;                 ///< 0 = MPG YUV range, 1 = JPG YUV range (source      image).
    int dstRange;                 ///< 0 = MPG YUV range, 1 = JPG YUV range (destination image).
    
    int dstFormatBpp;             ///< Number of bits per pixel of the destination pixel format.
    int srcFormatBpp;             ///< Number of bits per pixel of the source      pixel format.
    
    //颜色空间整体控制（暂不用）
//    int contrast, brightness, saturation;
    
    //tables
    int table_gV[256 + 2*YUVRGB_TABLE_HEADROOM];
    uint8_t *table_rV[256 + 2*YUVRGB_TABLE_HEADROOM];
    uint8_t *table_gU[256 + 2*YUVRGB_TABLE_HEADROOM];
    uint8_t *table_bU[256 + 2*YUVRGB_TABLE_HEADROOM];
    
    void *yuvTable;             // pointer to the yuv->rgb table start so it can be freed()
    
//    uint64_t yCoeff;
//    uint64_t vrCoeff;
//    uint64_t ubCoeff;
//    uint64_t vgCoeff;
//    uint64_t ugCoeff;
//    
//    uint64_t yOffset;
//    uint64_t uOffset;
//    uint64_t vOffset;
    
} MySwsContext;
//转换函数本体
int yuv2argb_c(MySwsContext *c, const uint8_t * src[], 
				int srcStride[], int srcSliceY, int srcSliceH, uint8_t *dst[], int dstStride[]);
				
//初始化MySwsContext
int init_MySwsContext(MySwsContext *c);
//释放Context
void free_MySwsContext(MySwsContext *c);
//获取转换参数集
MySwsContext * sws_getContext(int srcW, int srcH, enum PixelFormat srcFormat, int dstW, int dstH, enum PixelFormat dstFormat);


//初始化转换对照表
int yuv2rgb_c_init_tables(MySwsContext *c, const int inv_table[4], int fullRange, int brightness,
      int contrast, int saturation);

void fill_table(uint8_t* table[256 + 2 * YUVRGB_TABLE_HEADROOM], const int elemsize,
	const int64_t inc, void *y_tab);

void fill_gv_table(int table[256 + 2 * YUVRGB_TABLE_HEADROOM], const int elemsize, const int64_t inc);

#endif