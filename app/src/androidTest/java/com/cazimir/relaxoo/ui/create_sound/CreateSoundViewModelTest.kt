package com.cazimir.relaxoo.ui.create_sound

//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.repository.IRecordingRepository
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import java.io.File

class CreateSoundViewModelTest {

    @Rule
//    @JvmField // A JUnit Test Rule that swaps the background executor used by the Architecture Components with a different one which executes each task synchronously.
//    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val recordingsObserver: Observer<ArrayList<Recording>> = mock()
    private val mockSoundRepository: IRecordingRepository = mock()
    private val viewModel = CreateSoundViewModel()

    var mockRecording: Recording = Recording.Builder().withFile(File("Test")).withFileName("test.ogg").withPlaying(true).build()
    var mockEditedRecording: Recording = Recording.Builder().withFile(File("TestEdited")).withFileName("testEdited.ogg").withPlaying(true).build()
    val editedName = "TestEdited"
    val captor = ArgumentCaptor.forClass(ArrayList::class.java)

    @Before
    fun setUp() {
        viewModel.recordingsLive.observeForever(recordingsObserver)
        viewModel.repository = mockSoundRepository
    }

    @Test
    fun deleteRecordingCallsDeleteOnRepository() {
        viewModel.deleteRecording(mockRecording)
        verify(mockSoundRepository).deleteRecording(mockRecording)
    }

    @Test
    fun deleteRecordingTriggersObserver() {
        stubSoundRepositoryDeleteRecording(mockRecording)
        stubSoundRepositoryGetAllRecordingFiles(arrayOf())
        viewModel.deleteRecording(mockRecording)
        verify(recordingsObserver).onChanged(arrayListOf())
    }

    @Test
    fun getRecordedFilesReturnsFiles() {
        stubSoundRepositoryGetAllRecordingFiles(arrayOf(mockRecording.file))
        viewModel.refreshList()

        captor.run {
            verify(recordingsObserver).onChanged(capture() as ArrayList<Recording>?)
            assertEquals(mockRecording.file, (value.get(0) as Recording).file)
        }
    }

    @Test
    fun editRecordingCallsEditOnRepository() {

        viewModel.editRecording(mockRecording, editedName)
        verify(mockSoundRepository).editRecording(mockRecording, editedName)
    }

    @Test
    fun editRecordingSuccessfulCallesRefreshList() {
        stubSoundRepositoryEditRecording(mockRecording)
        stubSoundRepositoryGetAllRecordingFiles(arrayOf(mockEditedRecording.file))
        viewModel.editRecording(mockRecording, editedName)

        captor.run {
            verify(recordingsObserver).onChanged(capture() as ArrayList<Recording>?)
            assertEquals(mockEditedRecording.file.name, editedName)
        }
    }

    private fun stubSoundRepositoryDeleteRecording(recording: Recording) {
        whenever(mockSoundRepository.deleteRecording(recording)).thenReturn(true)
    }

    private fun stubSoundRepositoryGetAllRecordingFiles(arrayToReturn: Array<File>) {
        whenever(mockSoundRepository.getRecordings()).thenReturn(arrayToReturn)
    }

    private fun stubSoundRepositoryEditRecording(recording: Recording) {
        whenever(mockSoundRepository.editRecording(recording, editedName)).thenReturn(true)
    }
}
