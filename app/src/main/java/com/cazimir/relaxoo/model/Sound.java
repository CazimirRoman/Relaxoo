package com.cazimir.relaxoo.model;

public class Sound {

    private String name;
    private int drawable;
    private boolean playing;
    private String backgroundColor;
    private int volume;

    public Sound(String name, int drawable) {
        this.name = name;
        this.drawable = drawable;
    }

    public String name() {
        return this.name;
    }
}
