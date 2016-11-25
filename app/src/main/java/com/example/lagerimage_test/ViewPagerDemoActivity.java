package com.example.lagerimage_test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.shizhefei.view.largeimage.LargeImageView;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;
import com.shizhefei.view.viewpager.RecyclingPagerAdapter;

import java.io.IOException;


public class ViewPagerDemoActivity extends FragmentActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpagerdemo);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
    }

    private RecyclingPagerAdapter adapter = new RecyclingPagerAdapter() {
        private String[] ss = {"ccc.jpg", "111.jpg", "aaa.jpg"};

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.ttt, container, false);
            }
            Log.d("wsx", " p:" + position + " ss[position]:" + ss[position]);
            LargeImageView largeImageView = (LargeImageView) convertView;
            try {
                largeImageView.setImage(new InputStreamBitmapDecoderFactory(getAssets().open(ss[position])));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return largeImageView;
        }

        @Override
        public int getCount() {
            return ss.length;
        }
    };
}
