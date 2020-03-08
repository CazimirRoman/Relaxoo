package com.cazimir.relaxoo.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.Objects;

public class Sound {

    private String id;
    private int soundPoolId;
    private int streamId;
    private String name;
    private String logoPath;
    private String filePath;
    private boolean playing;
    private float volume = 0.5f;
    private boolean pro;

    public boolean isCustom() {
        return custom;
    }

    private boolean custom;

    private Sound(
            String id,
            int soundPoolId,
            int streamId,
            String name,
            String logoPath,
            String file,
          boolean playing,
          float volume,
          boolean pro,
          boolean custom) {
    this.id = id;
    this.soundPoolId = soundPoolId;
    this.streamId = streamId;
    this.filePath = file;
    this.name = name;
    this.logoPath = logoPath;
    this.playing = playing;
    this.pro = pro;
    this.volume = volume;
    this.custom = custom;
  }

  public Sound() {
  }

  public static Sound withVolume(Sound sound, float volume) {
    return new Sound(
            sound.id,
            sound.soundPoolId,
            sound.streamId,
            sound.name,
            sound.logoPath,
            sound.filePath,
            sound.playing,
            volume,
            sound.pro,
            sound.custom);
  }

  public static Sound withId(Sound sound, String id) {
    return new Sound(
            id,
            sound.soundPoolId,
            sound.streamId,
            sound.name,
            sound.logoPath,
            sound.filePath,
            sound.playing,
            sound.volume,
            sound.pro,
            sound.custom);
  }

  public static Sound withSoundPoolId(Sound sound, int soundPoolId) {
    return new Sound(
            sound.id,
            soundPoolId,
            sound.streamId,
            sound.name,
            sound.logoPath,
            sound.filePath,
            sound.playing,
            sound.volume,
            sound.pro,
            sound.custom);
  }

  public static Sound withStreamId(Sound sound, int streamId) {
    return new Sound(
            sound.id,
            sound.soundPoolId,
            streamId,
            sound.name,
            sound.logoPath,
            sound.filePath,
            sound.playing,
            sound.volume,
            sound.pro,
            sound.custom);
  }

  public static Sound withPlaying(Sound sound) {
    return new Sound(
            sound.id,
            sound.soundPoolId,
            sound.streamId,
            sound.name,
            sound.logoPath,
            sound.filePath,
            !sound.isPlaying(),
            sound.volume,
            sound.pro,
            sound.custom);
  }

  public static Sound withCustom(Sound sound, boolean custom) {
    return new Sound(
            sound.id,
            sound.soundPoolId,
            sound.streamId,
            sound.name,
            sound.logoPath,
            sound.filePath,
            sound.isPlaying(),
            sound.volume,
            sound.pro,
            custom);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Sound sound = (Sound) o;
    return soundPoolId == sound.soundPoolId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(soundPoolId);
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public boolean isPro() {
    return pro;
  }

  public String getLogoPath() {
    return logoPath;
  }

  @Exclude
  public int streamId() {
    return streamId;
  }

  @Exclude
  public boolean isPlaying() {
    return playing;
  }

  @Exclude
  public float volume() {
    return volume;
  }

  @Exclude
  public int soundPoolId() {
    return soundPoolId;
  }

  public String getFilePath() {
    return filePath;
  }

  @NonNull
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(System.lineSeparator());
    sb.append("Sound ");
    sb.append("soundPoolId=").append(soundPoolId);
    sb.append(", streamId=").append(streamId);
    sb.append(", name='").append(name).append('\'');
    sb.append(", logoPath=").append(logoPath);
    sb.append(", getFilePath=").append(filePath);
    sb.append(", playing=").append(playing);
    sb.append(", volume=").append(volume);
    sb.append(", isPro=").append(pro);
    sb.append(", isCustom=").append(custom);
    sb.append('}');
    return sb.toString();
  }

  public static final class SoundBuilder {

    private String id;
    private int soundPoolId;
    private int streamId;
    private String name;
    private String logoPath;
    private String filePath;
    private boolean playing;
    private float volume = 0.5f;
    private boolean pro;
    private boolean custom;

    private SoundBuilder() {}

    public static SoundBuilder aSound() {
      return new SoundBuilder();
    }

    public SoundBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public SoundBuilder withSoundPoolId(Integer soundPoolId) {
      this.soundPoolId = soundPoolId;
      return this;
    }

    public SoundBuilder withStreamId(int streamId) {
      this.streamId = streamId;
      return this;
    }

    public SoundBuilder withName(String name) {
      this.name = name;
      return this;
    }

    public SoundBuilder withLogo(String logo) {
      this.logoPath = logo;
      return this;
    }

    public SoundBuilder withFilePath(String filePath) {
      this.filePath = filePath;
      return this;
    }

    public SoundBuilder withPlaying(boolean playing) {
      this.playing = playing;
      return this;
    }

    public SoundBuilder withVolume(float volume) {
      this.volume = volume;
      return this;
    }

    public SoundBuilder withPro(boolean pro) {
      this.pro = pro;
      return this;
    }

    public SoundBuilder withCustom(boolean custom) {
      this.custom = custom;
      return this;
    }

    public Sound build() {
      return new Sound(
              id, soundPoolId, streamId, name, logoPath, filePath, playing, volume, pro, custom);
    }
  }
}
