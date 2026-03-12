package com.example.juego;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MissionsActivity extends AppCompatActivity {

    private RecyclerView missionsRecycler;
    private MissionAdapter adapter;
    private GameState state = GameState.getInstance();
    private Handler updateHandler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions_pro);

        missionsRecycler = findViewById(R.id.missionsRecycler);

        missionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MissionAdapter(state.getDailyMissions(), state, (position, mission) -> {
            if (mission.isCompleted() && !mission.isClaimed()) {
                if (state.claimMissionReward(position)) {
                    adapter.notifyItemChanged(position);
                    updateUI();
                    Toast.makeText(this, "🎉 ¡Recompensa reclamada! +" +
                        GameState.fmt(mission.getCoinReward()) + " coins", Toast.LENGTH_SHORT).show();
                }
            }
        });
        missionsRecycler.setAdapter(adapter);

        updateUI();
        startUpdateLoop();
    }

    private void updateUI() {
        // Actualizar la lista de misiones
        adapter.notifyDataSetChanged();
    }

    private void startUpdateLoop() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = () -> {
            adapter.notifyDataSetChanged();
            updateUI();
            updateHandler.postDelayed(updateRunnable, 1000);
        };
        updateHandler.post(updateRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (updateHandler != null) updateHandler.removeCallbacks(updateRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUpdateLoop();
    }
}
