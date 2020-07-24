package com.zf.sample;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CardHolder extends RecyclerView.ViewHolder {

    public TextView tvTitle;
    public RelativeLayout rlBg;

    public CardHolder(@NonNull View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tv_title);
        rlBg = itemView.findViewById(R.id.rl_bg);
    }
}
