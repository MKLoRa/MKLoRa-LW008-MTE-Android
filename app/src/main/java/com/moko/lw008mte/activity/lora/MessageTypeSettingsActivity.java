package com.moko.lw008mte.activity.lora;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw008mte.activity.BaseActivity;
import com.moko.lw008mte.databinding.Lw008MteActivityMsgTypeSettingsBinding;
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
import java.util.List;


public class MessageTypeSettingsActivity extends BaseActivity {
    private Lw008MteActivityMsgTypeSettingsBinding mBind;
    private boolean mReceiverTag = false;
    private static final String unconfirmed = "Unconfirmed";
    private static final String confirmed = "Confirmed";
    private final ArrayList<String> payloadTypes = new ArrayList<>(4);
    private final ArrayList<String> retransmissionTimes = new ArrayList<>(8);
    private int heartbeatFlag;
    private int positioningFlag;
    private int lowPowerFlag;
    private int shockFlag;
    private int manDownFlag;
    private int eventFlag;
    private int gpsLimitFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw008MteActivityMsgTypeSettingsBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        mBind.tvTitle.postDelayed(() -> {
            List<OrderTask> orderTasks = new ArrayList<>(8);
            orderTasks.add(OrderTaskAssembler.getHeartbeatPayload());
            orderTasks.add(OrderTaskAssembler.getPositioningPayload());
            orderTasks.add(OrderTaskAssembler.getLowPowerPayload());
            orderTasks.add(OrderTaskAssembler.getManDownPayload());
            orderTasks.add(OrderTaskAssembler.getShockPayload());
            orderTasks.add(OrderTaskAssembler.getEventPayload());
            orderTasks.add(OrderTaskAssembler.getGPSLimitPayload());
            LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }, 300);
        payloadTypes.add(unconfirmed);
        payloadTypes.add(confirmed);
        retransmissionTimes.add("0");
        retransmissionTimes.add("1");
        retransmissionTimes.add("2");
        retransmissionTimes.add("3");
        setListener();
    }

    private void setListener() {
        mBind.tvHeartbeatPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvHeartbeatPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvHeartbeatPayloadType, 1);
        });
        mBind.tvPositioningPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvPositioningPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvPositioningPayloadType, 2);
        });
        mBind.tvLowPowerPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvLowPowerPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvLowPowerPayloadType, 3);
        });
        mBind.tvShockPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvShockPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvShockPayloadType, 4);
        });
        mBind.tvManDownPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvManDownPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvManDownPayloadType, 5);
        });
        mBind.tvEventPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvEventPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvEventPayloadType, 6);
        });
        mBind.tvGPSLimitPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvGPSLimitPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvGPSLimitPayloadType, 7);
        });


        mBind.tvHeartbeatTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvHeartbeatTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvHeartbeatTimes, 0);
        });
        mBind.tvPositioningTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvPositioningTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvPositioningTimes, 0);
        });
        mBind.tvLowPowerTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvLowPowerTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvLowPowerTimes, 0);
        });
        mBind.tvShockTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvShockTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvShockTimes, 0);
        });
        mBind.tvManDownTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvManDownTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvManDownTimes, 0);
        });
        mBind.tvEventTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvEventTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvEventTimes, 0);
        });
        mBind.tvGPSLimitTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvGPSLimitTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvGPSLimitTimes, 0);
        });
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
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (null != value && value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0) {
                            switch (configKeyEnum) {
                                case KEY_HEARTBEAT_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvHeartbeatPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvHeartbeatTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineHeartbeatTime, mBind.layoutHeartbeatTime);
                                    }
                                    break;
                                case KEY_POSITIONING_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvPositioningPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvPositioningTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.linePosTimes, mBind.layoutPosTimes);
                                    }
                                    break;
                                case KEY_LOW_POWER_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvLowPowerPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvLowPowerTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineLowPowerTime, mBind.layoutLowPowerTime);
                                    }
                                    break;
                                case KEY_SHOCK_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvShockPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvShockTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineShockTimes, mBind.layoutShockTimes);
                                    }
                                    break;
                                case KEY_MAN_DOWN_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvManDownPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvManDownTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineManDownTimes, mBind.layoutManDownTimes);
                                    }
                                    break;
                                case KEY_EVENT_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvEventPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvEventTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineEventTimes, mBind.layoutEventTimes);
                                    }
                                    break;
                                case KEY_GPS_LIMIT_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvGPSLimitPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvGPSLimitTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineGpsLimitTimes, mBind.layoutGpsLimitTimes);
                                    }
                                    break;
                            }
                        } else if (flag == 1) {
                            switch (configKeyEnum) {
                                case KEY_HEARTBEAT_PAYLOAD:
                                    heartbeatFlag = value[4] & 0xff;
                                    break;
                                case KEY_POSITIONING_PAYLOAD:
                                    positioningFlag = value[4] & 0xff;
                                    break;
                                case KEY_LOW_POWER_PAYLOAD:
                                    lowPowerFlag = value[4] & 0xff;
                                    break;
                                case KEY_SHOCK_PAYLOAD:
                                    shockFlag = value[4] & 0xff;
                                    break;
                                case KEY_MAN_DOWN_PAYLOAD:
                                    manDownFlag = value[4] & 0xff;
                                    break;
                                case KEY_EVENT_PAYLOAD:
                                    eventFlag = value[4] & 0xff;
                                    break;
                                case KEY_GPS_LIMIT_PAYLOAD:
                                    gpsLimitFlag = value[4] & 0xff;
                                    if (shockFlag == 1 && heartbeatFlag == 1 && lowPowerFlag == 1 && eventFlag == 1 && manDownFlag == 1 && shockFlag == 1
                                            && gpsLimitFlag == 1 && positioningFlag == 1) {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    } else {
                                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                    }
                                    break;

                            }
                        }
                    }
                }
            }
        });
    }

    private void setMaxTimes(int enable, View view, LinearLayout linearLayout) {
        view.setVisibility(enable == 1 ? View.VISIBLE : View.GONE);
        linearLayout.setVisibility(enable == 1 ? View.VISIBLE : View.GONE);
    }

    private void showBottomDialog(ArrayList<String> mValues, int mSelected, TextView textView, int type) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mSelected);
        dialog.setListener(value -> {
            textView.setText(mValues.get(value));
            if (type == 1) {
                setMaxTimes(value, mBind.lineHeartbeatTime, mBind.layoutHeartbeatTime);
            } else if (type == 2) {
                setMaxTimes(value, mBind.linePosTimes, mBind.layoutPosTimes);
            } else if (type == 3) {
                setMaxTimes(value, mBind.lineLowPowerTime, mBind.layoutLowPowerTime);
            } else if (type == 4) {
                setMaxTimes(value, mBind.lineShockTimes, mBind.layoutShockTimes);
            } else if (type == 5) {
                setMaxTimes(value, mBind.lineManDownTimes, mBind.layoutManDownTimes);
            } else if (type == 6) {
                setMaxTimes(value, mBind.lineEventTimes, mBind.layoutEventTimes);
            } else if (type == 7) {
                setMaxTimes(value, mBind.lineGpsLimitTimes, mBind.layoutGpsLimitTimes);
            }
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onBack(View view) {
        finish();
    }

    public void onSave(View view) {
        showSyncingProgressDialog();
        shockFlag = 0;
        heartbeatFlag = 0;
        lowPowerFlag = 0;
        eventFlag = 0;
        gpsLimitFlag = 0;
        positioningFlag = 0;
        int heartbeatPayloadType = confirmed.equals(mBind.tvHeartbeatPayloadType.getText().toString().trim()) ? 1 : 0;
        int heartbeatTime = Integer.parseInt(mBind.tvHeartbeatTimes.getText().toString().trim()) + 1;
        int positioningPayloadType = confirmed.equals(mBind.tvPositioningPayloadType.getText().toString().trim()) ? 1 : 0;
        int positioningTime = Integer.parseInt(mBind.tvPositioningTimes.getText().toString().trim()) + 1;
        int lowPowerPayloadType = confirmed.equals(mBind.tvLowPowerPayloadType.getText().toString().trim()) ? 1 : 0;
        int lowPowerTime = Integer.parseInt(mBind.tvLowPowerTimes.getText().toString().trim()) + 1;
        int shockPayloadType = confirmed.equals(mBind.tvShockPayloadType.getText().toString().trim()) ? 1 : 0;
        int shockTime = Integer.parseInt(mBind.tvShockTimes.getText().toString().trim()) + 1;
        int manDownPayloadType = confirmed.equals(mBind.tvManDownPayloadType.getText().toString().trim()) ? 1 : 0;
        int manDownTime = Integer.parseInt(mBind.tvManDownTimes.getText().toString().trim()) + 1;
        int eventPayloadType = confirmed.equals(mBind.tvEventPayloadType.getText().toString().trim()) ? 1 : 0;
        int eventTime = Integer.parseInt(mBind.tvEventTimes.getText().toString().trim()) + 1;
        int gpsLimitPayloadType = confirmed.equals(mBind.tvGPSLimitPayloadType.getText().toString().trim()) ? 1 : 0;
        int gpsLimitTime = Integer.parseInt(mBind.tvGPSLimitTimes.getText().toString().trim()) + 1;
        List<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.setHeartbeatPayload(heartbeatPayloadType, heartbeatTime));
        orderTasks.add(OrderTaskAssembler.setLowPowerPayload(lowPowerPayloadType, lowPowerTime));
        orderTasks.add(OrderTaskAssembler.setPositioningPayload(positioningPayloadType, positioningTime));
        orderTasks.add(OrderTaskAssembler.setShockPayload(shockPayloadType, shockTime));
        orderTasks.add(OrderTaskAssembler.setManDownPayload(manDownPayloadType, manDownTime));
        orderTasks.add(OrderTaskAssembler.setEventPayload(eventPayloadType, eventTime));
        orderTasks.add(OrderTaskAssembler.setGPSLimitPayload(gpsLimitPayloadType, gpsLimitTime));
        LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }
}
