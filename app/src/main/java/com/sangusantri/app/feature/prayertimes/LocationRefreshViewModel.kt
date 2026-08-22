package com.sangusantri.app.feature.prayertimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.usecase.RefreshFromCurrentLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Holds the home-screen widget's "refresh from where I am now" request for the whole app rather
 * than for one screen.
 *
 * Scoped to the navigation host, above the destination, because the tap has to refresh whether the
 * reader lands on Jadwal Sholat or is already sitting on Beranda — both render the same Room flows,
 * so neither needs to run this itself. Jadwal Sholat's own "Izinkan lokasi" button is separate and
 * unchanged: that one asks for the permission, this one never does.
 *
 * It also refreshes once on creation, which is a cold start of the app — open it after travelling
 * and the schedule is for where you are before you look at it. Being a ViewModel is what makes that
 * "once": it survives rotation, so turning the phone does not spend another location read and
 * month fetch.
 */
@HiltViewModel
class LocationRefreshViewModel
@Inject
constructor(
    private val refreshFromCurrentLocation: RefreshFromCurrentLocationUseCase,
) : ViewModel() {
    private var inFlight: Job? = null

    init {
        refresh()
    }

    /**
     * No-ops without the coarse-location permission — the use case checks, and never asks.
     *
     * Ignored while one is already running, so a cold start *from* the widget does not fire twice:
     * [init] here and the tap's own call land within a frame of each other.
     */
    fun refresh() {
        if (inFlight?.isActive == true) return
        inFlight = viewModelScope.launch { refreshFromCurrentLocation() }
    }
}
