package com.example.noone.inputmethod;

import android.content.Intent;
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
                InputQueue.getInstance().put(KeyCode.KEY_0);
                break;
            case R.id.button_1:
                InputQueue.getInstance().put(KeyCode.KEY_1);
                break;
            case R.id.button_d:
                InputQueue.getInstance().put(KeyCode.KEY_BACKSPACE);
                break;
        }
    }

    public void click2(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button_up:
                InputQueue.getInstance().put(KeyCode.KEY_00);
                break;
            case R.id.button_left:
                InputQueue.getInstance().put(KeyCode.KEY_01);
                break;
            case R.id.button_down:
                InputQueue.getInstance().put(KeyCode.KEY_10);
                break;
            case R.id.button_right:
                InputQueue.getInstance().put(KeyCode.KEY_11);
                break;
            case R.id.button_center:
                InputQueue.getInstance().put(KeyCode.KEY_BACKSPACE);
                break;
        }
    }

}
