package com.cazimir.relaxoo.repository

import androidx.lifecycle.LiveData
import com.cazimir.relaxoo.model.Sound

interface ISoundRepository {
    fun getSounds(): LiveData<List<Sound>>
}