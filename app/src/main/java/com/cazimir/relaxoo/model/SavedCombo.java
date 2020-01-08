package com.cazimir.relaxoo.model;

import java.util.List;

public class SavedCombo {

  private final String name;
  private final List<Sound> sounds;

  private SavedCombo(String name, List<Sound> sounds) {
    this.name = name;
    this.sounds = sounds;
  }

  private SavedCombo(Builder builder) {
    name = builder.name;
    sounds = builder.sounds;
  }

  public String name() {
    return name;
  }


  public static final class Builder {
    private String name;
    private List<Sound> sounds;

    public Builder() {
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withSounds(List<Sound> sounds) {
      this.sounds = sounds;
      return this;
    }

    public SavedCombo build() {
      return new SavedCombo(this);
    }
  }
}
