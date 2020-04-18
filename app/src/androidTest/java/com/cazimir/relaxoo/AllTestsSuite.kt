package com.cazimir.relaxoo

import TimerTest
import com.cazimir.relaxoo.muting.MutingTest
import com.cazimir.relaxoo.recording_own_sounds.RecordingOwnSoundsTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        GeneralTest::class,
        PlayingAndStoppingTest::class,
        MutingTest::class,
        RandomTest::class,
        RecordingOwnSoundsTest::class,
        ViewRotationTest::class,
        TimerTest::class,
        MutingTest::class
)
class AllTestsSuite {
}