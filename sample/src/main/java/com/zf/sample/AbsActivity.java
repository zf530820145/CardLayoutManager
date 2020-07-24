package com.zf.sample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.zf.card.CardItemAnimator;
import com.zf.card.CardLayoutManager;


import java.util.ArrayList;

public class AbsActivity extends AppCompatActivity {

    private ArrayList<CardModel> data=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abs);
        initData();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new CardLayoutManager());
        recyclerView.setItemAnimator(new CardItemAnimator());
        recyclerView.setAdapter(new CardAdapter(this, data));
    }

    public void initData() {
        data.add(new CardModel("omisego", R.drawable.omg_background, R.drawable.ic_omg));
        data.add(new CardModel("NEM", R.drawable.nem_background, R.drawable.ic_nem));
        data.add(new CardModel("Ethereum", R.drawable.ethereum_background, R.drawable.ic_ethereum));
        data.add(new CardModel("Ripple", R.drawable.ripple_background, R.drawable.ic_ripple));
        data.add(new CardModel("Bitcoin", R.drawable.bitcoin_background, R.drawable.ic_bitcoin));

    }
}