package com.cazimir.relaxoo

data class PreconditionsToStartFetchingData(val isFragmentStarted: Boolean = false, val arePermissionsGranted: Boolean = false, val isInternetUp: Boolean = false)