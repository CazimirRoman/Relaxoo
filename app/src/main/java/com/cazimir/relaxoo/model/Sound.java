package com.cazimir.relaxoo.model;

import androidx.annotation.NonNull;

public class Sound {

  private final int soundPoolId;
  private final int streamId;
  private final String name;
  private final int drawable;
  private final String filePath;
  private final boolean playing;
  private final float volume = 0.5f;
  private final boolean pro;

  public boolean pro() {
    return pro;
  }

  private Sound(
          int soundPoolId,
          int streamId,
          String name,
          int drawable,
          String file,
          boolean playing,
          float volume, boolean pro) {
    this.soundPoolId = soundPoolId;
    this.streamId = streamId;
    this.filePath = file;
    this.name = name;
    this.drawable = drawable;
    this.playing = playing;
    this.pro = pro;
  }

  public static Sound newSound(
          String name, int graphic, String soundFile, boolean playing, float volume, boolean pro) {
    return new Sound(-1, -1, name, graphic, soundFile, playing, volume, pro);
  }

  public static Sound withSoundPoolId(Sound sound, int soundPoolId) {
    return new Sound(
        soundPoolId,
        sound.streamId,
        sound.name,
        sound.drawable,
            sound.filePath,
        sound.playing,
        sound.volume, sound.pro);
  }

  public static Sound withStreamId(Sound sound, int streamId) {
    return new Sound(
        sound.soundPoolId,
        streamId,
        sound.name,
        sound.drawable,
            sound.filePath,
        sound.playing,
        sound.volume, sound.pro);
  }

  public static Sound withPlaying(Sound sound) {
    return new Sound(
        sound.soundPoolId,
        sound.streamId,
        sound.name,
        sound.drawable,
            sound.filePath,
        !sound.isPlaying(),
        sound.volume, sound.pro);
  }

  public int drawable() {
    return drawable;
  }

  public int streamId() {
    return streamId;
  }

  public String name() {
    return this.name;
  }

  public boolean isPlaying() {
    return playing;
  }

  public float volume() {
    return volume;
  }

  public int soundPoolId() {
    return soundPoolId;
  }

  public String filePath() {
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
    sb.append(", drawable=").append(drawable);
  sb.append(", filePath=").append(filePath);
    sb.append(", playing=").append(playing);
    sb.append(", volume=").append(volume);
    sb.append(", pro=").append(pro);
    sb.append('}');
    return sb.toString();
  }

  public static final class SoundBuilder {
    private int soundPoolId;
    private int streamId;
    private String name;
    private int drawable;
    private String filePath;
    private boolean playing;
    private float volume = 0.5f;
    private boolean pro;


    private SoundBuilder() {}

    public static SoundBuilder aSound() {
      return new SoundBuilder();
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

    public SoundBuilder withDrawable(int drawable) {
      this.drawable = drawable;
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

    public Sound build() {
      return new Sound(soundPoolId, streamId, name, drawable, filePath, playing, volume, pro);
    }
  }
}
