/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
    val database: SleepDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private val tonight = MutableLiveData<SleepNight?>()
    val startButtonVisible = Transformations.map(tonight) { sleepNight ->
        sleepNight == null
    }
    val stopButtonVisible = Transformations.map(tonight) { sleepNight ->
        sleepNight != null
    }

    val nights = database.getAllNights()

    val clearButtonVisible = Transformations.map(nights) { sleepNights ->
        sleepNights?.isNotEmpty()
    }

    private val _navigateToSleepQuality = MutableLiveData<SleepNight?>()
    val navigateToSleepQuality: LiveData<SleepNight?> get() = _navigateToSleepQuality

    private val _showSnackBarEvent = MutableLiveData<Boolean>()
    val showSnackBarEvent: LiveData<Boolean> get() = _showSnackBarEvent

    private val _navigateToSleepDetail = MutableLiveData<Long?>()
    val navigateToSleepDetail: LiveData<Long?> get() = _navigateToSleepDetail

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        viewModelScope.launch {
            tonight.value = getTonightFromDatabase()
        }

    }

    /**
     *
     * @return SleepNight? returns the current started SleepNight,
     * null otherwise
     */
    private suspend fun getTonightFromDatabase(): SleepNight? {
        var night = database.getTonight()
        // If the start and the end times are not the same, meaning
        // that the night has already been completed return null
        if (night?.endTimeMilli != night?.startTimeMilli) {
            night = null
        }
        return night
    }

    fun onStartTracking() {
        viewModelScope.launch {
            val newNight = SleepNight()
            insert(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun insert(newNight: SleepNight) {
        database.insert(newNight)
    }

    fun onStopTracking() {
        viewModelScope.launch {
            // If no current night tracking started return
            val oldNight = tonight.value ?: return@launch
            // If there is current night tracking already started
            // Update the end time
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)

            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(night: SleepNight) {
        database.update(night)
    }

    fun onClear() {
        viewModelScope.launch {
            clear()
            tonight.value = null
            _showSnackBarEvent.value = true
        }
    }

    private suspend fun clear() {
        database.clear()
    }

    fun doneNavigation() {
        _navigateToSleepQuality.value = null
    }

    fun doneShowingSnackBar() {
        _showSnackBarEvent.value = false
    }

    fun onSleepNightClicked(nightId: Long) {
        _navigateToSleepDetail.value = nightId
    }

    fun onSleepDetailNavigated() {
        _navigateToSleepDetail.value = null
    }
}

