package com.zf.sample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class CardAdapter extends RecyclerView.Adapter {
    private final Context context;
    private final ArrayList<CardModel> data;

    public CardAdapter(Context context, ArrayList<CardModel> data) {
       this.context=context;
       this.data=data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_card_view, parent, false);
        return new CardHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        CardModel cardModel = data.get(position);

        final CardHolder cardHolder = (CardHolder) holder;
        cardHolder.rlBg.setBackgroundResource(cardModel.background);
        cardHolder.tvTitle.setText(cardModel.title);
        cardHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.size()-1>0&&holder.getLayoutPosition()==data.size()-1){
                    final ViewPropertyAnimator animation =   cardHolder.itemView.animate();
                    animation
                            .translationY(400)
                            .scaleX(0.8f)
                            .scaleY(0.6f)
                            .setDuration(250).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            animation.setListener(null);
                            notifyItemMoved(data.size() - 1,0);
                            Collections.rotate(data,1);
                        }
                    }).start();

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data!=null?data.size():0;
    }
}
