package com.example.noone.inputmethod;

import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SimpleInputMethodService extends InputMethodService {

    private TextView textBuf;

    public SimpleInputMethodService() {
    }


    @Override
    public View onCreateInputView() {
        View view = getLayoutInflater().inflate(R.layout.keyboard, null);
        textBuf = view.findViewById(R.id.text_buffer);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    InputConnection inputConnection = getCurrentInputConnection();
                    StringBuffer buf = new StringBuffer();
                    while (buf.length() < 8) {
                        String msg = InputQueue.getInstance().pull();
                        if ((msg == null) || (msg.length() == 0)) {
                            continue;
                        }

                        if (KeyCode.KEY_BACKSPACE.equals(msg)) {
                            if (buf.length() > 0) {
                                buf = new StringBuffer();
                                Message message = clearBufferHandler.obtainMessage(1, "");
                                clearBufferHandler.sendMessage(message);
                            } else {
                                inputConnection.deleteSurroundingText(1, 0);
                            }

                        } else {
                            buf.append(msg);
                            Message message = appendBufferHandler.obtainMessage(1, msg);
                            appendBufferHandler.sendMessage(message);
                        }

                    }

                    if (buf.length() == 4) {
                        // 4bit加个空格
                        Message message = appendBufferHandler.obtainMessage(1, " ");
                        appendBufferHandler.sendMessage(message);
                    } else if (buf.length() == 8) {
                        // 8bit生成一个字符
                        try {
                            char c = (char) Byte.parseByte(buf.toString(), 2);
                            inputConnection.commitText(String.valueOf(c), 1);
                            Message message = clearBufferHandler.obtainMessage(1, "");
                            clearBufferHandler.sendMessage(message);
                        } catch (Exception e) {

                        }
                    }

                }
            }
        }).start();
        return view;
    }

    private Handler appendBufferHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            textBuf.append(msg.obj.toString());
        }
    };

    private Handler clearBufferHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            textBuf.setText("");
        }
    };

}
