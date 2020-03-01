package com.cazimir.relaxoo.model

class AboutItem(val name: MenuItemType, val icon: Int)

enum class MenuItemType(name: String) {
    REMOVE_ADS("Remove Ads"),
    SETTINGS("Settings"),
    SHARE("Share"),
    PRIVACY_POLICY("Privacy policy"),
    RATE_APP("Rate app"),
    MORE_APPS("More apps")
}
