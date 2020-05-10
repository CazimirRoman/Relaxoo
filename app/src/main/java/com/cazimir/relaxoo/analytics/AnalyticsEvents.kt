package com.cazimir.relaxoo.analytics

import android.os.Bundle
import com.cazimir.relaxoo.model.Sound

class AnalyticsEvents {

    companion object {

        fun soundClicked(sound: Sound): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putString(BundleKey.SOUND_NAME.bundleKey, sound.name)
            return Pair(EventKey.SOUND_CLICKED.name, bundle)
        }
    }
}

enum class EventKey {
    SOUND_CLICKED
}

enum class BundleKey(val bundleKey: String) {
    SOUND_NAME("sound_name")
}