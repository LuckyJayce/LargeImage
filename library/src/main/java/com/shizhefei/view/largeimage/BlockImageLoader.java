/*
Copyright 2015 shizhefei（LuckyJayce）
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
   http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.shizhefei.view.largeimage;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.shizhefei.view.largeimage.factory.BitmapDecoderFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockImageLoader {

    private final int BASE_BLOCKSIZE;

    private Context context;

    private Handler mainHandler;

    public BlockImageLoader(Context context) {
        super();
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        // width = getNearScale(width);
        int size = context.getResources().getDisplayMetrics().heightPixels / 2 + 1;
        BASE_BLOCKSIZE = size + (size % 2 == 0 ? 0 : 1);
    }

    public void setOnImageLoadListener(OnImageLoadListener onImageLoadListener) {
        this.onImageLoadListener = onImageLoadListener;
    }

    /**
     * 获取接近的2的乘方的数 , 1，2，4，8这种数字<br>
     * 比如7.5 那么最接近的是8<br>
     * 比如5.5 那么最接近的是4<br>
     *
     * @param imageScale
     * @return
     */
    public int getNearScale(float imageScale) {
        int scale = (int) imageScale;
        int startS = 1;
        if (scale > 2) {
            do {
                startS *= 2;
            } while ((scale = scale / 2) > 2);
        }
        if (Math.abs(startS - imageScale) < Math.abs(startS * 2 - imageScale)) {
            scale = startS;
        } else {
            scale = startS * 2;
        }
        return scale;
    }

    private HandlerThread handlerThread;
    private LoadHandler handler;

    public boolean hasLoad() {
        LoadData loadData = mLoadData;
        return loadData != null && loadData.mDecoder != null;
    }

    private Rect madeRect(Bitmap bitmap, int row, int col, int scaleKey, float imageScale) {
        int size = scaleKey * BASE_BLOCKSIZE;
        Rect rect = new Rect();
        rect.left = (col * size);
        rect.top = (row * size);
        rect.right = rect.left + bitmap.getWidth() * scaleKey;
        rect.bottom = rect.top + bitmap.getHeight() * scaleKey;
        return rect;
    }

    /**
     * @param imageScale 图片width/屏幕width 的值。也就是说如果imageScale = 4，表示图片的400像素用来当做显示100像素<br>
     *                   imageScale越大一个单位点position包含的blockSize的正方形图片真实像素就越多<br>
     *                   （blockSize*imageScale的图片实际像素,
     *                   imageScale也可以理解为一个position点拥有的blockSize的正方形个数）
     * @param imageRect  在View上图片显示的区域(需要加载的图片区域)
     * @return
     */
    public List<DrawData> getDrawData(float imageScale, Rect imageRect) {
        //  为什么要把图片切成一块一块的，而不是直接加载整张图?
        //为了避免滚动手势导致图片持续加载大的图片，。比如显示区域是800800，向右移动2像素，难道要重新加载800800的图片区域。 而选择分成小份之前显示现在还要显示的那部分就不用重新加载了。

        //获取图片数据信息。start(OnImageLoadListener invalidateListener)会去调用线程用BitmapRegionDecoder加载图片数据
        //如果还没有加载好，返回空列表
        LoadData loadData = mLoadData;
        if (loadData == null || loadData.mDecoder == null) {
            return new ArrayList<>(0);
        }
        //图片的宽高
        int imageWidth = loadData.mImageWidth;
        int imageHeight = loadData.mImageHeight;
        //CacheData 图片块列表，List<CacheData>各个缩放级别的图片块
        List<CacheData> cacheDatas = loadData.mCacheDatas;

        //完整图片的缩略图，用于一开始展示，避免一开始没有加载好图片块，导致空白
        Bitmap cacheImageData = loadData.mCacheImageData;
        //mCacheImageScale 完整图片缩略图的 缩放级别
        int cacheImageScale = loadData.mCacheImageScale;

        //方法要返回出去的图片绘制数据
        List<DrawData> drawDatas = new ArrayList<>();

        //避免图片区域超出图片实际大小
        if (imageRect.left < 0) {
            imageRect.left = 0;
        }
        if (imageRect.top < 0) {
            imageRect.top = 0;
        }
        if (imageRect.right > loadData.mImageWidth) {
            imageRect.right = loadData.mImageWidth;
        }
        if (imageRect.bottom > loadData.mImageHeight) {
            imageRect.bottom = loadData.mImageHeight;
        }

        if (cacheImageData == null) {
            //加载完整图片的缩略图
            try {
                int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
                int s = (int) Math.sqrt(1.0f * imageWidth * imageHeight / (screenWidth / 2) / (screenHeight / 2));
                cacheImageScale = getNearScale(s);
                if (cacheImageScale < s) {
                    cacheImageScale *= 2;
                }
                handler.sendMessage(handler.obtainMessage(MESSAGE_PIC, cacheImageScale));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //如果有缩略图，只绘制缩略图的显示区域
            Rect cacheImageRect = new Rect(imageRect);
            int cache = dip2px(context, 100);
            cache = (int) (cache * imageScale);
            cacheImageRect.right += cache;
            cacheImageRect.top -= cache;
            cacheImageRect.left -= cache;
            cacheImageRect.bottom += cache;

            if (cacheImageRect.left < 0) {
                cacheImageRect.left = 0;
            }
            if (cacheImageRect.top < 0) {
                cacheImageRect.top = 0;
            }
            if (cacheImageRect.right > imageWidth) {
                cacheImageRect.right = imageWidth;
            }
            if (cacheImageRect.bottom > imageHeight) {
                cacheImageRect.bottom = imageHeight;
            }
            Rect r = new Rect();
            r.left = (int) Math.abs(1.0f * cacheImageRect.left / cacheImageScale);
            r.right = (int) Math.abs(1.0f * cacheImageRect.right / cacheImageScale);
            r.top = (int) Math.abs(1.0f * cacheImageRect.top / cacheImageScale);
            r.bottom = (int) Math.abs(1.0f * cacheImageRect.bottom / cacheImageScale);
            drawDatas.add(new DrawData(cacheImageData, r, cacheImageRect));
        }
        //根据外部传进来的缩放尺寸转化成2的指数次方的值
        //因为手势放大缩小操作要加载不同清晰度的图片区域，比如之前的图片缩放是4，现在缩放是4.2，难道要重新加载？
        //通过public int getNearScale(float imageScale)方法计算趋于2的指数次方的值（1，2，4，8，16）
        // 比如3.9和4.2和4比较接近，就直接加载图片显示比例为4的图片块
        int scale = getNearScale(imageScale);

        //如果缩略图的清晰够用，就不需要去加载图片块，直接画缩略图就好啦
        if (cacheImageScale <= scale && cacheImageData != null) {
            return drawDatas;
        }

        // 横向分多少份，纵向分多少分，才合理？加载的图片块宽高像素多少？
//        我采用了基准块（图片比例是1，一个图片块的宽高的sise） BASEBLOCKSIZE = context.getResources().getDisplayMetrics().widthPixels / 2;
//        图片缩放比例为1的话，图片块宽高是BASEBLOCKSIZE
//        图片缩放比例为4的话，图片块宽高是4*BASEBLOCKSIZE
//        图片没被位移，那么屏幕上显示横向2列，纵向getDisplayMetrics().heightPixels/BASEBLOCKSIZE行

        int blockSize = BASE_BLOCKSIZE * scale;

        //计算完整图片被切分图片块的最大行数，和最大列数
        int maxRow = imageHeight / blockSize + (imageHeight % blockSize == 0 ? 0 : 1);
        int maxCol = imageWidth / blockSize + (imageWidth % blockSize == 0 ? 0 : 1);

        // 该scale下对应的position范围
        int startRow = imageRect.top / blockSize + (imageRect.top % blockSize == 0 ? 0 : 1) - 1;
        int endRow = imageRect.bottom / blockSize + (imageRect.bottom % blockSize == 0 ? 0 : 1);
        int startCol = imageRect.left / blockSize + (imageRect.left % blockSize == 0 ? 0 : 1) - 1;
        int endCol = imageRect.right / blockSize + (imageRect.right % blockSize == 0 ? 0 : 1);
        if (startRow < 0) {
            startRow = 0;
        }
        if (startCol < 0) {
            startCol = 0;
        }
        if (endRow > maxRow) {
            endRow = maxRow;
        }
        if (endCol > maxCol) {
            endCol = maxCol;
        }

        //加载旁边一部分没显示的区域的图片块,因此上下左右都多加载了一部分的图片块
        int cacheStartRow = startRow - 1;
        int cacheEndRow = endRow + 1;
        int cacheStartCol = startCol - 1;
        int cacheEndCol = endCol + 1;
        if (cacheStartRow < 0) {
            cacheStartRow = 0;
        }
        if (cacheStartCol < 0) {
            cacheStartCol = 0;
        }
        if (cacheEndRow > maxRow) {
            cacheEndRow = maxRow;
        }
        if (cacheEndCol > maxCol) {
            cacheEndCol = maxCol;
        }

        //需要加载的图片块的坐标，Position其实就是[row,col]
        Set<Position> needShowPositions = new HashSet<Position>();

        // 移除掉之前的任务
        handler.removeMessages(preMessageWhat);
        int what = preMessageWhat == MESSAGE_BLOCK_1 ? MESSAGE_BLOCK_2 : MESSAGE_BLOCK_1;
        preMessageWhat = what;

        //如果当前的图片缩放切块列表不为空并且不是需要的缩放级别
        if (loadData.mCurrentCacheData != null && loadData.mCurrentCacheData.scale != scale) {
            //就把当前的缩放切块放在缓存列表中
            cacheDatas.add(new CacheData(loadData.mCurrentCacheData.scale, new HashMap<Position, Bitmap>(loadData.mCurrentCacheData.images)));
            loadData.mCurrentCacheData = null;
        }
        //如果当前的图片缩放切块列表是null,那就从列表中根据scale取
        if (loadData.mCurrentCacheData == null) {
            Iterator<CacheData> iterator = cacheDatas.iterator();
            while (iterator.hasNext()) {
                CacheData cacheData = iterator.next();
                if (scale == cacheData.scale) {
                    //这边用了ConcurrentHashMap，因为有多线程操作它
                    loadData.mCurrentCacheData = new CacheData(scale, new ConcurrentHashMap<Position, Bitmap>(cacheData.images));
                    iterator.remove();
                }
            }
        }
        //如果上面的列表中没取到，就新建一个
        if (loadData.mCurrentCacheData == null) {
            loadData.mCurrentCacheData = new CacheData(scale, new ConcurrentHashMap<Position, Bitmap>());
            //通知handler去加载所有需要的图片切块
            for (int row = startRow; row < endRow; row++) {
                for (int col = startCol; col < endCol; col++) {
                    Position position = new Position(row, col);
                    needShowPositions.add(position);
                    handler.sendMessage(handler.obtainMessage(what, new MessageData(position, scale)));
                }
            }

            //加载上下左右的旁边边的切块（虽然这边没有显示，但是做了个预先加载操作）

            /**
             * <pre>
             * #########  1
             * #       #
             * #       #
             * #########
             *
             * <pre>
             */

            // 上 #########
            for (int row = cacheStartRow; row < startRow; row++) {
                for (int col = cacheStartCol; col < cacheEndCol; col++) {
                    Position position = new Position(row, col);
                    handler.sendMessage(handler.obtainMessage(what, new MessageData(position, scale)));
                }
            }
            // 下 #########
            for (int row = endRow + 1; row < cacheEndRow; row++) {
                for (int col = cacheStartCol; col < cacheEndCol; col++) {
                    Position position = new Position(row, col);
                    handler.sendMessage(handler.obtainMessage(what, new MessageData(position, scale)));
                }
            }
            // # 左
            // #
            for (int row = startRow; row < endRow; row++) {
                for (int col = cacheStartCol; col < startCol; col++) {
                    Position position = new Position(row, col);
                    handler.sendMessage(handler.obtainMessage(what, new MessageData(position, scale)));
                }
            }
            // # 右
            // #
            for (int row = startRow; row < endRow; row++) {
                for (int col = endRow + 1; col < cacheEndRow; col++) {
                    Position position = new Position(row, col);
                    handler.sendMessage(handler.obtainMessage(what, new MessageData(position, scale)));
                }
            }
        } else {
            /*
             * 找出该scale所有存在的图片切块，和记录所有不存在的position
			 */
            Set<Position> usePositions = new HashSet<Position>();
            for (int row = startRow; row < endRow; row++) {
                for (int col = startCol; col < endCol; col++) {
                    Position position = new Position(row, col);
                    Bitmap bitmap = loadData.mCurrentCacheData.images.get(position);
                    if (bitmap == null) {
                        //记录没有加载到的图片块，再后面的代码有用到
                        needShowPositions.add(position);
                        handler.sendMessage(handler.obtainMessage(what, new MessageData(position, scale)));
                    } else {
                        usePositions.add(position);
                        Rect rect = madeRect(bitmap, row, col, scale, imageScale);
                        drawDatas.add(new DrawData(bitmap, null, rect));
                    }
                }
            }
            // 上 #########
            for (int row = cacheStartRow; row < startRow; row++) {
                for (int col = cacheStartCol; col < cacheEndCol; col++) {
                    Position position = new Position(row, col);
                    usePositions.add(position);
                    handler.sendMessage(handler.obtainMessage(what, new MessageData(position, scale)));
                }
            }
            // 下 #########
            for (int row = endRow + 1; row < cacheEndRow; row++) {
                for (int col = cacheStartCol; col < cacheEndCol; col++) {
                    Position position = new Position(row, col);
                    usePositions.add(position);
                    handler.sendMessage(handler.obtainMessage(what, new MessageData(position, scale)));
                }
            }
            // # 左
            // #
            for (int row = startRow; row < endRow; row++) {
                for (int col = cacheStartCol; col < startCol; col++) {
                    Position position = new Position(row, col);
                    usePositions.add(position);
                    handler.sendMessage(handler.obtainMessage(what, new MessageData(position, scale)));
                }
            }
            // # 右
            // #
            for (int row = startRow; row < endRow; row++) {
                for (int col = endRow + 1; col < cacheEndRow; col++) {
                    Position position = new Position(row, col);
                    usePositions.add(position);
                    handler.sendMessage(handler.obtainMessage(what, new MessageData(position, scale)));
                }
            }
            //移除掉那些没被用到的缓存的图片块
            loadData.mCurrentCacheData.images.keySet().retainAll(usePositions);
        }

        if (!needShowPositions.isEmpty()) {
            //根据趋于scale进行排序，如果当前需要的缩放级别是4，如果列表是1，8，2，4.那么排序后就是  4，2，1，8
            //因为要取相近的缩放级别的缩略图，显示效果更好
            Collections.sort(cacheDatas, new NearComparator(scale));

            //下面的代码要做好心理准备才可观看!!!!!!!!!!!

            //大意就是：比如循环不同的缩放级别。获取图片块列表中needShowPositions。但是每个级别的图片块显示范围不一样。
            //比如4个缩放级别2的图片块显示的区域是  1个缩放级别4的一个图片块的区域。缩放级别图片块越大显示的区域越广，
            //所以下面都是当前缩放级别的Position转化各对应缩放级别的图片块的Position。

            //循环图片块列表的缩放级别列表
            Iterator<CacheData> iterator = cacheDatas.iterator();
            while (iterator.hasNext()) {
                CacheData cacheData = iterator.next();
                int scaleKey = cacheData.scale;
                if (scaleKey / scale == 2) {// 这里都是大于scale的.,缩小的图，单位区域大

                    int ds = scaleKey / scale;

                    // 单位图片的真实区域
                    int size = scale * BASE_BLOCKSIZE;

                    // 显示区域范围
                    int startRowKey = cacheStartRow / 2;
                    int endRowKey = cacheEndRow / 2;
                    int startColKey = cacheStartCol / 2;
                    int endColKey = cacheEndCol / 2;

                    Iterator<Entry<Position, Bitmap>> imageiterator = cacheData.images.entrySet().iterator();
                    while (imageiterator.hasNext()) {
                        Entry<Position, Bitmap> entry = imageiterator.next();
                        Position position = entry.getKey();
                        if (!(startRowKey <= position.row && position.row <= endRowKey && startColKey <= position.col && position.col <= endColKey)) {
                            imageiterator.remove();
                        }
                    }

                    Iterator<Entry<Position, Bitmap>> imagesIterator = cacheData.images.entrySet().iterator();
                    while (imagesIterator.hasNext()) {
                        Entry<Position, Bitmap> entry = imagesIterator.next();
                        Position position = entry.getKey();
                        int startPositionRow = position.row * ds;
                        int endPositionRow = startPositionRow + ds;
                        int startPositionCol = position.col * ds;
                        int endPositionCol = startPositionCol + ds;
                        Bitmap bitmap = entry.getValue();
                        int iW = bitmap.getWidth();
                        int iH = bitmap.getHeight();

                        // 单位图片的大小
                        int blockImageSize = (int) Math.ceil(1.0f * BASE_BLOCKSIZE / ds);

                        for (int row = startPositionRow, i = 0; row <= endPositionRow; row++, i++) {
                            int top = i * blockImageSize;
                            if (top >= iH) {
                                break;
                            }
                            for (int col = startPositionCol, j = 0; col <= endPositionCol; col++, j++) {
                                int left = j * blockImageSize;
                                if (left >= iW) {
                                    break;
                                }
                                if (needShowPositions.remove(new Position(row, col))) {
                                    int right = left + blockImageSize;
                                    int bottom = top + blockImageSize;
                                    if (right > iW) {
                                        right = iW;
                                    }
                                    if (bottom > iH) {
                                        bottom = iH;
                                    }
                                    Rect rect = new Rect();
                                    rect.left = col * size;
                                    rect.top = row * size;
                                    rect.right = rect.left + (right - left) * scaleKey;
                                    rect.bottom = rect.top + (bottom - top) * scaleKey;
                                    drawDatas.add(new DrawData(bitmap, new Rect(left, top, right, bottom), rect));
                                }
                            }
                        }
                    }
                } else if (scale / scaleKey == 2) {// 放大的图，单位区域小
                    int size = scaleKey * BASE_BLOCKSIZE;

                    int startRowKey = imageRect.top / size + (imageRect.top % size == 0 ? 0 : 1) - 1;
                    int endRowKey = imageRect.bottom / size + (imageRect.bottom % size == 0 ? 0 : 1);
                    int startColKey = imageRect.left / size + (imageRect.left % size == 0 ? 0 : 1) - 1;
                    int endColKey = imageRect.right / size + (imageRect.right % size == 0 ? 0 : 1);

                    Position tempPosition = new Position();
                    Iterator<Entry<Position, Bitmap>> imageiterator = cacheData.images.entrySet().iterator();
                    while (imageiterator.hasNext()) {
                        Entry<Position, Bitmap> entry = imageiterator.next();
                        Position position = entry.getKey();
                        if (!(startRowKey <= position.row && position.row <= endRowKey && startColKey <= position.col && position.col <= endColKey)) {
                            imageiterator.remove();
                        } else {
                            Bitmap bitmap = entry.getValue();
                            tempPosition.set(position.row / 2 + (position.row % 2 == 0 ? 0 : 1), position.col / 2 + (position.col % 2 == 0 ? 0 : 1));
                            if (needShowPositions.contains(tempPosition)) {
                                Rect rect = new Rect();
                                rect.left = position.col * size;
                                rect.top = position.row * size;
                                rect.right = rect.left + bitmap.getWidth() * scaleKey;
                                rect.bottom = rect.top + bitmap.getHeight() * scaleKey;
                                drawDatas.add(new DrawData(bitmap, null, rect));
                            }
                        }
                    }
                } else {
                    iterator.remove();
                }
            }
        }
        return drawDatas;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {// API
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight(); // earlier version
    }

    private static class MessageData {
        Position position;
        int scale;

        public MessageData(Position position, int scale) {
            super();
            this.position = position;
            this.scale = scale;
        }

    }

    private int preMessageWhat = 1;

    /**
     * 缓存快数据
     */
    private static class CacheData {
        //图片缩放级别
        int scale;
        //根据位置存放对应的缓存块的Map
        Map<Position, Bitmap> images;

        public CacheData(int scale, Map<Position, Bitmap> images) {
            super();
            this.scale = scale;
            this.images = images;
        }
    }

    private class NearComparator implements Comparator<CacheData> {
        private int scale;

        public NearComparator(int scale) {
            super();
            this.scale = scale;
        }

        @Override
        public int compare(CacheData lhs, CacheData rhs) {
            int dScale = Math.abs(scale - lhs.scale) - Math.abs(scale - rhs.scale);
            if (dScale == 0) {
                if (lhs.scale > rhs.scale) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (dScale < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public class DrawData {
        //绘制的图片
        public Bitmap bitmap;
        //绘制到View上的区域
        public Rect srcRect;
        //图片的区域
        public Rect imageRect;

        public DrawData(Bitmap bitmap, Rect srcRect, Rect imageRect) {
            super();
            this.bitmap = bitmap;
            this.srcRect = srcRect;
            this.imageRect = imageRect;
        }

    }

    /**
     * 图片块的位置
     */
    private static class Position {
        int row;
        int col;

        public Position() {
            super();
        }

        public Position(int row, int col) {
            super();
            this.row = row;
            this.col = col;
        }

        public Position set(int row, int col) {
            this.row = row;
            this.col = col;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Position) {
                Position position = (Position) o;
                return row == position.row && col == position.col;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int iTotal = 17;
            int iConstant = 37;
            iTotal = iTotal * iConstant + row;
            iTotal = iTotal * iConstant + col;
            return iTotal;
        }

        @Override
        public String toString() {
            return "row:" + row + " col:" + col;
        }
    }

    private OnImageLoadListener onImageLoadListener;

    public interface OnImageLoadListener {

        void onBlockImageLoadFinished();

        void onLoadImageSize(int imageWidth, int imageHeight);

        void onLoadFail(Exception e);
    }

    public void destroy() {
        if (handlerThread != null) {
            handlerThread.quit();
            handlerThread = null;
            handler = null;
        }
        mainHandler.removeCallbacksAndMessages(null);
        release(this.mLoadData);
    }

    public int getWidth() {
        return mLoadData == null ? 0 : mLoadData.mImageWidth;
    }

    public int getHeight() {
        return mLoadData == null ? 0 : mLoadData.mImageHeight;
    }

    private volatile LoadData mLoadData;

    public void load(BitmapDecoderFactory factory) {
        if (handlerThread == null) {
            handlerThread = new HandlerThread("111");
            handlerThread.start();
            handler = new LoadHandler(handlerThread.getLooper());
        }
        LoadData loadData = mLoadData;
        if (loadData != null && loadData.mFactory != null) {
            release(loadData);
        }
        this.mLoadData = new LoadData(factory);
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler.sendEmptyMessage(MESSAGE_LOAD);
        }
    }

    private void release(LoadData loadData) {
        if (loadData == null) {
            return;
        }
        if (loadData.mDecoder != null) {
            try {
                loadData.mDecoder.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
            loadData.mDecoder = null;
        }
    }

    public static final int MESSAGE_PIC = 665;
    public static final int MESSAGE_LOAD = 666;
    public static final int MESSAGE_BLOCK_1 = 1;
    public static final int MESSAGE_BLOCK_2 = 2;

    /**
     * 用于存放图片信息数据以及图片缓存块
     */
    private static class LoadData {
        public LoadData(BitmapDecoderFactory factory) {
            this.mFactory = factory;
        }

        /**
         * 当前的缩放的图片切块
         */
        private volatile CacheData mCurrentCacheData;
        /**
         * 保存显示区域中的各个缩放的图片切块
         */
        private List<CacheData> mCacheDatas = new LinkedList<CacheData>();

        /**
         * 完整图片的缩略图，用于一开始展示，避免一开始没有加载好图片块，导致空白
         */
        private volatile Bitmap mCacheImageData;
        /**
         * 完整图片的缩略图
         */
        private volatile int mCacheImageScale;
        private volatile int mImageHeight;
        private volatile int mImageWidth;
        private volatile BitmapDecoderFactory mFactory;
        private volatile BitmapRegionDecoder mDecoder;
    }

    private class LoadHandler extends Handler {

        public LoadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LoadData loadData = mLoadData;
            if (msg.what == MESSAGE_LOAD) {//start调用的一开始加载图片的图片信息
                if (loadData.mFactory != null) {
                    try {
                        loadData.mDecoder = loadData.mFactory.made();
                        loadData.mImageWidth = loadData.mDecoder.getWidth();
                        loadData.mImageHeight = loadData.mDecoder.getHeight();
                        final int imageWidth = loadData.mImageWidth;
                        final int imageHeight = loadData.mImageHeight;
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (onImageLoadListener != null) {
                                    onImageLoadListener.onLoadImageSize(imageWidth, imageHeight);
                                }
                            }
                        });
                    } catch (final IOException e) {
                        e.printStackTrace();
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (onImageLoadListener != null) {
                                    onImageLoadListener.onLoadFail(e);
                                }
                            }
                        });
                    }
                }
            } else if (msg.what == MESSAGE_PIC) { //加载完整图片的缩略图
                Integer scale = (Integer) msg.obj;
                Options decodingOptions = new Options();
                decodingOptions.inSampleSize = scale;
                try {
                    loadData.mCacheImageData = loadData.mDecoder.decodeRegion(new Rect(0, 0, loadData.mImageWidth, loadData.mImageHeight),
                            decodingOptions);
                    loadData.mCacheImageScale = scale;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (onImageLoadListener != null) {
                                onImageLoadListener.onBlockImageLoadFinished();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {//加载图片块
                MessageData data = (MessageData) msg.obj;
                CacheData cacheData = loadData.mCurrentCacheData;
                if (cacheData == null || cacheData.scale != data.scale) {
                    return;
                }
                Position position = data.position;
                Bitmap imageData = cacheData.images.get(position);
                // 不存在才需要加载，（这里避免之前的任务重复被执行）
                if (imageData == null) {
                    int imageBlockSize = BASE_BLOCKSIZE * data.scale;
                    int left = imageBlockSize * position.col;
                    int right = left + imageBlockSize;
                    int top = imageBlockSize * position.row;
                    int bottom = top + imageBlockSize;
                    if (right > loadData.mImageWidth) {
                        right = loadData.mImageWidth;
                    }
                    if (bottom > loadData.mImageHeight) {
                        bottom = loadData.mImageHeight;
                    }
                    Rect clipImageRect = new Rect(left, top, right, bottom);
                    Options decodingOptions = new Options();
                    decodingOptions.inSampleSize = data.scale;
                    // 加载clipRect的区域的图片块
                    try {
                        imageData = loadData.mDecoder.decodeRegion(clipImageRect, decodingOptions);
                        if (imageData != null) {
                            cacheData.images.put(position, imageData);
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (onImageLoadListener != null) {
                                        onImageLoadListener.onBlockImageLoadFinished();
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.d("nnnn", position.toString() + " " + clipImageRect.toShortString());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
