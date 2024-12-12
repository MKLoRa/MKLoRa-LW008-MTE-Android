package com.moko.lw008mte.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.lw008mte.R;
import com.moko.lw008mte.entity.TimeSegmentedPoint;

import java.util.List;

public class TimeSegmentedPointAdapter extends BaseQuickAdapter<TimeSegmentedPoint, BaseViewHolder> {
    public TimeSegmentedPointAdapter(List<TimeSegmentedPoint> data) {
        super(R.layout.lw008_mte_item_time_segmented_point, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, TimeSegmentedPoint item) {
        helper.setText(R.id.tv_point_name, item.name);
        helper.setText(R.id.tv_report_interval, item.intervalStr);
        helper.setText(R.id.tv_point_start_hour, item.startHour);
        helper.setText(R.id.tv_point_start_min, item.startMin);
        helper.setText(R.id.tv_point_end_hour, item.endHour);
        helper.setText(R.id.tv_point_end_min, item.endMin);
        helper.setText(R.id.et_report_interval, String.valueOf(item.interval));
        helper.addOnClickListener(R.id.tv_point_start_hour);
        helper.addOnClickListener(R.id.tv_point_start_min);
        helper.addOnClickListener(R.id.tv_point_end_hour);
        helper.addOnClickListener(R.id.tv_point_end_min);
    }
}
