# LargeImage
Android 加载大图  可以高清显示10000*10000像素的图片  
可以滑动，放大缩小具有PhotoView的效果  
普通图片也可以用它展示
#Gradle

 	 compile 'com.shizhefei:LargeImageView:1.0.7'

Download Demo [apk](raw/LargeImage.apk)

#效果

![image](raw/demo.gif)


#使用方法

	<com.shizhefei.view.largeimage.LargeImageView
	    android:id="@+id/imageView"
	    android:scrollbars="vertical|horizontal"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent" />

代码

	largeImageView = (LargeImageView) findViewById(R.id.imageView);

	//通过文件的方式加载sd卡中的大图
    largeImageView.setImage(new FileBitmapDecoderFactory(file));

    //通过流的方式加载assets文件夹里面的大图
    largeImageView.setImage(new InputStreamBitmapDecoderFactory(getAssets().open(ss[position])))

    //加载普通大小图片
	largeImageView.setImage(R.drawable.cat);
    largeImageView.setImage(drawable);
    largeImageView.setImage(bitmap);

支持的事件

        largeImageView.setOnClickListener(onClickListener);
        largeImageView.setOnLongClickListener(onLongClickListener);
设置是否可以缩放

     largeImageView.setEnabled(true);

Hook临界值(不设置的话会使用默认的计算缩放最小倍数和最大倍数)


    /**
     * Hook临界值
     */
    public interface CriticalScaleValueHook {

        /**
         * 返回最小的缩放倍数
         * scale为1的话表示，显示的图片和View一样宽
         *
         * @param largeImageView
         * @param imageWidth
         * @param imageHeight
         * @param suggestMinScale 默认建议的最小的缩放倍数
         * @return
         */
        float getMinScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMinScale);

        /**
         * 返回最大的缩放倍数
         * scale为1的话表示，显示的图片和View一样宽
         *
         * @param largeImageView
         * @param imageWidth
         * @param imageHeight
         * @param suggestMaxScale 默认建议的最大的缩放倍数
         * @return
         */
        float getMaxScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMaxScale);

    }

例如

       largeImageView.setCriticalScaleValueHook(new LargeImageView.CriticalScaleValueHook() {
            @Override
            public float getMinScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMinScale) {
                return 1;
            }

            @Override
            public float getMaxScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMaxScale) {
                return 4;
            }
        });


加载网络的图片，先下载本地，再通过加载图片的文件  
比如glide加载图片，具体代码查看demo

        String url = "http://img.tuku.cn/file_big/201502/3d101a2e6cbd43bc8f395750052c8785.jpg";
        Glide.with(this).load(url).downloadOnly(new ProgressTarget<String, File>(url, null) {
            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                ringProgressBar.setVisibility(View.VISIBLE);
                ringProgressBar.setProgress(0);
            }

            @Override
            public void onProgress(long bytesRead, long expectedLength) {
                int p = 0;
                if (expectedLength >= 0) {
                    p = (int) (100 * bytesRead / expectedLength);
                }
                ringProgressBar.setProgress(p);
            }

            @Override
            public void onResourceReady(File resource, GlideAnimation<? super File> animation) {
                super.onResourceReady(resource, animation);
                ringProgressBar.setVisibility(View.GONE);
                largeImageView.setImage(new FileBitmapDecoderFactory(resource));
            }

            @Override
            public void getSize(SizeReadyCallback cb) {
                cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            }
        });


#实现原理

只加载显示的区域的图片，切成小块拼接.  

#LargeImageView 
根据滚动和缩放事件 scrollTo 对应的位置，计算当前显示区域的图片绘制出来

#UpdateImageView
监听View的显示区域的变化，然后加载显示区域内应该显示的图片区域，然后绘制到View上  
UpdateView负责监听显示区域的变化的View，子类通过重写onUpdateWindow(Rect visiableRect)监听显示区域，大部分代码源于SurfaceView监听代码

###BlockImageLoader负责加载显示区域的图片块。   ###


###实现细节：  ###
每次LargeImageView的onDraw方法都会调用ImageManagerd的getDrawData(float imageScale, Rect imageRect)方法，imageRect为在View上图片显示的区域(需要加载的图片区域)，imageScale 假设等于4的话，就是View上显示1像素，image要加载4个像素的区域（缩小4倍的图片）  
getDrawData(float imageScale, Rect imageRect)实现细节：  
手势移动，图片显示区域会变化。比如显示区域是800*800，向右移动2像素，难道要重新加载800*800的图片区域？
所以我采用了图片切块的操作,分块的优化  



1. 比如图片显示比例是1，那么要横向分多少份才，纵向分多少分，才合理？图片显示比例是4，横向分多少份才，纵向分多少分，才合理。
-  
 
所以我采用了基准块（图片比例是1，一个图片块的宽高的合理sise） 
BASE_BLOCKSIZE = context.getResources().getDisplayMetrics().heightPixels / 2+1;  
图片缩放比例为1的话，图片块宽高是BASE_BLOCKSIZE  
图片缩放比例为4的话，图片块宽高是4*BASE_BLOCKSIZE  
图片没被位移，那么屏幕上显示横向2列，纵向getDisplayMetrics().heightPixels/BASE_BLOCKSIZE行  


2.因为手势放大缩小操作要加载不同清晰度的图片区域，比如之前的图片缩放是4，现在缩放是4.2，难道要重新加载？ 
-  
通过public int getNearScale(float imageScale)方法计算趋于2的指数次方的值（1，2，4，8，16）  
比如3.9和4.2和4比较接近，就直接加载图片显示比例为4的图片块  

3.之前没加载的区域，难道要空白显示么？
- 
为了避免加载出现白色块，我会缓存当前比例的加载的图片块，以及2倍比例的图片块（之前加载过，并且当前还属于当前显示区域的，如果不是的话也不缓存它）
所以发现没有的话去拿其他比例的图片区去显示

4.难道只加载显示区域？
- 
当然，还会去加载旁边一部分没显示的区域的图片块


5.onDraw方法是UI线程,调用getDrawData(float imageScale, Rect imageRect)加载图片块的方法怎么不卡住
-   

getDrawData只返回之前加载过的图片块，而没有加载的是通过LoadHandler.sendMessage去加载
LoadHandler的Loop是通过HandlerThread线程创建的Loop，也就是开个线程加载. 
   
每加载一个图片块通过	onImageLoadListenner.onBlockImageLoadFinished();onDraw重绘  
onDraw又调用getDrawData加载，直至需要显示的图片块加载完成


##主力类库##

**1.https://github.com/LuckyJayce/ViewPagerIndicator**  
Indicator 取代 tabhost，实现网易顶部tab，新浪微博主页底部tab，引导页，无限轮播banner等效果，高度自定义tab和特效

**2.https://github.com/LuckyJayce/MVCHelper**  
实现下拉刷新，滚动底部自动加载更多，分页加载，自动切换显示网络失败布局，暂无数据布局，支持任意view，支持切换主流下拉刷新框架。

**3.https://github.com/LuckyJayce/MultiTypeView**  
简化RecyclerView的多种type的adapter，Fragment可以动态添加到RecyclerView上，实现复杂的界面分多个模块开发

**4.https://github.com/LuckyJayce/EventBus**  
事件总线，通过动态代理接口的形式发布,接收事件。定义一个接口把事件发给注册并实现接口的类

**5.https://github.com/LuckyJayce/LargeImage**  
大图加载，可供学习

**6.https://github.com/LuckyJayce/GuideHelper**  
新手引导页，轻松的实现对应的view上面的显示提示信息和展示功能给用户  

**7.https://github.com/LuckyJayce/HVScrollView**  
可以双向滚动的ScrollView，支持嵌套ScrollView联级滑动，支持设置支持的滚动方向

**8.https://github.com/LuckyJayce/CoolRefreshView**  
  下拉刷新RefreshView，支持任意View的刷新 ，支持自定义Header，支持NestedScrollingParent,NestedScrollingChild的事件分发，嵌套ViewPager不会有事件冲突 

有了这些类库，让你6的飞起


# 说明   
其中 android-gesture-detectors-lib 手势类库  
源地址https://github.com/Almeros/android-gesture-detectors   

# 联系方式和问题建议

* 微博:http://weibo.com/u/3181073384
* QQ 群: 开源项目使用交流，问题解答: 549284336（开源盛世） 

License
=======

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
