package com.example.lagerimage_test;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

        String url = "http://short.im.rockhippo.cn/uploads/msg/201703/20170309/1485/1489068660846.jpg";
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
    }
}
