//YUV转换为ARGB格式函数
#include"common.h"
#include"myyuv2rgb.h"
#include"avcodec.h"

const int ff_yuv2rgb_coeffs[8][4] = {
	{ 117504, 138453, 13954, 34903 }, /* no sequence_display_extension */
	{ 117504, 138453, 13954, 34903 }, /* ITU-R Rec. 709 (1990) */
	{ 104597, 132201, 25675, 53279 }, /* unspecified */
	{ 104597, 132201, 25675, 53279 }, /* reserved */
	{ 104448, 132798, 24759, 53109 }, /* FCC */
	{ 104597, 132201, 25675, 53279 }, /* ITU-R Rec. 624-4 System B, G */
	{ 104597, 132201, 25675, 53279 }, /* SMPTE 170M */
	{ 117579, 136230, 16907, 35559 }  /* SMPTE 240M (1987) */
};
//#define YUV2RGBFUNC(yuva2argb_c, uint32_t, 1)
//static int yuva2argb_c(SwsContext *c, const uint8_t * src[], 
//				int srcStride[], int srcSliceY, int srcSliceH, uint8_t *dst[], int dstStride[])
//{
//	int y;
//	//没有透明度 alpha 或 YUV422P格式，则 U,V的 linesize * 2
//	if(!alpha && c->srcFormat == PIX_FMT_YUV422P){
//		srcStride[1] *= 2;
//		srcStride[2] *= 2;
//	}
//	for(y = 0; y < srcSliceH; y += 2){
//		//dst_1 
//		uint32_t *dst_1 = (uint32_t)(dst[0] + (y + srcSliceY) * dstStride[0]);
//		//dst_2
//		uint32_t *dst_2 = (uint32_t)(dst[0] + (y + srcSliceY + 1) * dstStride[0]);
//		
//		uint32_t av_unused *r, *g, *b;
//		//第y，y+1行的Y行开头指针，第y/2行的U，V行的行开头指针
//		const uint8_t *py_1 = src[0] + y * srcStride[0];
//		const uint8_t *py_2 = py_1   +     srcStride[0];
//		const uint8_t *pu   = src[1] + (y >> 1) * srcStride[1];
//		const uint8_t *pv	= src[2] + (y >> 2) * srcStride[2];
//		
//		const uint8_t av_unused *pa_1, *pa_2;
//		//1280 / 8
//		unsigned int h_size = c->dstW >> 3;
//		if(alpha) {	//源数据alpha行
//			pa_1 = src[3] + y * srcStride[3];
//			pa_2 = pa_1	  +		srcStride[3];
//		}
//		
//		while(h_size--){
//			int av_unused U, V, Y;
////#define LOADCHROMA(0);
////查表操作
//			U = pu[0];
//			V = pv[0];
//			r = (void *)c->table_rV[V + YUVRGB_TABLE_HEADROOM];
//			g = (void *)(c->table_gU[U+YUVRGB_TABLE_HEADROOM] + c->table_gV[V+YUVRGB_TABLE_HEADROOM]);
//			b = (void *)c->table_bU[U+YUVRGB_TABLE_HEADROOM];
////PUTRGBA(dst_1, py_1, pa_1, 0, 0);
////将查表结果赋值给输出数据
//			Y 				 = py_1[2 * 0];
//			dst_1[2 * 0] 	 = r[Y] + g[Y] + b[Y] + (pa_1[2 * 0] << 0);
//			Y				 = py_1[2 * 0];
//			dst_1[2 * 0 + 1] = r[Y] + g[Y] + b[Y] + (pa_1[2 * 0 + 1] << 0);
////PUTRGBA(dst_2, py_2, pa_2, 0, 0);
//			Y 				 = py_2[2 * 0];
//			dst_2[2 * 0] 	 = r[Y] + g[Y] + b[Y] + (pa_2[2 * 0] << 0);
//			Y				 = py_2[2 * 0];
//			dst_2[2 * 0 + 1] = r[Y] + g[Y] + b[Y] + (pa_2[2 * 0 + 1] << 0);
//		    
//		    LOADCHROMA(1);
//    		PUTRGBA(dst_2, py_2, pa_2, 1, 0);
//    		PUTRGBA(dst_1, py_1, pa_1, 1, 0);
//
//    		LOADCHROMA(2);
//    		PUTRGBA(dst_1, py_1, pa_1, 2, 0);
//    		PUTRGBA(dst_2, py_2, pa_2, 2, 0);
//
//    		LOADCHROMA(3);
//    		PUTRGBA(dst_2, py_2, pa_2, 3, 0);
//    		PUTRGBA(dst_1, py_1, pa_1, 3, 0); 
//    		
//    		pa_1 += 8;
//    		pa_2 += 8;      
////ENDYUV2RGBLINE(8, 0)
//			pu	  += 4 >> 0;
//			pv	  += 4 >> 0;
//			py_1  += 8 >> 0;
//			py_2  += 8 >> 0;
//			dst_1 += 8 >> 0;
//			dst_2 += 8 >> 0;
//		}
//		//dstW % 8 >= 4
//		if(c->dstW & (4 >> 0)) {
//			int av_unused Y, U, V;
////
//			                                                             
//			LOADCHROMA(0);
//    		PUTRGBA(dst_1, py_1, pa_1, 0, 0);
//  			PUTRGBA(dst_2, py_2, pa_2, 0, 0);
//
//    		LOADCHROMA(1);
//    		PUTRGBA(dst_2, py_2, pa_2, 1, 0);
//    		PUTRGBA(dst_1, py_1, pa_1, 1, 0);
//    		pa_1 += 4;
//    		pa_2 += 4;
//			ENDYUV2RGBLINE(8, 1)
//    		LOADCHROMA(0);
//    		PUTRGBA(dst_1, py_1, pa_1, 0, 0);
//    		PUTRGBA(dst_2, py_2, pa_2, 0, 0);
////ENDYUV2RGBFUNC()
//		}
//	}
//	return srcSliceH;
//}	

//转换函数宏定义
#define LOADCHROMA(i)                               \
    U = pu[i];                                      \
    V = pv[i];                                      \
    r = (void *)c->table_rV[V+YUVRGB_TABLE_HEADROOM];                     \
    g = (void *)(c->table_gU[U+YUVRGB_TABLE_HEADROOM] + c->table_gV[V+YUVRGB_TABLE_HEADROOM]);  \
    b = (void *)c->table_bU[U+YUVRGB_TABLE_HEADROOM];
    
#define PUTRGBA(dst, ysrc, asrc, i, s)                                  \
    Y              = ysrc[2 * i];                                       \
    dst[2 * i]     = r[Y] + g[Y] + b[Y] + (0xFF     << s);       \
    Y              = ysrc[2 * i + 1];                                   \
    dst[2 * i + 1] = r[Y] + g[Y] + b[Y] + (0xFF		<< s);

#define YUV2RGBFUNC(func_name, dst_type, alpha)                             \
    int func_name(MySwsContext *c, const uint8_t *src[],               \
                         int srcStride[], int srcSliceY, int srcSliceH,     \
                         uint8_t *dst[], int dstStride[])                   \
    {                                                                       \
        int y;                                                              \
                                                                            \
        if (!alpha && c->srcFormat == PIX_FMT_YUV422P) {                    \
            srcStride[1] *= 2;                                              \
            srcStride[2] *= 2;                                              \
        }                                                                   \
        for (y = 0; y < srcSliceH; y += 2) {                                \
            dst_type *dst_1 =                                               \
                (dst_type *)(dst[0] + (y /*+ srcSliceY*/)     * dstStride[0]);  \
            dst_type *dst_2 =                                               \
                (dst_type *)(dst[0] + (y /*+ srcSliceY*/ + 1) * dstStride[0]);  \
            dst_type av_unused *r, *g, *b;                                  \
            const uint8_t *py_1 = src[0] +  y       * srcStride[0];         \
            const uint8_t *py_2 = py_1   +            srcStride[0];         \
            const uint8_t *pu   = src[1] + (y >> 1) * srcStride[1];         \
            const uint8_t *pv   = src[2] + (y >> 1) * srcStride[2];         \
            const uint8_t av_unused *pa_1, *pa_2;                           \
            unsigned int h_size = c->dstW >> 3;                             \
            if (alpha) {                                                    \
                pa_1 = src[3] + y * srcStride[3];                           \
                pa_2 = pa_1   +     srcStride[3];                           \
            }                                                               \
            while (h_size--) {                                              \
                int av_unused U, V, Y;                                      \
                
#define ENDYUV2RGBLINE(dst_delta, ss)               \
    pu    += 4 >> ss;                               \
    pv    += 4 >> ss;                               \
    py_1  += 8 >> ss;                               \
    py_2  += 8 >> ss;                               \
    dst_1 += dst_delta >> ss;                       \
    dst_2 += dst_delta >> ss;                       \
    }                                               \
    if (c->dstW & (4 >> ss)) {                      \
        int av_unused Y, U, V;                      \
        
#define ENDYUV2RGBFUNC()                            \
            }                                       \
        }                                           \
        return srcSliceH;                           \
    }

#define CLOSEYUV2RGBFUNC(dst_delta)                 \
    ENDYUV2RGBLINE(dst_delta, 0)                    \
    ENDYUV2RGBFUNC()

//转换函数宏定义结束

//转换函数体
YUV2RGBFUNC(yuv2argb_c, uint32_t, 0)
    LOADCHROMA(0);
    PUTRGBA(dst_1, py_1, pa_1, 0, 0);
    PUTRGBA(dst_2, py_2, pa_2, 0, 0);

    LOADCHROMA(1);
    PUTRGBA(dst_2, py_2, pa_2, 1, 0);
    PUTRGBA(dst_1, py_1, pa_1, 1, 0);

    LOADCHROMA(2);
    PUTRGBA(dst_1, py_1, pa_1, 2, 0);
    PUTRGBA(dst_2, py_2, pa_2, 2, 0);

    LOADCHROMA(3);
    PUTRGBA(dst_2, py_2, pa_2, 3, 0);
    PUTRGBA(dst_1, py_1, pa_1, 3, 0);
    //pa_1 += 8;
    //pa_2 += 8;
ENDYUV2RGBLINE(8, 0)
    LOADCHROMA(0);
    PUTRGBA(dst_1, py_1, pa_1, 0, 0);
    PUTRGBA(dst_2, py_2, pa_2, 0, 0);

    LOADCHROMA(1);
    PUTRGBA(dst_2, py_2, pa_2, 1, 0);
    PUTRGBA(dst_1, py_1, pa_1, 1, 0);
 /*   pa_1 += 4;
    pa_2 += 4;*/
ENDYUV2RGBLINE(8, 1)
    LOADCHROMA(0);
    PUTRGBA(dst_1, py_1, pa_1, 0, 0);
    PUTRGBA(dst_2, py_2, pa_2, 0, 0);
ENDYUV2RGBFUNC()
//转换函数体结束

//工具函数：取64位数倒数第2块16位数，四舍五入
static uint16_t roundToInt16(int64_t f)
{
    int r = (f + (1 << 15)) >> 16;

    if (r < -0x7FFF)
        return 0x8000;
    else if (r > 0x7FFF)
        return 0x7FFF;
    else
        return r;
}
//初始化列表，较高层用于判断参数
int yuv2rgb_c_init_tables(MySwsContext *c, const int inv_table[4],
                                     int fullRange, int brightness,
                                     int contrast, int saturation)
{
	const int isRgb = 1;
	const int isNotNe = 0;
	const int bpp = c->dstFormatBpp;
	
	uint8_t *y_table;
	uint16_t *y_table16;
	uint32_t *y_table32;
	int i, base, rbase, gbase, bbase, abase, needAlpha;
	const int yoffs = fullRange ? 384 : 326;
	
	int64_t crv = inv_table[0];		//inv_table = ff_yuv2rgb_coeffs[5]
	int64_t cbu = inv_table[1];
	int64_t cgu = -inv_table[2];
	int64_t cgv = -inv_table[3];
	int64_t cy = 1 << 16;
	int64_t oy = 0;
	int64_t yb = 0;
	
	if(!fullRange) {	//fullRange = srcRange = 1
		cy = (cy * 255)/219;
		oy = 16 << 16;
	} else {
		crv = (crv * 224) / 255;
		cbu = (cbu * 224) / 255;
		cgu = (cgu * 224) / 255;
		cgv = (cgv * 224) / 255;
	}
	//根据对比度和饱和度调整
	//brightness = 0; contrast = 1 << 16; saturation = 1 << 16
	cy   = (cy  * contrast)              >> 16;
    crv  = (crv * contrast * saturation) >> 32;
    cbu  = (cbu * contrast * saturation) >> 32;
    cgu  = (cgu * contrast * saturation) >> 32;
    cgv  = (cgv * contrast * saturation) >> 32;
    oy  -= 256 * brightness;
    
//    c->uOffset = 0x0400040004000400LL;
//    c->vOffset = 0x0400040004000400LL;
//    c->yCoeff  = roundToInt16(cy  * 8192) * 0x0001000100010001ULL;
//    c->vrCoeff = roundToInt16(crv * 8192) * 0x0001000100010001ULL;
//    c->ubCoeff = roundToInt16(cbu * 8192) * 0x0001000100010001ULL;
//    c->vgCoeff = roundToInt16(cgv * 8192) * 0x0001000100010001ULL;
//    c->ugCoeff = roundToInt16(cgu * 8192) * 0x0001000100010001ULL;
//    c->yOffset = roundToInt16(oy  *    8) * 0x0001000100010001ULL;
    
    //scale coefficients by cy
    crv = ((crv << 16) + 0x8000) / FFMAX(cy, 1);
    cbu = ((cbu << 16) + 0x8000) / FFMAX(cy, 1);
    cgu = ((cgu << 16) + 0x8000) / FFMAX(cy, 1);
    cgv = ((cgv << 16) + 0x8000) / FFMAX(cy, 1);
    
    //av_freep(&c->yuvTable);
    
#define ALLOC_YUV_TABLE(x)          \
    c->yuvTable = av_malloc(x); \
    if (!c->yuvTable)           \
        return -1;
    
    switch(bpp){
    	case 32:
    	case 64:
    		base = 0;
    		rbase = base + (isRgb ? 16 : 0);
    		gbase = base + 8;
    		bbase = base + (isRgb ? 0 : 16);
    		needAlpha = 0;
    		if(!needAlpha)
    			abase = (base + 24) & 31;
    		ALLOC_YUV_TABLE(1024 * 3 * 4);
    		y_table32 = c->yuvTable;
    		yb = -(384 << 16) - oy;
    		for (i = 0; i < 1024; i++) {
           		unsigned yval       = clip_uint8((yb + 0x8000) >> 16);
    			y_table32[i]        = (yval << rbase) +
                                  (needAlpha ? 0 : (255u << abase));
            	y_table32[i + 1024] =  yval << gbase;
            	y_table32[i + 2048] =  yval << bbase;
            	yb += cy;
            }
            fill_table(c->table_rV, 4, crv, y_table32 + yoffs);
        	fill_table(c->table_gU, 4, cgu, y_table32 + yoffs + 1024);
        	fill_table(c->table_bU, 4, cbu, y_table32 + yoffs + 2048);
        	
        	fill_gv_table(c->table_gV, 4, cgv);
        	break;
        default:
        	return -1;
        }
    return 0;
}

//获取转换参数集
MySwsContext * sws_getContext(int srcW, int srcH, enum PixelFormat srcFormat, int dstW, int dstH, enum PixelFormat dstFormat)
{
	MySwsContext *c = av_malloc(sizeof(MySwsContext));
	if(!c)
		return NULL;
	
	c->srcW = srcW;
	c->srcW      = srcW;
    c->srcH      = srcH;
    c->dstW      = dstW;
    c->dstH      = dstH;
    c->srcFormat = srcFormat;
    c->dstFormat = dstFormat;
    
    switch(srcFormat){
    case PIX_FMT_YUV420P:
    	c->srcFormatBpp = 12;
    	c->srcRange = 1;
    	break;
    default:
    	c->srcFormatBpp = 12;
    	c->srcRange = 0;

    }
    
    switch(dstFormat){
    case PIX_FMT_ARGB:
    	c->dstFormatBpp = 32;
    	c->dstRange = 0;
    	break;
    default:
    	c->dstFormatBpp = 32;
    	c->dstRange = 0;
    }
    
    if(init_MySwsContext(c) < 0) {
    	free_MySwsContext(c);
    	return NULL;
    }
    
    return c;
}

//初始化MySwsContext
int init_MySwsContext(MySwsContext *c)
{
//	  int srcW              = c->srcW;
//    int srcH              = c->srcH;
//    int dstW              = c->dstW;
//    int dstH              = c->dstH;
//	
//	enum AVPixelFormat srcFormat = c->srcFormat;
//    enum AVPixelFormat dstFormat = c->dstFormat;
    int ret = 0;
    
    ret = yuv2rgb_c_init_tables(c, ff_yuv2rgb_coeffs[5], c->srcRange, 0, 1 << 16, 1 << 16);
    
    return ret;
}
    
//释放Context
void free_MySwsContext(MySwsContext *c)
{
	
	if(!c)
		return;
	free(&c->yuvTable);
	free(c);
}

void fill_table(uint8_t* table[256 + 2 * YUVRGB_TABLE_HEADROOM], const int elemsize,
	const int64_t inc, void *y_tab)
{
	int i;
	uint8_t *y_table = y_tab;

	y_table -= elemsize * (inc >> 9);

	for (i = 0; i < 256 + 2 * YUVRGB_TABLE_HEADROOM; i++) {
		int64_t cb = clip(i - YUVRGB_TABLE_HEADROOM, 0, 255)*inc;
		table[i] = y_table + elemsize * (cb >> 16);
	}
}

void fill_gv_table(int table[256 + 2 * YUVRGB_TABLE_HEADROOM], const int elemsize, const int64_t inc)
{
	int i;
	int off = -(inc >> 9);

	for (i = 0; i < 256 + 2 * YUVRGB_TABLE_HEADROOM; i++) {
		int64_t cb = clip(i - YUVRGB_TABLE_HEADROOM, 0, 255)*inc;
		table[i] = elemsize * (off + (cb >> 16));
	}
}