package com.cazimir.relaxoo;

import androidx.annotation.NonNull;

class MergePermissionFragmentStarted {
    private final boolean fragmentStarted;
    private final boolean permissionsGranted;

    private MergePermissionFragmentStarted(Builder builder) {
        fragmentStarted = builder.fragmentStarted;
        permissionsGranted = builder.permissionsGranted;
    }

    static MergePermissionFragmentStarted withPermissionGranted(
            MergePermissionFragmentStarted old, boolean permissionGranted) {
        return new MergePermissionFragmentStarted(
                new Builder()
                        .withFragmentStarted(old.fragmentStarted)
                        .withPermissionsGranted(permissionGranted));
    }

    static MergePermissionFragmentStarted withFragmentInstantiated(
            MergePermissionFragmentStarted combined, boolean fragmentInstantiated) {
        return new MergePermissionFragmentStarted(
                new Builder()
                        .withFragmentStarted(fragmentInstantiated)
                        .withPermissionsGranted(combined.permissionsGranted));
    }

    boolean isFragmentStarted() {
        return fragmentStarted;
    }

    @Override
    @NonNull
    public String toString() {
        String sb = System.lineSeparator() + "MergePermissionFragmentStarted{" +
                "soundGridFragmentStarted=" + fragmentStarted +
                ", permissionsGranted=" + permissionsGranted +
                '}';
        return sb;
    }

    boolean isPermissionsGranted() {
        return permissionsGranted;
    }

    static final class Builder {
        private boolean fragmentStarted;
        private boolean permissionsGranted;

        Builder() {
        }

        Builder withFragmentStarted(boolean val) {
            fragmentStarted = val;
            return this;
        }

        Builder withPermissionsGranted(boolean val) {
            permissionsGranted = val;
            return this;
        }

        MergePermissionFragmentStarted build() {
            return new MergePermissionFragmentStarted(this);
        }
    }
}
