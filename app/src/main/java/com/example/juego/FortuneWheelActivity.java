package com.example.juego;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class FortuneWheelActivity extends AppCompatActivity {

    private WheelView wheelView;
    private TextView tvCoins, tvResultTitle, tvResultPrize;
    private LinearLayout resultContainer;
    private Button btnSpin, btnBack;
    private GameState state = GameState.getInstance();
    private MiniGame miniGame;
    private Vibrator vibrator;
    private Random random = new Random();

    private boolean isSpinning = false;
    private MiniGame.FortuneWheelPrize[] prizes;
    private int selectedPrizeIndex = 0;

    // Colores de los segmentos
    private final int[] SEGMENT_COLORS = {
        Color.parseColor("#FF6B6B"),  // Rojo
        Color.parseColor("#4ECDC4"),  // Turquesa
        Color.parseColor("#FFE66D"),  // Amarillo
        Color.parseColor("#95E1D3"),  // Verde claro
        Color.parseColor("#F38181"),  // Rosa
        Color.parseColor("#AA96DA"),  // Púrpura
        Color.parseColor("#FCBAD3"),  // Rosa claro
        Color.parseColor("#A8D8EA"),  // Azul claro
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fortune_wheel_pro);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Obtener el minijuego
        for (MiniGame game : state.getMiniGames()) {
            if (game.getType() == MiniGame.MiniGameType.FORTUNE_WHEEL) {
                miniGame = game;
                break;
            }
        }

        // Generar premios
        prizes = miniGame.generateWheelPrizes(state.getProductionPerSecond());

        // Bind views
        tvCoins = findViewById(R.id.tvCoins);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultPrize = findViewById(R.id.tvResultPrize);
        resultContainer = findViewById(R.id.resultContainer);
        btnSpin = findViewById(R.id.btnSpin);
        btnBack = findViewById(R.id.btnBack);

        // Reemplazar ImageView por nuestra WheelView personalizada
        ImageView placeholder = findViewById(R.id.wheelImage);
        FrameLayout parent = (FrameLayout) placeholder.getParent();
        int index = parent.indexOfChild(placeholder);
        parent.removeView(placeholder);

        wheelView = new WheelView(this, prizes, SEGMENT_COLORS);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            dpToPx(300), dpToPx(300)
        );
        params.gravity = android.view.Gravity.CENTER;
        wheelView.setLayoutParams(params);
        parent.addView(wheelView, index);

        // Listeners
        btnSpin.setOnClickListener(v -> spinWheel());
        btnBack.setOnClickListener(v -> finish());

        updateUI();
    }

    private void updateUI() {
        tvCoins.setText("💰 " + GameState.fmt(state.getCoins()));

        if (!miniGame.isAvailable()) {
            if (miniGame.getRemainingPlays() <= 0) {
                btnSpin.setText("Sin partidas");
                btnSpin.setEnabled(false);
            } else {
                long cooldown = miniGame.getCooldownRemaining();
                btnSpin.setText("⏱️ " + formatTime(cooldown));
                btnSpin.setEnabled(false);
            }
        } else {
            btnSpin.setText("🎰 GIRAR (" + miniGame.getRemainingPlays() + ")");
            btnSpin.setEnabled(!isSpinning);
        }
    }

    private void spinWheel() {
        if (isSpinning || !miniGame.isAvailable()) return;

        isSpinning = true;
        resultContainer.setVisibility(View.INVISIBLE);
        btnSpin.setEnabled(false);
        btnSpin.setText("🎰 Girando...");

        // Registrar partida
        miniGame.startGame();
        state.setTotalMiniGamesPlayed(state.getTotalMiniGamesPlayed() + 1);

        // Seleccionar premio basado en probabilidades
        selectedPrizeIndex = selectPrizeIndex();

        // Calcular ángulo final
        float degreesPerSegment = 360f / prizes.length;
        float targetAngle = 360 - (selectedPrizeIndex * degreesPerSegment + degreesPerSegment / 2);

        // Añadir vueltas extras para efecto dramático (5-8 vueltas)
        int extraSpins = 5 + random.nextInt(4);
        float totalRotation = targetAngle + (360 * extraSpins);

        // Obtener rotación actual
        float currentRotation = wheelView.getRotation() % 360;

        // Crear animación
        ObjectAnimator animator = ObjectAnimator.ofFloat(
            wheelView, "rotation", currentRotation, currentRotation + totalRotation
        );

        // Duración basada en vueltas (más dramático)
        animator.setDuration(4000 + (extraSpins * 500));
        animator.setInterpolator(new DecelerateInterpolator(1.5f));

        // Vibración durante el giro
        startSpinVibration(animator.getDuration());

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onSpinComplete();
            }
        });

        animator.start();
    }

    private int selectPrizeIndex() {
        int roll = random.nextInt(100);
        int cumulative = 0;

        for (int i = 0; i < prizes.length; i++) {
            cumulative += prizes[i].probability;
            if (roll < cumulative) {
                return i;
            }
        }
        return prizes.length - 1;
    }

    private void startSpinVibration(long duration) {
        if (vibrator == null || !vibrator.hasVibrator()) return;

        // Patrón de vibración que simula el "tick" de la ruleta
        Handler handler = new Handler(Looper.getMainLooper());
        final int[] tickCount = {0};
        final int maxTicks = 30;

        Runnable tickRunnable = new Runnable() {
            @Override
            public void run() {
                if (tickCount[0] < maxTicks && isSpinning) {
                    vibrator.vibrate(VibrationEffect.createOneShot(20, 50));
                    tickCount[0]++;

                    // Incrementar intervalo para simular desaceleración
                    long nextDelay = 50 + (tickCount[0] * 10);
                    handler.postDelayed(this, nextDelay);
                }
            }
        };
        handler.post(tickRunnable);
    }

    private void onSpinComplete() {
        isSpinning = false;

        MiniGame.FortuneWheelPrize prize = prizes[selectedPrizeIndex];

        // Vibración de celebración
        if (vibrator != null && vibrator.hasVibrator()) {
            if (prize.value > 0) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, 255));
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
            }
        }

        // Dar premio
        if (prize.value > 0) {
            state.setCoins(state.getCoins() + prize.value);
        }

        // Mostrar resultado con animación
        showResult(prize);

        updateUI();
    }

    private void showResult(MiniGame.FortuneWheelPrize prize) {
        if (prize.value > 0) {
            tvResultTitle.setText("🎉 ¡GANASTE! 🎉");
            tvResultTitle.setTextColor(Color.parseColor("#FFD700"));
            tvResultPrize.setText(prize.name + "\n💰 +" + GameState.fmt(prize.value));
            tvResultPrize.setTextColor(Color.parseColor("#4CAF50"));
        } else if (prize.name.contains("Prod")) {
            tvResultTitle.setText("⚡ ¡BONUS! ⚡");
            tvResultTitle.setTextColor(Color.parseColor("#00E5FF"));
            tvResultPrize.setText(prize.name);
            tvResultPrize.setTextColor(Color.parseColor("#00E5FF"));
        } else if (prize.name.contains("Crit")) {
            tvResultTitle.setText("🎯 ¡BONUS! 🎯");
            tvResultTitle.setTextColor(Color.parseColor("#FF4081"));
            tvResultPrize.setText(prize.name);
            tvResultPrize.setTextColor(Color.parseColor("#FF4081"));
        } else {
            tvResultTitle.setText("😢 ¡Oh no!");
            tvResultTitle.setTextColor(Color.parseColor("#888888"));
            tvResultPrize.setText("Mejor suerte la próxima vez");
            tvResultPrize.setTextColor(Color.parseColor("#888888"));
        }

        // Animación de aparición
        resultContainer.setAlpha(0f);
        resultContainer.setScaleX(0.5f);
        resultContainer.setScaleY(0.5f);
        resultContainer.setVisibility(View.VISIBLE);

        resultContainer.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return minutes + "m " + secs + "s";
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // =========== Vista personalizada de la ruleta ===========

    public static class WheelView extends View {
        private Paint segmentPaint;
        private Paint textPaint;
        private Paint borderPaint;
        private RectF wheelBounds;
        private MiniGame.FortuneWheelPrize[] prizes;
        private int[] colors;

        public WheelView(Context context, MiniGame.FortuneWheelPrize[] prizes, int[] colors) {
            super(context);
            this.prizes = prizes;
            this.colors = colors;
            init();
        }

        private void init() {
            segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            segmentPaint.setStyle(Paint.Style.FILL);

            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(28);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setFakeBoldText(true);
            textPaint.setShadowLayer(2, 1, 1, Color.BLACK);

            borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(8);
            borderPaint.setColor(Color.parseColor("#FFD700"));
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            int padding = 20;
            wheelBounds = new RectF(padding, padding, w - padding, h - padding);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (prizes == null || wheelBounds == null) return;

            float sweepAngle = 360f / prizes.length;
            float startAngle = -90; // Empezar arriba

            // Dibujar segmentos
            for (int i = 0; i < prizes.length; i++) {
                segmentPaint.setColor(colors[i % colors.length]);
                canvas.drawArc(wheelBounds, startAngle, sweepAngle, true, segmentPaint);

                // Dibujar texto del premio
                drawTextOnSegment(canvas, prizes[i].name, startAngle, sweepAngle);

                startAngle += sweepAngle;
            }

            // Dibujar borde dorado
            canvas.drawOval(wheelBounds, borderPaint);

            // Dibujar líneas separadoras
            Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(Color.parseColor("#1A1A2E"));
            linePaint.setStrokeWidth(3);

            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            float radius = wheelBounds.width() / 2f;

            startAngle = -90;
            for (int i = 0; i < prizes.length; i++) {
                double rad = Math.toRadians(startAngle);
                float endX = centerX + (float)(radius * Math.cos(rad));
                float endY = centerY + (float)(radius * Math.sin(rad));
                canvas.drawLine(centerX, centerY, endX, endY, linePaint);
                startAngle += sweepAngle;
            }
        }

        private void drawTextOnSegment(Canvas canvas, String text, float startAngle, float sweepAngle) {
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            float radius = wheelBounds.width() / 2f * 0.65f;

            float angle = startAngle + sweepAngle / 2;
            double rad = Math.toRadians(angle);

            float x = centerX + (float)(radius * Math.cos(rad));
            float y = centerY + (float)(radius * Math.sin(rad));

            canvas.save();
            canvas.translate(x, y);
            canvas.rotate(angle + 90);

            // Simplificar el texto
            String displayText = text;
            if (text.startsWith("💰")) {
                displayText = text.replace("💰 ", "");
            }
            if (displayText.length() > 8) {
                displayText = displayText.substring(0, 7) + "..";
            }

            canvas.drawText(displayText, 0, 8, textPaint);
            canvas.restore();
        }
    }
}
