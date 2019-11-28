package com.cazimir.relaxoo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.cazimir.relaxoo.ui.soundlist.SoundListFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SoundListFragment.newInstance())
                    .commitNow();
        }
    }
}
