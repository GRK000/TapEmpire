package com.example.juego;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private RecyclerView achievementsRecycler;
    private TextView tvProgress;
    private AchievementAdapter adapter;
    private GameState state = GameState.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements_pro);

        tvProgress = findViewById(R.id.tvProgress);
        achievementsRecycler = findViewById(R.id.achievementsRecycler);

        // Configurar RecyclerView
        achievementsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AchievementAdapter(state.getAchievements(), state);
        achievementsRecycler.setAdapter(adapter);

        updateHeader();
    }

    private void updateHeader() {
        List<Achievement> achievements = state.getAchievements();
        int unlocked = 0;
        for (Achievement a : achievements) {
            if (a.isUnlocked()) unlocked++;
        }
        tvProgress.setText(unlocked + "/" + achievements.size() + " Desbloqueados");
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        updateHeader();
    }
}
