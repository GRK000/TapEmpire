package com.example.juego;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BusinessMapActivity extends AppCompatActivity {

    private LinearLayout mapContainer;
    private TextView tvTotalProduction, tvCoins;
    private GameState state = GameState.getInstance();
    private Handler updateHandler;
    private Runnable updateRunnable;
    private List<BusinessView> businessViews = new ArrayList<>();
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_map_pro);

        mapContainer = findViewById(R.id.mapContainer);
        tvTotalProduction = findViewById(R.id.tvTotalProduction);
        tvCoins = findViewById(R.id.tvCoins);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        createBusinessMap();
        startUpdateLoop();
    }

    private void createBusinessMap() {
        mapContainer.removeAllViews();
        businessViews.clear();

        List<Generator> generators = state.getGenerators();

        // Layout del mapa: diseño tipo ciudad
        // Filas con diferentes negocios

        int[] rowSizes = {2, 3, 2, 3}; // Patrón de negocios por fila
        int genIndex = 0;

        for (int row = 0; row < 4 && genIndex < generators.size(); row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER);
            rowLayout.setPadding(0, 20, 0, 20);

            // Añadir calle antes de la fila
            if (row > 0) {
                View street = createStreet(true);
                mapContainer.addView(street);
            }

            int businessesInRow = Math.min(rowSizes[row % rowSizes.length], generators.size() - genIndex);

            for (int col = 0; col < businessesInRow && genIndex < generators.size(); col++) {
                Generator gen = generators.get(genIndex);

                // Separador vertical (calle)
                if (col > 0) {
                    View verticalStreet = createStreet(false);
                    rowLayout.addView(verticalStreet);
                }

                BusinessView bv = new BusinessView(gen, genIndex);
                businessViews.add(bv);
                rowLayout.addView(bv.getView());

                genIndex++;
            }

            mapContainer.addView(rowLayout);
        }
    }

    private View createStreet(boolean horizontal) {
        View street = new View(this);
        int width = horizontal ? LinearLayout.LayoutParams.MATCH_PARENT : dpToPx(20);
        int height = horizontal ? dpToPx(15) : LinearLayout.LayoutParams.MATCH_PARENT;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        if (horizontal) {
            params.setMargins(dpToPx(30), 0, dpToPx(30), 0);
        }
        street.setLayoutParams(params);
        street.setBackgroundColor(Color.parseColor("#3D3D5C"));

        return street;
    }

    private void startUpdateLoop() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = () -> {
            state.updateProduction(System.currentTimeMillis());
            updateUI();
            updateBusinessAnimations();
            updateHandler.postDelayed(updateRunnable, 100);
        };
        updateHandler.post(updateRunnable);
    }

    private void updateUI() {
        tvTotalProduction.setText("⚙️ " + GameState.fmt(state.getProductionPerSecond()) + "/seg");
        tvCoins.setText("💰 " + GameState.fmt(state.getCoins()));
    }

    private void updateBusinessAnimations() {
        for (BusinessView bv : businessViews) {
            bv.update();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (updateHandler != null) updateHandler.removeCallbacks(updateRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (updateHandler != null) updateHandler.post(updateRunnable);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // ============== Vista de cada negocio ==============

    private class BusinessView {
        private Generator generator;
        private int index;
        private CardView card;
        private TextView tvEmoji;
        private TextView tvName;
        private TextView tvProduction;
        private TextView tvOwned;
        private View productionIndicator;
        private List<TextView> coinAnimations = new ArrayList<>();
        private long lastCoinSpawn = 0;

        public BusinessView(Generator gen, int idx) {
            this.generator = gen;
            this.index = idx;
            createView();
        }

        private void createView() {
            card = new CardView(BusinessMapActivity.this);
            card.setRadius(dpToPx(15));
            card.setCardElevation(dpToPx(8));
            card.setUseCompatPadding(true);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                dpToPx(140), dpToPx(160)
            );
            cardParams.setMargins(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
            card.setLayoutParams(cardParams);

            // Contenedor interno
            FrameLayout container = new FrameLayout(BusinessMapActivity.this);
            container.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ));

            // Fondo basado en estado
            updateCardBackground();

            // Layout principal
            LinearLayout mainLayout = new LinearLayout(BusinessMapActivity.this);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setGravity(Gravity.CENTER);
            mainLayout.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            mainLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ));

            // Emoji grande
            tvEmoji = new TextView(BusinessMapActivity.this);
            tvEmoji.setText(generator.getEmoji());
            tvEmoji.setTextSize(40);
            tvEmoji.setGravity(Gravity.CENTER);
            mainLayout.addView(tvEmoji);

            // Nombre
            tvName = new TextView(BusinessMapActivity.this);
            tvName.setText(generator.getName().replace(generator.getEmoji() + " ", ""));
            tvName.setTextSize(11);
            tvName.setTextColor(Color.WHITE);
            tvName.setGravity(Gravity.CENTER);
            tvName.setMaxLines(1);
            mainLayout.addView(tvName);

            // Cantidad owned
            tvOwned = new TextView(BusinessMapActivity.this);
            tvOwned.setTextSize(12);
            tvOwned.setTextColor(Color.parseColor("#FFD700"));
            tvOwned.setGravity(Gravity.CENTER);
            mainLayout.addView(tvOwned);

            // Producción
            tvProduction = new TextView(BusinessMapActivity.this);
            tvProduction.setTextSize(10);
            tvProduction.setTextColor(Color.parseColor("#4CAF50"));
            tvProduction.setGravity(Gravity.CENTER);
            mainLayout.addView(tvProduction);

            // Indicador de producción activa
            productionIndicator = new View(BusinessMapActivity.this);
            FrameLayout.LayoutParams indicatorParams = new FrameLayout.LayoutParams(
                dpToPx(8), dpToPx(8)
            );
            indicatorParams.gravity = Gravity.TOP | Gravity.END;
            indicatorParams.setMargins(0, dpToPx(8), dpToPx(8), 0);
            productionIndicator.setLayoutParams(indicatorParams);
            productionIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));

            container.addView(mainLayout);
            container.addView(productionIndicator);
            card.addView(container);

            // Click para comprar
            card.setOnClickListener(v -> {
                if (state.buyGenerator(index)) {
                    animatePurchase();
                    update();
                }
            });

            update();
        }

        private void updateCardBackground() {
            if (!generator.isUnlocked()) {
                card.setCardBackgroundColor(Color.parseColor("#2D2D44"));
            } else if (generator.getOwned() > 0) {
                card.setCardBackgroundColor(Color.parseColor("#1E3A5F"));
            } else {
                card.setCardBackgroundColor(Color.parseColor("#2D2D44"));
            }
        }

        public void update() {
            int owned = generator.getOwned();
            double production = generator.getProductionPerSecond();

            tvOwned.setText("x" + owned);
            tvProduction.setText("+" + GameState.fmt(production) + "/s");

            // Actualizar visibilidad del indicador de producción
            if (owned > 0 && generator.isUnlocked()) {
                productionIndicator.setVisibility(View.VISIBLE);
                pulseIndicator();

                // Spawner monedas con animación
                if (System.currentTimeMillis() - lastCoinSpawn > (1000 / Math.max(1, owned * 0.5))) {
                    spawnCoinAnimation();
                    lastCoinSpawn = System.currentTimeMillis();
                }
            } else {
                productionIndicator.setVisibility(View.INVISIBLE);
            }

            // Opacidad si está bloqueado
            if (!generator.isUnlocked()) {
                card.setAlpha(0.5f);
                tvName.setText("🔒 " + GameState.fmt(generator.getUnlockRequirement()));
            } else {
                card.setAlpha(1.0f);
                tvName.setText(generator.getName().replace(generator.getEmoji() + " ", ""));
            }

            updateCardBackground();
        }

        private void pulseIndicator() {
            if (productionIndicator.getTag() != null) return;
            productionIndicator.setTag("animating");

            ObjectAnimator pulse = ObjectAnimator.ofFloat(productionIndicator, "alpha", 1f, 0.3f);
            pulse.setDuration(500);
            pulse.setRepeatCount(ValueAnimator.INFINITE);
            pulse.setRepeatMode(ValueAnimator.REVERSE);
            pulse.start();
        }

        private void spawnCoinAnimation() {
            if (coinAnimations.size() > 5) return;

            TextView coin = new TextView(BusinessMapActivity.this);
            coin.setText("💰");
            coin.setTextSize(14);

            FrameLayout parent = (FrameLayout) card.getChildAt(0);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;
            coin.setLayoutParams(params);

            parent.addView(coin);
            coinAnimations.add(coin);

            // Animar hacia arriba y desaparecer
            coin.setTranslationY(0);
            coin.setAlpha(1f);

            coin.animate()
                .translationY(-dpToPx(60))
                .alpha(0f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    parent.removeView(coin);
                    coinAnimations.remove(coin);
                })
                .start();
        }

        private void animatePurchase() {
            card.animate()
                .scaleX(1.1f).scaleY(1.1f)
                .setDuration(100)
                .withEndAction(() ->
                    card.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
        }

        public View getView() {
            return card;
        }
    }
}
