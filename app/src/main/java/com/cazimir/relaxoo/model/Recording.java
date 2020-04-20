package com.cazimir.relaxoo.model;

import java.io.File;
import java.util.Objects;

public class Recording {
    private final String id;
    private final File file;
    private final String fileName;
    private final boolean playing;

    private Recording(String id, File file, String fileName, boolean playing) {
        this.id = id;
        this.file = file;
        this.fileName = fileName;
        this.playing = playing;
    }

    private Recording(Builder builder, String fileName) {
        this.id = builder.id;
        file = builder.file;
        playing = builder.playing;
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public boolean isPlaying() {
        return playing;
    }

    public String getId() {
        return id;
    }

    public static final class Builder {
        private String id;
        private File file;
        private boolean playing;
        private String fileName;

        public Builder() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Builder builder = (Builder) o;
            return fileName.equals(builder.fileName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileName);
        }

        public Builder withFile(File val) {
            file = val;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withPlaying(boolean val) {
            playing = val;
            return this;
        }

        public Builder withFileName(String val) {
            fileName = val;
            return this;
        }

        public Recording build() {
            return new Recording(this, fileName);
        }
    }
}
