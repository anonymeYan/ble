package com.example.viewpager_fragment;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class TitleLayout extends LinearLayout {

    public TitleLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.title, this);

        Button titleBack = (Button) findViewById(R.id.btn_1);
        titleBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity) getContext()).finish();
            }
        });

        Button titleEdit = (Button) findViewById(R.id.btn_2);
        titleEdit.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"软件版本号：V1.0",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
