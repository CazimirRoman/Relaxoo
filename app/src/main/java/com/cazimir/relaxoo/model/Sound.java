package com.cazimir.relaxoo.model;

public class Sound {

    private int id;
    private String name;
    private int drawable;
    private boolean playing;
    private String backgroundColor;
    private int volume = 50;

    public Sound(String name, int drawable) {
        this.name = name;
        this.drawable = drawable;
    }

    public String name() {
        return this.name;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
