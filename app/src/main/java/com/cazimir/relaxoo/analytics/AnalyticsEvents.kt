package com.cazimir.relaxoo.analytics

import android.os.Bundle
import com.cazimir.relaxoo.model.Sound

class AnalyticsEvents {

    companion object {

        fun soundClicked(sound: Sound): Pair<String, Bundle> {
            // the bundle is used to put additional data about a parameter (name, id, isPlaying etc)
            val bundle = Bundle()
            bundle.putString(BundleKey.SOUND_NAME.key, sound.name)
            return Pair(EventKey.SOUND_CLICKED.name, bundle)
        }

        fun boughtPro(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.BOUGHT_PRO.key, true)
            return Pair(EventKey.BOUGHT_PRO.name, bundle)
        }

        fun muteClicked(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.MUTE_CLICKED.key, true)
            return Pair(EventKey.MUTE_CLICKED.name, bundle)
        }

        fun randomClicked(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.RANDOM_CLICKED.key, true)
            return Pair(EventKey.RANDOM_CLICKED.name, bundle)
        }

        fun saveComboClicked(comboName: String): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.SAVE_COMBO_CLICKED.key, true)
            bundle.putString(BundleKey.SAVED_COMBO_NAME.key, comboName)
            return Pair(EventKey.SAVE_COMBO_CLICKED.name, bundle)
        }

        fun comboTriggered(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.COMBO_TRIGGERED.key, true)
            return Pair(EventKey.COMBO_TRIGGERED.name, bundle)
        }

        fun timerClicked(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.TIMER_CLICKED.key, true)
            return Pair(EventKey.TIMER_CLICKED.name, bundle)
        }

        fun comboDeleted(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.COMBO_DELETED.key, true)
            return Pair(EventKey.COMBO_DELETED.name, bundle)
        }

        fun pinnedToDashboard(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.PINNED_TO_DASHBOARD.key, true)
            return Pair(EventKey.PINNED_TO_DASHBOARD.name, bundle)
        }

        fun removeFromDashboard(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.REMOVE_FROM_DASHBOARD.key, true)
            return Pair(EventKey.REMOVE_FROM_DASHBOARD.name, bundle)
        }

        fun editRecording(newName: String): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.EDIT_RECORDING.key, true)
            bundle.putString(BundleKey.RECORDING_NAME.key, newName)
            return Pair(EventKey.EDIT_RECORDING.name, bundle)
        }

        fun recordClicked(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.RECORD_CLICKED.key, true)
            return Pair(EventKey.RECORD_CLICKED.name, bundle)
        }

        fun shareClicked(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.SHARE_CLICKED.key, true)
            return Pair(EventKey.SHARE_CLICKED.name, bundle)
        }

        fun removedAds(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.REMOVED_ADS.key, true)
            return Pair(EventKey.REMOVE_ADS.name, bundle)
        }

        fun privacyPolicyClicked(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.PRIVACY_POLICY_CLICKED.key, true)
            return Pair(EventKey.PRIVACY_POLICY_CLICKED.name, bundle)
        }

        fun rateAppClicked(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.RATE_APP_CLICKED.key, true)
            return Pair(EventKey.RATE_APP_CLICKED.name, bundle)
        }

        fun moreAppsClicked(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.MORE_APPS_CLICKED.key, true)
            return Pair(EventKey.MORE_APPS_CLICKED.name, bundle)
        }

        fun shutdownAppFromNotification(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.SHUTDOWN_APP_FROM_NOTIFICATION.key, true)
            return Pair(EventKey.SHUTDOWN_APP_FROM_NOTIFICATION.name, bundle)
        }

        fun shutdownAppFromBack(): Pair<String, Bundle> {
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.SHUTDOWN_APP_BACK.key, true)
            return Pair(EventKey.SHUTDOWN_APP_BACK.name, bundle)
        }
    }
}

enum class EventKey {
    SOUND_CLICKED,
    MUTE_CLICKED,
    RANDOM_CLICKED,
    SAVE_COMBO_CLICKED,
    TIMER_CLICKED,
    BOUGHT_PRO,
    COMBO_TRIGGERED,
    COMBO_DELETED,
    PINNED_TO_DASHBOARD,
    REMOVE_FROM_DASHBOARD,
    EDIT_RECORDING,
    RECORD_CLICKED,
    SHARE_CLICKED,
    REMOVE_ADS,
    PRIVACY_POLICY_CLICKED,
    RATE_APP_CLICKED,
    MORE_APPS_CLICKED,
    SHUTDOWN_APP_FROM_NOTIFICATION,
    SHUTDOWN_APP_BACK
}

enum class BundleKey(val key: String) {
    //you can insert more items into the bundle. this is what this enum class is for. in our case we just have the sound name. for bought pro we do not need any additional information
    SOUND_NAME("sound_name"),
    BOUGHT_PRO("bought_pro"),
    MUTE_CLICKED("mute_clicked"),
    RANDOM_CLICKED("random_clicked"),
    SAVE_COMBO_CLICKED("save_combo_clicked"),
    SAVED_COMBO_NAME("saved_combo_name"),
    TIMER_CLICKED("timer_clicked"),
    COMBO_TRIGGERED("combo_triggered"),
    COMBO_DELETED("combo_deleted"),
    PINNED_TO_DASHBOARD("pinned_to_dashboard"),
    REMOVE_FROM_DASHBOARD("remove_from_dashboard"),
    RECORDING_NAME("recording_name"),
    EDIT_RECORDING("edit_recording"),
    RECORD_CLICKED("record_clicked"),
    SHARE_CLICKED("share_clicked"),
    REMOVED_ADS("removed_ads"),
    PRIVACY_POLICY_CLICKED("privacy_policy_clicked"),
    RATE_APP_CLICKED("rate_app_clicked"),
    MORE_APPS_CLICKED("more_apps_clicked"),
    SHUTDOWN_APP_FROM_NOTIFICATION("shutdown_app_from_notification"),
    SHUTDOWN_APP_BACK("shutdown_app_back")
}