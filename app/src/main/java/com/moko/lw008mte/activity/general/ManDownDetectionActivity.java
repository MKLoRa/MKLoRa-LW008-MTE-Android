package com.moko.lw008mte.activity.general;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw008mte.activity.BaseActivity;
import com.moko.lw008mte.databinding.Lw008MteActivityManDownDetectionBinding;
import com.moko.lw008mte.dialog.AlertMessageDialog;
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

public class ManDownDetectionActivity extends BaseActivity {

    private Lw008MteActivityManDownDetectionBinding mBind;
    private boolean mReceiverTag = false;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw008MteActivityManDownDetectionBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        mBind.cbManDownDetection.postDelayed(() -> {
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getManDownDetectionEnable());
            orderTasks.add(OrderTaskAssembler.getManDownDetectionTimeout());
            LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }, 500);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
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
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        if (value.length >= 5) {
                            int header = value[0] & 0xFF;// 0xED
                            int flag = value[1] & 0xFF;// read or write
                            int cmd = MokoUtils.toInt(Arrays.copyOfRange(value, 2, 4));
                            if (header != 0xED)
                                return;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (configKeyEnum == null) {
                                return;
                            }
                            int length = value[4] & 0xFF;
                            if (flag == 0x01) {
                                // write
                                int result = value[5] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_MAN_DOWN_DETECTION_ENABLE:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        break;
                                    case KEY_MAN_DOWN_DETECTION_TIMEOUT:
                                    case KEY_MAN_DOWN_IDLE_RESET:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        if (savedParamsError) {
                                            ToastUtils.showToast(ManDownDetectionActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Save Successfully！");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_MAN_DOWN_DETECTION_ENABLE:
                                        if (length > 0) {
                                            int enable = value[5] & 0xFF;
                                            mBind.cbManDownDetection.setChecked(enable == 1);
                                        }
                                        break;
                                    case KEY_MAN_DOWN_DETECTION_TIMEOUT:
                                        if (length > 0) {
                                            byte[] timeoutBytes = Arrays.copyOfRange(value, 5, 5 + length);
                                            int timeout = MokoUtils.toInt(timeoutBytes);
                                            mBind.etIdleDetectionTimeout.setText(String.valueOf(timeout));
                                        }
                                        break;
                                }
                            }
                        }
                        break;
                }
            }
        });
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            finish();
                            break;
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
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }


    public void onBack(View view) {
        backHome();
    }

    @Override
    public void onBackPressed() {
        backHome();
    }

    private void backHome() {
        setResult(RESULT_OK);
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked())
            return;
        if (isValid()) {
            showSyncingProgressDialog();
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        final String timeoutStr = mBind.etIdleDetectionTimeout.getText().toString();
        if (TextUtils.isEmpty(timeoutStr))
            return false;
        final int timeout = Integer.parseInt(timeoutStr);
        if (timeout < 1 || timeout > 8760)
            return false;
        return true;

    }

    private void saveParams() {
        final String timeoutStr = mBind.etIdleDetectionTimeout.getText().toString();
        final int timeout = Integer.parseInt(timeoutStr);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setManDownDetectionEnable(mBind.cbManDownDetection.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setManDownDetectionTimeout(timeout));
        LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onReset(View view) {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Reset Idle Status");
        dialog.setMessage("Whether to confirm the reset");
        dialog.setCancel("YES");
        dialog.setConfirm("Cancel");
        dialog.setOnAlertCancelListener(() -> {
            showSyncingProgressDialog();
            LoRaLW008MTEMokoSupport.getInstance().sendOrder(OrderTaskAssembler.setManDownIdleReset());
        });
        dialog.show(getSupportFragmentManager());
    }
}
