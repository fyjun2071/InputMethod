package com.example.noone.inputmethod;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.noone.inputmethod.touchdraw.DrawView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TouchDrawActivity extends AppCompatActivity {

    private final static String TAG = TouchDrawActivity.class.getSimpleName();

    private final static String DEVICE_NAME = "BBC micro:bit";

    public final static UUID ACCELEROMETERSERVICE_SERVICE_UUID = UUID.fromString("e95d0753-251d-470a-a062-fa1922dfa9a8");
    public final static UUID ACCELEROMETERDATA_CHARACTERISTIC_UUID = UUID.fromString("E95DCA4B-251D-470A-A062-FA1922DFA9A8");
    public final static UUID ACCELEROMETERPERIOD_CHARACTERISTIC_UUID = UUID.fromString("E95DFB24-251D-470A-A062-FA1922DFA9A8");

    private BluetoothGatt mBluetoothGatt;

    private DrawView dv;

    private final ScanCallback mScanCallback = new ScanCallback() {// 扫描Callback
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice dev = result.getDevice();
            if ((dev.getName() != null) && dev.getName().startsWith(DEVICE_NAME)) {
                Log.i(TAG, "发现micro:bit, " + dev.getAddress());
                if (mBluetoothGatt == null) {
                    mBluetoothGatt = dev.connectGatt(TouchDrawActivity.this, false, mBluetoothGattCallback);
                    Log.i(TAG, "开始连接micro:bit, " + dev.getAddress());
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_draw);

        dv = findViewById(R.id.touch_draw_view);

        reScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = new MenuInflater(this);
        inflator.inflate(R.menu.menu_touch_draw, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //获取自定义的绘图视图
        dv.paint.setXfermode(null);//取消擦除效果
        dv.paint.setStrokeWidth(1);//初始化画笔的宽度
        switch (item.getItemId()) {
            case R.id.red:
                dv.paint.setColor(Color.RED);//设置笔的颜色为红色
                item.setChecked(true);
                break;
            case R.id.green:
                dv.paint.setColor(Color.GREEN);//设置笔的颜色为绿色
                item.setChecked(true);
                break;
            case R.id.blue:
                dv.paint.setColor(Color.BLUE);//设置笔的颜色为蓝色
                item.setChecked(true);
                break;
            case R.id.width_1:
                dv.paint.setStrokeWidth(1);//设置笔触的宽度为1像素
                break;
            case R.id.width_2:
                dv.paint.setStrokeWidth(5);//设置笔触的宽度为5像素
                break;
            case R.id.width_3:
                dv.paint.setStrokeWidth(10);//设置笔触的宽度为10像素
                break;
            case R.id.clear:
                dv.clear();//擦除绘画
                break;
            case R.id.save:
                dv.save();//保存绘画
                break;
        }
        return true;
    }

    public void reScan() {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {

            //弹出对话框提示用户是后打开
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, 1);
            // 不做提示，强行打开，此方法需要权限<uses-permissionandroid:name="android.permission.BLUETOOTH_ADMIN" />;
//            bluetoothAdapter.enable();
            Toast.makeText(this,"蓝牙未开启", Toast.LENGTH_SHORT).show();
            return;
        }

        final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        // Android5.0新增的扫描API，扫描返回的结果更友好，比如BLE广播数据以前是byte[] scanRecord，而新API帮我们解析成ScanRecord类
        bluetoothLeScanner.startScan(mScanCallback);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(mScanCallback); //停止扫描
            }
        }, 3000);

    }

    public BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothDevice dev = gatt.getDevice();
            Log.i(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", dev.getName(), dev.getAddress(), status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "连接成功");
                gatt.discoverServices();
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(TouchDrawActivity.this,"连接失败", Toast.LENGTH_SHORT).show();
                }
            });


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
                if (ACCELEROMETERSERVICE_SERVICE_UUID.equals(gattService.getUuid())) {
                    setNotify(gattService);
                    return;
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(TouchDrawActivity.this,"未发现加速度服务", Toast.LENGTH_SHORT).show();
                }
            });
            Log.e(TAG, "未找到服务：" + ACCELEROMETERSERVICE_SERVICE_UUID);
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
            byte[] data = characteristic.getValue();
            final byte[] x_bytes = new byte[2];
            final byte[] y_bytes = new byte[2];
            byte[] z_bytes = new byte[2];
            System.arraycopy(data, 0, x_bytes, 0, 2);
            System.arraycopy(data, 2, y_bytes, 0, 2);
            System.arraycopy(data, 4, z_bytes, 0, 2);
//            Log.i(TAG, Arrays.toString(data));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dv.draw(getShortFromLitteEndianBytes(x_bytes) / 1000f, getShortFromLitteEndianBytes(y_bytes) / 1000f);
                }
            });

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

    public void setNotify(BluetoothGattService service ) {
        // 设置Characteristic通知
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(ACCELEROMETERDATA_CHARACTERISTIC_UUID);//通过UUID获取可通知的Characteristic

        mBluetoothGatt.setCharacteristicNotification(characteristic, true);

        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor : descriptors) {
            if (descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                mBluetoothGatt.writeDescriptor(descriptor);
                Log.d(TAG, "监听收数据:" + descriptor.getUuid());
            }
        }
    }

    @Override
    protected void onDestroy() {
        closeConn();
        super.onDestroy();
    }

    private void closeConn() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
//            mBluetoothGatt.close();
        }
    }

    private short getShortFromLitteEndianBytes(byte[] bytes) {
        ByteBuffer bf = ByteBuffer.wrap(bytes);
        bf.order(ByteOrder.LITTLE_ENDIAN);
        return bf.getShort();
    }

}
