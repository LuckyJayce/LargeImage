package com.example.lagerimage_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class MainActivity extends FragmentActivity {
    private View networkDemoButton;
    private View viewPagerDemoButton;
    private View listButton;
    private View singleDemoVButton;
    private View singleDemoHButton;
    private View singleDemoNButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        singleDemoVButton = findViewById(R.id.main_singleDemoV_button);
        singleDemoHButton = findViewById(R.id.main_singleDemoH_button);
        singleDemoNButton = findViewById(R.id.main_singleDemoN_button);
        viewPagerDemoButton = findViewById(R.id.main_viewPagerDemo_button);
        networkDemoButton = findViewById(R.id.main_networkDemo_button);
        listButton = findViewById(R.id.main_list_button);

        singleDemoVButton.setOnClickListener(onClickListener);
        singleDemoHButton.setOnClickListener(onClickListener);
        singleDemoNButton.setOnClickListener(onClickListener);
        viewPagerDemoButton.setOnClickListener(onClickListener);
        networkDemoButton.setOnClickListener(onClickListener);
        listButton.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == singleDemoNButton) {
                Intent intent = new Intent(getApplicationContext(),SingleDemoActivity.class);
                intent.putExtra("file_name","111.jpg");
                startActivity(intent);
            }else  if (v == singleDemoVButton) {
                Intent intent = new Intent(getApplicationContext(),SingleDemoActivity.class);
                intent.putExtra("file_name","ccc.jpg");
                startActivity(intent);
            }else  if (v == singleDemoHButton) {
                Intent intent = new Intent(getApplicationContext(),SingleDemoActivity.class);
                intent.putExtra("file_name","aaa.jpg");
                startActivity(intent);
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
