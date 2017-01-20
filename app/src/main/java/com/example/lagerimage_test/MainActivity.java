package com.example.lagerimage_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class MainActivity extends FragmentActivity {
    private View singleDemoButton;
    private View networkDemoButton;
    private View viewPagerDemoButton;
    private View listButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        singleDemoButton = findViewById(R.id.main_singleDemo_button);
        viewPagerDemoButton = findViewById(R.id.main_viewPagerDemo_button);
        networkDemoButton = findViewById(R.id.main_networkDemo_button);
        listButton = findViewById(R.id.main_list_button);

        singleDemoButton.setOnClickListener(onClickListener);
        viewPagerDemoButton.setOnClickListener(onClickListener);
        networkDemoButton.setOnClickListener(onClickListener);
        listButton.setOnClickListener(onClickListener);
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
            }
        }
    };
}
