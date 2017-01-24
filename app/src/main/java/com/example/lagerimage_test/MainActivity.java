package com.example.lagerimage_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class MainActivity extends FragmentActivity {
    private View singleDemoButton;
    private View networkDemoButton;
    private View viewPagerDemoButton;
    private View listButton;
    private View clearCacheButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        singleDemoButton = findViewById(R.id.main_singleDemo_button);
        viewPagerDemoButton = findViewById(R.id.main_viewPagerDemo_button);
        networkDemoButton = findViewById(R.id.main_networkDemo_button);
        listButton = findViewById(R.id.main_list_button);
        clearCacheButton = findViewById(R.id.main_clear_cache_button);

        singleDemoButton.setOnClickListener(onClickListener);
        viewPagerDemoButton.setOnClickListener(onClickListener);
        networkDemoButton.setOnClickListener(onClickListener);
        listButton.setOnClickListener(onClickListener);
        clearCacheButton.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == singleDemoButton) {
                startActivity(new Intent(getApplicationContext(),SingleDemoActivity.class));
            } else if (v == viewPagerDemoButton) {
                startActivity(new Intent(getApplicationContext(),ViewPagerDemoActivity.class));
            } else if (v == networkDemoButton) {
                startActivity(new Intent(getApplicationContext(),NetworkDemoActivity.class));
            }else if(v==listButton){
                startActivity(new Intent(getApplicationContext(),ListImageActivity.class));
            }else if(v==clearCacheButton){
                Toast.makeText(getApplicationContext(), "开始清除缓存", Toast.LENGTH_SHORT).show();
                Glide.get(getApplicationContext()).clearMemory();
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
        }
    };
}
