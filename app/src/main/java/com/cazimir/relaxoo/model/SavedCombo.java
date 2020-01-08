package com.cazimir.relaxoo.model;

import java.util.HashMap;
import java.util.Objects;

public class SavedCombo {

  private final String name;
  private final HashMap<Integer, Integer> soundPoolParameters;
  private boolean playing;

  private SavedCombo(String name, HashMap<Integer, Integer> soundPoolId, boolean playing) {
    this.name = name;
    this.soundPoolParameters = soundPoolId;
    this.playing = playing;
  }

  private SavedCombo(Builder builder) {
    name = builder.name;
    soundPoolParameters = builder.soundPoolParameters;
    playing = builder.playing;
  }

  public static SavedCombo withPlaying(SavedCombo savedCombo, boolean playing) {
    return new SavedCombo(savedCombo.name, savedCombo.soundPoolParameters, playing);
  }

  public HashMap<Integer, Integer> getSoundPoolParameters() {
    return soundPoolParameters;
  }

  public String name() {
    return name;
  }

  public boolean isPlaying() {
    return playing;
  }

  public static final class Builder {
    private String name;
    private HashMap<Integer, Integer> soundPoolParameters;
    private boolean playing;

    public Builder() {}

    public Builder withName(String val) {
      name = val;
      return this;
    }

    public Builder withSoundPoolParameters(HashMap<Integer, Integer> val) {
      soundPoolParameters = val;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SavedCombo that = (SavedCombo) o;
    return soundPoolParameters.equals(that.soundPoolParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(soundPoolParameters);
  }
}
