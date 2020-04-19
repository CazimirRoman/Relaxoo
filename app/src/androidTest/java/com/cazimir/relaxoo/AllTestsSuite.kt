package com.cazimir.relaxoo

import TimerTest
import com.cazimir.relaxoo.general.GeneralTest
import com.cazimir.relaxoo.muting.MutingTest
import com.cazimir.relaxoo.recording_own_sounds.RecordingOwnSoundsTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        GeneralTest::class,
        MutingTest::class,
        PlayingAndStoppingTest::class,
        RandomTest::class,
        RecordingOwnSoundsTest::class,
        RotationTest::class,
        ComboTest::class,
        TimerTest::class
)
class AllTestsSuite