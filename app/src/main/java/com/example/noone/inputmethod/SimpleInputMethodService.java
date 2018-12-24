package com.example.noone.inputmethod;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;

public class SimpleInputMethodService extends InputMethodService implements View.OnClickListener {

    public SimpleInputMethodService() {
    }


    @Override
    public View onCreateInputView() {
        View view = getLayoutInflater().inflate(R.layout.keyboard, null);

        view.findViewById(R.id.button).setOnClickListener(this);
        view.findViewById(R.id.button2).setOnClickListener(this);
        view.findViewById(R.id.button3).setOnClickListener(this);
        view.findViewById(R.id.button4).setOnClickListener(this);
        view.findViewById(R.id.button5).setOnClickListener(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String msg = InputQueue.getInstance().pull();
                    if ((msg == null) || (msg.length() == 0)) {
                        continue;
                    }

                    InputConnection inputConnection = getCurrentInputConnection();
                    inputConnection.commitText(msg, 1);
                }
            }
        }).start();
        return view;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button5) {
            hideWindow();
        } else {
            Button button = (Button) v;
            InputConnection inputConnection = getCurrentInputConnection();
            inputConnection.commitText(button.getText(), 1);
        }

    }
}
