package com.moko.lw008mte.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.lw008mte.R;
import com.moko.lw008mte.entity.LogData;

public class LogDataListAdapter extends BaseQuickAdapter<LogData, BaseViewHolder> {

    public LogDataListAdapter() {
        super(R.layout.lw008_mte_item_log_data);
    }

    @Override
    protected void convert(BaseViewHolder helper, LogData item) {
        helper.setText(R.id.tv_time, item.name);
        helper.setImageResource(R.id.iv_checked, item.isSelected ? R.drawable.lw008_ic_selected : R.drawable.lw008_ic_unselected);
    }
}
