package com.cazimir.relaxoo.model;

import java.util.List;
import java.util.Objects;

public class SavedCombo {

  private final String name;
  private final List<Sound> sounds;
  private boolean playing;

  private SavedCombo(String name, List<Sound> sounds, boolean playing) {
    this.name = name;
    this.sounds = sounds;
    this.playing = playing;
  }

  private SavedCombo(Builder builder) {
    name = builder.name;
    sounds = builder.sounds;
    playing = builder.playing;
  }

  public static SavedCombo withPlaying(SavedCombo savedCombo, boolean playing) {
    return new SavedCombo(savedCombo.name, savedCombo.sounds, playing);
  }

  public List<Sound> getSounds() {
    return sounds;
  }

  public String name() {
    return name;
  }

  public boolean isPlaying() {
    return playing;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SavedCombo that = (SavedCombo) o;
    return sounds.equals(that.sounds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sounds);
  }

  public static final class Builder {
    private String name;
    private List<Sound> sounds;
    private boolean playing;

    public Builder() {
    }

    public Builder withName(String val) {
      name = val;
      return this;
    }

    public Builder withSoundPoolParameters(List<Sound> val) {
      sounds = val;
      return this;
    }

    public Builder withPlaying(boolean val) {
      playing = val;
      return this;
    }

    public SavedCombo build() {
      return new SavedCombo(this);
    }
  }
}
