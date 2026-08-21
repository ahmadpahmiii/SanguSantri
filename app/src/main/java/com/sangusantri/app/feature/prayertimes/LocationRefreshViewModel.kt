package com.sangusantri.app.feature.prayertimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.usecase.RefreshFromCurrentLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
 */
@HiltViewModel
class LocationRefreshViewModel
@Inject
constructor(
    private val refreshFromCurrentLocation: RefreshFromCurrentLocationUseCase,
) : ViewModel() {
    /** Caller must have checked the coarse-location permission first. */
    fun refresh() {
        viewModelScope.launch { refreshFromCurrentLocation() }
    }
}
