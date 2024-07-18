package com.moko.lw008mte.activity;


import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw008mte.R;
import com.moko.lw008mte.databinding.Lw008MteActivityFilterTlmBinding;
import com.moko.lw008mte.dialog.BottomDialog;
import com.moko.lw008mte.dialog.LoadingMessageDialog;
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

public class FilterTLMActivity extends BaseActivity {


    private Lw008MteActivityFilterTlmBinding mBind;

    private boolean savedParamsError;

    private ArrayList<String> mValues;
    private int mSelected;
    private boolean mTLMEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw008MteActivityFilterTlmBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mValues = new ArrayList<>();
        mValues.add("Null");
        mValues.add("version 0");
        mValues.add("version 1");
        showSyncingProgressDialog();
        mBind.tvTlmVersion.postDelayed(() -> {
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getFilterEddystoneTlmVersion());
            orderTasks.add(OrderTaskAssembler.getFilterEddystoneTlmEnable());
            LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }, 500);
    }


    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
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
                        if (value.length >= 4) {
                            int header = value[0] & 0xFF;// 0xED
                            int flag = value[1] & 0xFF;// read or write
                            int cmd = value[2] & 0xFF;
                            if (header != 0xED)
                                return;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (configKeyEnum == null) {
                                return;
                            }
                            int length = value[3] & 0xFF;
                            if (flag == 0x01) {
                                // write
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_FILTER_EDDYSTONE_TLM_VERSION:
                                    case KEY_FILTER_EDDYSTONE_TLM_ENABLE:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        if (savedParamsError) {
                                            ToastUtils.showToast(FilterTLMActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Save Successfully！");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_FILTER_EDDYSTONE_TLM_VERSION:
                                        if (length > 0) {
                                            mSelected = value[4] & 0xFF;
                                            mBind.tvTlmVersion.setText(mValues.get(mSelected));
                                        }
                                        break;
                                    case KEY_FILTER_EDDYSTONE_TLM_ENABLE:
                                        if (length > 0) {
                                            mTLMEnable = value[4] == 1;
                                            mBind.ivTlmEnable.setImageResource(mTLMEnable ? R.drawable.lw008_ic_checked : R.drawable.lw008_ic_unchecked);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
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


    public void onTLMVersion(View view) {
        if (isWindowLocked())
            return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            mBind.tvTlmVersion.setText(mValues.get(mSelected));
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setFilterEddystoneTlmVersion(mSelected));
            orderTasks.add(OrderTaskAssembler.getFilterEddystoneTlmVersion());
            LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onTLMEnable(View view) {
        if (isWindowLocked())
            return;
        mTLMEnable = !mTLMEnable;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterEddystoneTlmEnable(mTLMEnable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getFilterEddystoneTlmEnable());
        LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
