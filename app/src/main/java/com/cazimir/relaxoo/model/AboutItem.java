package com.cazimir.relaxoo.model;

public class AboutItem {
    private int icon;
    private String name;

    private AboutItem(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public static final class AboutItemBuilder {
        private int icon;
        private String name;

        public static AboutItemBuilder anAboutItem() {
            return new AboutItemBuilder();
        }

        public AboutItemBuilder withIcon(int icon) {
            this.icon = icon;
            return this;
        }

        public AboutItemBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public AboutItem build() {
            return new AboutItem(this.icon, this.name);
        }
    }
}
