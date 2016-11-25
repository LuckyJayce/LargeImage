package com.example.lagerimage_test;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.example.lagerimage_test.glide.OkHttpProgressGlideModule;
import com.example.lagerimage_test.glide.ProgressTarget;
import com.shizhefei.view.largeimage.LargeImageView;
import com.shizhefei.view.largeimage.factory.FileBitmapDecoderFactory;

import java.io.File;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

import static android.R.attr.resource;


public class NetworkDemoActivity extends FragmentActivity {
    private LargeImageView largeImageView;
    private RingProgressBar ringProgressBar;
    private View clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networkdemo);
        largeImageView = (LargeImageView) findViewById(R.id.networkDemo_photoView);
        ringProgressBar = (RingProgressBar) findViewById(R.id.networkDemo_ringProgressBar);
        clearButton = findViewById(R.id.networkDemo_button);

        final Glide glide = Glide.get(this);
        OkHttpProgressGlideModule a = new OkHttpProgressGlideModule();
        a.registerComponents(this, glide);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "开始清除缓存", Toast.LENGTH_SHORT).show();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Glide.get(getApplicationContext()).clearDiskCache();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "清除缓存成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }.start();
            }
        });

        String url = "http://img.tuku.cn/file_big/201502/3d101a2e6cbd43bc8f395750052c8785.jpg";
        Glide.with(this).load(url).downloadOnly(new ProgressTarget<String, File>(url, null) {
            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                ringProgressBar.setVisibility(View.VISIBLE);
                ringProgressBar.setProgress(0);
//                Log.d("wsx", "onLoadStarted");
            }

            @Override
            public void onProgress(long bytesRead, long expectedLength) {
                int p = 0;
                if (expectedLength >= 0) {
                    p = (int) (100 * bytesRead / expectedLength);
                }
                Log.d("wsx", "onProgress p：" + p + " expectedLength：" + expectedLength);
                ringProgressBar.setProgress(p);
            }

            @Override
            public void onResourceReady(File resource, GlideAnimation<? super File> animation) {
                super.onResourceReady(resource, animation);
                ringProgressBar.setVisibility(View.GONE);
                largeImageView.setImage(new FileBitmapDecoderFactory(resource));
                Log.d("wsx", "onResourceReady  resource：" + resource);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                Toast.makeText(getApplicationContext(), "下载失败", Toast.LENGTH_SHORT).show();
                Log.d("wsx", "onLoadFailed  resource：" + resource + " e:" + e);
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
                super.onLoadCleared(placeholder);
                Toast.makeText(getApplicationContext(), "请求被取消", Toast.LENGTH_SHORT).show();
                Log.d("wsx", "onLoadCleared  resource：" + resource + " e:");
            }

            @Override
            public void getSize(SizeReadyCallback cb) {
                cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            }
        });
    }
}
