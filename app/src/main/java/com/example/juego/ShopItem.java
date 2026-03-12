package com.example.juego;

public class ShopItem {
    private final String title;
    private final int cost;

    public ShopItem(String title, int cost) {
        this.title = title;
        this.cost = cost;
    }

    public String getTitle() {
        return title;
    }

    public int getCost() {
        return cost;
    }

}
