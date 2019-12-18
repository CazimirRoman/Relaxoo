package com.cazimir.relaxoo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.cazimir.relaxoo.adapter.SampleAdapter;
import com.cazimir.relaxoo.ui.sound_grid.SoundGridFragment;

public class MainActivity extends FragmentActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    ViewPager pager = findViewById(R.id.pager);

    pager.setAdapter(new SampleAdapter(getSupportFragmentManager()));

  }
}
