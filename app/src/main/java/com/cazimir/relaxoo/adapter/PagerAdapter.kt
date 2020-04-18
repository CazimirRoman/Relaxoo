package com.cazimir.relaxoo.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cazimir.relaxoo.BuildConfig
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.ui.about.AboutFragment
import com.cazimir.relaxoo.ui.admin_add_sound.AdminAddSoundFragment
import com.cazimir.relaxoo.ui.create_sound.CreateSoundFragment
import com.cazimir.relaxoo.ui.favorites.FavoritesFragment
import com.cazimir.relaxoo.ui.sound_grid.SoundGridFragment

class PagerAdapter(fragmentActivity: MainActivity) : FragmentStateAdapter(fragmentActivity) {

    companion object {
        val NUMBER_OF_FRAGMENTS = if (BuildConfig.DEBUG) 5 else 4
    }

    override fun getItemCount(): Int {
        return NUMBER_OF_FRAGMENTS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SoundGridFragment.newInstance()
            1 -> FavoritesFragment.newInstance()
            2 -> CreateSoundFragment.newInstance()
            3 -> AboutFragment.newInstance()
            else -> AdminAddSoundFragment.newInstance()
        }
    }
}
