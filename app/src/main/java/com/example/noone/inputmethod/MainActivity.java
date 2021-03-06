package com.example.noone.inputmethod;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    /**
     * uart服务
     */
    public final static UUID UARTSERVICE_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    /**
     * 写
     */
    public final static UUID UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    /**
     * 读
     */
    public final static UUID UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    private BleDevAdapter mBleDevAdapter;
    private BluetoothGatt mBluetoothGatt;
    private boolean isConnected = false;

    private ImageView connectStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv = findViewById(R.id.list_devices);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mBleDevAdapter = new BleDevAdapter(new BleDevAdapter.Listener() {
            @Override
            public void onItemClick(View v, BluetoothDevice dev) {
                closeConn();
                // 连接蓝牙设备
                mBluetoothGatt = dev.connectGatt(MainActivity.this, false, mBluetoothGattCallback);
                Log.v(TAG, String.format("与[%s]开始连接............", dev));
                connectStatus = v.findViewById(R.id.connect_status);
            }
        });
        rv.setAdapter(mBleDevAdapter);
        rv.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeConn();
    }

    // BLE中心设备连接外围设备的数量有限(大概2~7个)，在建立新连接之前必须释放旧连接资源，否则容易出现连接错误133
    private void closeConn() {
        if (mBluetoothGatt != null) {
            isConnected = false;
            mBluetoothGatt.disconnect();
//            mBluetoothGatt.close();
        }
    }

    // 扫描BLE
    public void reScan(View view) {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {

            //弹出对话框提示用户是后打开
          Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          startActivityForResult(enabler, 1);
            // 不做提示，强行打开，此方法需要权限<uses-permissionandroid:name="android.permission.BLUETOOTH_ADMIN" />;
//            bluetoothAdapter.enable();
            return;
        }

        if (mBleDevAdapter.isScanning) {
            Toast.makeText(this,"正在扫描...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"开始扫描...", Toast.LENGTH_SHORT).show();
            mBleDevAdapter.reScan();
        }
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 读取数据成功会回调->onCharacteristicChanged()
    public void read(View view) {
        BluetoothGattService service = getGattService(UARTSERVICE_SERVICE_UUID);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UART_TX_CHARACTERISTIC_UUID);//通过UUID获取可读的Characteristic
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 写入数据成功会回调->onCharacteristicWrite()
    public void write(String text) {
        BluetoothGattService service = getGattService(UARTSERVICE_SERVICE_UUID);
        if (service != null) {
//            String text = mWriteET.getText().toString();
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UART_RX_CHARACTERISTIC_UUID);//通过UUID获取可写的Characteristic
            characteristic.setValue(text.getBytes()); //单次最多20个字节
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    // 设置通知Characteristic变化会回调->onCharacteristicChanged()
    public void setNotify() {
        BluetoothGattService service = getGattService(UARTSERVICE_SERVICE_UUID);
        if (service != null) {
            // 设置Characteristic通知
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UART_TX_CHARACTERISTIC_UUID);//通过UUID获取可通知的Characteristic

            mBluetoothGatt.setCharacteristicNotification(characteristic, true);

            // 向Characteristic的Descriptor属性写入通知开关，使蓝牙设备主动向手机发送数据
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UART_TX_CHARACTERISTIC_UUID);
            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);//和通知类似,但服务端不主动发数据,只指示客户端读取数据
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);

            List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
            for (BluetoothGattDescriptor descriptor : descriptors) {
                if (descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
                    mBluetoothGatt.writeDescriptor(descriptor);
                    Log.d(TAG, "startRead: " + "监听收数据");
                }
            }
        }
    }

    // 获取Gatt服务
    private BluetoothGattService getGattService(UUID uuid) {
        if (!isConnected) {
            Toast.makeText(MainActivity.this,"没有连接", Toast.LENGTH_SHORT).show();
            return null;
        }

        BluetoothGattService service = mBluetoothGatt.getService(uuid);
        if (service == null) {
            Log.e(TAG, "没有找到服务:" + uuid);
        }

        return service;
    }

    // 与服务端连接的Callback
    public BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothDevice dev = gatt.getDevice();
            Log.i(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", dev.getName(), dev.getAddress(), status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    connected();

                    // 启动服务发现
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    disconnected();
                }

            } else {
                closeConn();
            }

            Log.i(TAG, String.format(status == 0 ? (newState == 2 ? "与[%s]连接成功" : "与[%s]连接断开") : ("与[%s]连接出错,错误码:" + status), dev));
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, String.format("启动服务发现:%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), status));
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }

            List<BluetoothGattService> supportedGattServices = mBluetoothGatt.getServices();
            for (BluetoothGattService gattService : supportedGattServices) {
                if (UARTSERVICE_SERVICE_UUID.equals(gattService.getUuid())) {
                    setNotify();
                    return;
                }
            }
            Toast.makeText(MainActivity.this,"未发现URAT服务", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "未找到服务：" + UARTSERVICE_SERVICE_UUID);
            closeConn();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            Log.i(TAG, String.format("读取Characteristic:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            Log.i(TAG, String.format("onCharacteristicWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            InputQueue.getInstance().put(valueStr);
            Log.i(TAG, String.format("通知Characteristic:%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = Arrays.toString(descriptor.getValue());
            Log.i(TAG, String.format("读取Descriptor:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = Arrays.toString(descriptor.getValue());
            Log.i(TAG, String.format("写入Descriptor:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
        }
    };

    public void test(View view) {
        Intent intent = new Intent(this, InputDemoActivity.class);
        startActivity(intent);
    }

    public void draw(View view) {
        closeConn();
        Intent intent = new Intent(this, TouchDrawActivity.class);
        startActivity(intent);
    }

    public void disconnect(View view) {
        closeConn();
    }

    private void connected() {
        isConnected = true;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"连接成功", Toast.LENGTH_SHORT).show();
                connectStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    private void disconnected() {
        isConnected = false;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"连接断开", Toast.LENGTH_SHORT).show();
                connectStatus.setVisibility(View.INVISIBLE);
            }
        });
    }

}
