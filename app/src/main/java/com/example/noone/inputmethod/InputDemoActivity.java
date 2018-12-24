package com.example.noone.inputmethod;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class InputDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_demo);
    }

    public void click1(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button_0:
                InputQueue.getInstance().put("0");
                break;
            case R.id.button_1:
                InputQueue.getInstance().put("1");
                break;
            case R.id.button_d:
                InputQueue.getInstance().put("delete");
                break;
        }
    }

    public void click2(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button_up:
                InputQueue.getInstance().put("00");
                break;
            case R.id.button_left:
                InputQueue.getInstance().put("01");
                break;
            case R.id.button_down:
                InputQueue.getInstance().put("10");
                break;
            case R.id.button_right:
                InputQueue.getInstance().put("11");
                break;
            case R.id.button_center:
                InputQueue.getInstance().put("delete");
                break;
        }
    }
}
