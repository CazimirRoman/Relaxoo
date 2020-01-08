package com.cazimir.relaxoo.model;

public class Sound {

  private final int soundPoolId;
  private final int streamId;
  private final String name;
  private final int drawable;
  private final int file;
  private final boolean playing;
  private final float volume = 0.5f;

  private Sound(
      int soundPoolId,
      int streamId,
      String name,
      int drawable,
      int file,
      boolean playing,
      float volume) {
    this.soundPoolId = soundPoolId;
    this.streamId = streamId;
    this.file = file;
    this.name = name;
    this.drawable = drawable;
    this.playing = playing;
  }

  public static Sound newSound(
      String name, int graphic, int soundFile, boolean playing, float volume) {
    return new Sound(-1, -1, name, graphic, soundFile, playing, volume);
  }

  public static Sound withSoundPoolId(Sound sound, int soundPoolId) {
    return new Sound(
        soundPoolId,
        sound.streamId,
        sound.name,
        sound.drawable,
        sound.file,
        sound.playing,
        sound.volume);
  }

  public static Sound withStreamId(Sound sound, int streamId) {
    return new Sound(
        sound.soundPoolId,
        streamId,
        sound.name,
        sound.drawable,
        sound.file,
        sound.playing,
        sound.volume);
  }

  public static Sound withPlaying(Sound sound) {
    return new Sound(
        sound.soundPoolId,
        sound.streamId,
        sound.name,
        sound.drawable,
        sound.file,
        !sound.isPlaying(),
        sound.volume);
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

  public int file() {
    return file;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer(System.lineSeparator());
    sb.append("Sound ");
    sb.append("{soundPoolId=").append(soundPoolId);
    sb.append(", streamId=").append(streamId);
    sb.append(", name='").append(name).append('\'');
    sb.append(", drawable=").append(drawable);
    sb.append(", file=").append(file);
    sb.append(", playing=").append(playing);
    sb.append(", volume=").append(volume);
    sb.append('}');
    return sb.toString();
}

  public static final class SoundBuilder {
    private int soundPoolId;
    private int streamId;
    private String name;
    private int drawable;
    private int file;
    private boolean playing;
    private float volume = 0.5f;

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

    public SoundBuilder withFile(int file) {
      this.file = file;
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

    public Sound build() {
      Sound sound = new Sound(soundPoolId, streamId, name, drawable, file, playing, volume);
      return sound;
    }
  }
}
