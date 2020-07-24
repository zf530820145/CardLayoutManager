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
        data.add(new CardModel("Blue", R.drawable.bg_blue));
        data.add(new CardModel("Yellow", R.drawable.bg_yellow));
        data.add(new CardModel("Green", R.drawable.bg_green));
        data.add(new CardModel("Purple", R.drawable.bg_purple));
        data.add(new CardModel("Red", R.drawable.bg_red));

    }
}