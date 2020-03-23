package com.cazimir.relaxoo.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.cazimir.relaxoo.BuildConfig
import com.cazimir.relaxoo.ui.about.AboutFragment
import com.cazimir.relaxoo.ui.admin_add_sound.AdminAddSoundFragment
import com.cazimir.relaxoo.ui.create_sound.CreateSoundFragment
import com.cazimir.relaxoo.ui.favorites.FavoritesFragment
import com.cazimir.relaxoo.ui.sound_grid.SoundGridFragment

class PagerAdapter(val fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val soundGridFragment = SoundGridFragment.newInstance()
    val favoritesFragment = FavoritesFragment.newInstance()
    // val createSoundFragment = BlankFragment2.newInstance("sdgsgd", "sdfds")
    val createSoundFragment = CreateSoundFragment.newInstance()
    val aboutFragment = AboutFragment.newInstance()
    val adminAddSoundFragment = AdminAddSoundFragment.newInstance()

    override fun getItem(position: Int): Fragment {

        Log.d("PagerAdapter", "getItem: called with position: $position")

        when (position) {
            0 -> return soundGridFragment
            1 -> return favoritesFragment
            2 -> return createSoundFragment
            3 -> return aboutFragment
            else -> return adminAddSoundFragment
        }
    }

    override fun getCount(): Int {
        return NUMBER_OF_FRAGMENTS
    }

    companion object {
        val NUMBER_OF_FRAGMENTS = if (BuildConfig.DEBUG) 5 else 4
    }
}
