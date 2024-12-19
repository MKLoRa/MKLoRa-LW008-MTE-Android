package com.moko.lw008mte.activity.device;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw008mte.AppConstants;
import com.moko.lw008mte.R;
import com.moko.lw008mte.activity.BaseActivity;
import com.moko.lw008mte.databinding.Lw008MteActivityOnOffSettingsBinding;
import com.moko.lw008mte.dialog.AlertMessageDialog;
import com.moko.lw008mte.dialog.BottomDialog;
import com.moko.lw008mte.utils.ToastUtils;
import com.moko.support.lw008mte.LoRaLW008MTEMokoSupport;
import com.moko.support.lw008mte.OrderTaskAssembler;
import com.moko.support.lw008mte.entity.OrderCHAR;
import com.moko.support.lw008mte.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OnOffSettingsActivity extends BaseActivity {
    private Lw008MteActivityOnOffSettingsBinding mBind;
    private boolean mReceiverTag;
    private boolean shutdownPayloadOpen;
    private boolean offByButtonOpen;
    private boolean autoPowerOnOpen;
    private ArrayList<String> mValues;
    private int mSelected;
    private int mDeviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw008MteActivityOnOffSettingsBinding.inflate(getLayoutInflater());
        mDeviceType = getIntent().getIntExtra(AppConstants.EXTRA_KEY_DEVICE_TYPE, 0x00);
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        mValues = new ArrayList<>();
        mValues.add("Continuous approach");
        mValues.add("Multiple approaches");
        mBind.llAutoPowerOn.setVisibility(mDeviceType == 0x30 ? View.VISIBLE : View.GONE);
        if (!LoRaLW008MTEMokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            LoRaLW008MTEMokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(4);
            orderTasks.add(OrderTaskAssembler.getOffByMagnetic());
            orderTasks.add(OrderTaskAssembler.getShutdownPayloadEnable());
            orderTasks.add(OrderTaskAssembler.getOffByButtonEnable());
            if (mDeviceType == 0x30)
                orderTasks.add(OrderTaskAssembler.getAutoPowerOn());
            LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
        setListener();
    }

    private void setListener() {
        mBind.ivShutdownPayload.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setShutdownInfoReport(shutdownPayloadOpen ? 0 : 1));
            orderTasks.add(OrderTaskAssembler.getShutdownPayloadEnable());
            LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        mBind.ivOffByButton.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setOffByButton(offByButtonOpen ? 0 : 1));
            orderTasks.add(OrderTaskAssembler.getOffByButtonEnable());
            LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });

        mBind.ivAutoPowerOn.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setAutoPowerOn(autoPowerOnOpen ? 0 : 1));
            orderTasks.add(OrderTaskAssembler.getAutoPowerOn());
            LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });

        mBind.ivPowerOff.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning!");
            dialog.setMessage("Are you sure to turn off the device? Please make sure the device has a button to turn on!");
            dialog.setConfirm("OK");
            dialog.setOnAlertConfirmListener(() -> {
                showSyncingProgressDialog();
                LoRaLW008MTEMokoSupport.getInstance().sendOrder(OrderTaskAssembler.close());
            });
            dialog.show(getSupportFragmentManager());
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (!MokoConstants.ACTION_CURRENT_DATA.equals(action))
            EventBus.getDefault().cancelEventDelivery(event);
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (null != value && value.length >= 5) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = MokoUtils.toInt(Arrays.copyOfRange(value, 2, 4));
                        if (header != 0xED) return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) {
                            return;
                        }
                        int length = value[4] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[5] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_SHUTDOWN_PAYLOAD_ENABLE:
                                case KEY_OFF_BY_BUTTON:
                                case KEY_OFF_BY_MAGNETIC:
                                case KEY_AUTO_POWER_ON_ENABLE:
                                    if (result == 1) {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    } else {
                                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_OFF_BY_MAGNETIC:
                                    if (length == 1) {
                                        mSelected = value[5] & 0xFF;
                                        mBind.tvPowerOnMethod.setText(mValues.get(mSelected));
                                    }
                                    break;
                                case KEY_SHUTDOWN_PAYLOAD_ENABLE:
                                    if (length == 1) {
                                        int enable = value[5] & 0xFF;
                                        shutdownPayloadOpen = enable == 1;
                                        mBind.ivShutdownPayload.setImageResource(enable == 1 ? R.drawable.lw008_ic_checked : R.drawable.lw008_ic_unchecked);
                                    }
                                    break;
                                case KEY_OFF_BY_BUTTON:
                                    if (length == 1) {
                                        int enable = value[5] & 0xFF;
                                        offByButtonOpen = enable == 1;
                                        mBind.ivOffByButton.setImageResource(enable == 1 ? R.drawable.lw008_ic_checked : R.drawable.lw008_ic_unchecked);
                                    }
                                    break;
                                case KEY_AUTO_POWER_ON_ENABLE:
                                    if (length == 1) {
                                        int enable = value[5] & 0xFF;
                                        autoPowerOnOpen = enable == 1;
                                        mBind.ivAutoPowerOn.setImageResource(enable == 1 ? R.drawable.lw008_ic_checked : R.drawable.lw008_ic_unchecked);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void onPowerOnMethod(View v) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            mBind.tvPowerOnMethod.setText(mValues.get(mSelected));
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setOffByMagnetic(value));
            orderTasks.add(OrderTaskAssembler.getOffByMagnetic());
            LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(getSupportFragmentManager());
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                        dismissSyncProgressDialog();
                        finish();
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    public void onBack(View view) {
        finish();
    }
}
