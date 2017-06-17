package com.example.lagerimage_test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.shizhefei.view.largeimage.LargeImageView;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;

import java.io.IOException;
import java.io.InputStream;

import static com.example.lagerimage_test.R.id.imageView;


public class SingleDemoActivity extends FragmentActivity {
    private LargeImageView largeImageView;
    private ToggleButton toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singledemo);

        largeImageView = (LargeImageView) findViewById(imageView);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);

        toggleButton.setOnCheckedChangeListener(onCheckedChangeListener);
        largeImageView.setOnClickListener(onClickListener);
        largeImageView.setOnLongClickListener(onLongClickListener);
        largeImageView.setOnDoubleClickListener(onDoubleClickListener);
        try {
            String fileName = getIntent().getStringExtra("file_name");
            InputStream inputStream = getAssets().open("mvc.png");
            largeImageView.setImage(new InputStreamBitmapDecoderFactory(inputStream), getResources().getDrawable(R.drawable.mvc));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    largeImageView.setScale(0.5f);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == largeImageView) {
                Toast.makeText(getApplicationContext(), "点击事件", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (v == largeImageView) {
                Toast.makeText(getApplicationContext(), "长按事件", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    };
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            largeImageView.setEnabled(!isChecked);
        }
    };

    private LargeImageView.CriticalScaleValueHook criticalScaleValueHook = new LargeImageView.CriticalScaleValueHook() {
        @Override
        public float getMinScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMinScale) {
            return 1;
        }

        @Override
        public float getMaxScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMaxScale) {
            return 4;
        }
    };

    private LargeImageView.OnDoubleClickListener onDoubleClickListener = new LargeImageView.OnDoubleClickListener() {
        @Override
        public boolean onDoubleClick(LargeImageView view, MotionEvent event) {
            float fitScale = view.getFitScale();
            float maxScale = view.getMaxScale();
            float minScale = view.getMinScale();
            String message = "双击事件 minScale:" + minScale + " maxScale:" + maxScale + " fitScale:" + fitScale;
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            //返回true 拦截双击缩放的事件
            return false;
        }
    };
}
