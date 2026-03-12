package com.example.juego;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ShopItemAdapter extends ArrayAdapter<ShopItem> {
    private final Context context;
    private final List<ShopItem> items;
    private final GameState state;
    private final Consumer<ShopItem> onItemBuy;

    public ShopItemAdapter(Context context, List<ShopItem> items, GameState state, Consumer<ShopItem> onItemBuy) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
        this.state = state;
        this.onItemBuy = onItemBuy;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ShopItem item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.shop_item, parent, false);
        }

        TextView title = convertView.findViewById(R.id.itemTitle);
        TextView cost = convertView.findViewById(R.id.itemCost);

        title.setText(item.getTitle());
        cost.setText("Coste: " + item.getCost());

        // Color feedback
        /*if (currentCoins >= item.getCost()) {
            convertView.setAlpha(1.0f);
        } else {
            convertView.setAlpha(0.4f);
        }*/

        if (state.getCoins() >= item.getCost()) {
            convertView.setAlpha(1.0f);
        } else {
            convertView.setAlpha(0.4f);
        }

        convertView.setOnClickListener(v -> {
            if (state.getCoins() >= item.getCost()) {
                onItemBuy.accept(item);
            } else {
                Toast.makeText(context, "No tienes suficientes coins", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }
}

