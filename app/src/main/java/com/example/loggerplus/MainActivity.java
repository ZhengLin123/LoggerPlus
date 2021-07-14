package com.example.loggerplus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogUtil.init(this);

        LogUtil.d("hhhhhhhhhhhh");


    }

    public void testNull(View view) {
        String str = null;
        str.isEmpty();
    }
}
