package com.cazimir.relaxoo

import com.cazimir.relaxoo.muting.MutingTest
import com.cazimir.relaxoo.recording_own_sounds.RecordingOwnSoundsTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    RecordingOwnSoundsTest::class,
    MutingTest::class
)
class TestSuite {
}