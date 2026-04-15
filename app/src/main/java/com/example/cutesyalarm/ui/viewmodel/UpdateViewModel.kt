package com.example.cutesyalarm.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cutesyalarm.util.UpdateManager
import kotlinx.coroutines.launch

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val updateManager = UpdateManager(application)

    private val _updateInfo = mutableStateOf<UpdateManager.UpdateInfo?>(null)
    val updateInfo: State<UpdateManager.UpdateInfo?> = _updateInfo

    private val _isChecking = mutableStateOf(false)
    val isChecking: State<Boolean> = _isChecking

    private val _hasInstallPermission = mutableStateOf(updateManager.canInstallUnknownSources())
    val hasInstallPermission: State<Boolean> = _hasInstallPermission

    private val _currentVersion = mutableStateOf(updateManager.getCurrentVersionName())
    val currentVersion: State<String> = _currentVersion

    init {
        // Check for updates on startup (only once per day)
        if (updateManager.shouldCheckForUpdate()) {
            checkForUpdate()
        }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            _isChecking.value = true
            try {
                val update = updateManager.checkForUpdate()
                // Don't show if user ignored this version
                if (update != null && !updateManager.isVersionIgnored(update.versionName)) {
                    _updateInfo.value = update
                }
            } catch (e: Exception) {
                // Silent fail - don't bother user with update check failures
            } finally {
                _isChecking.value = false
            }
        }
    }

    fun downloadUpdate() {
        _updateInfo.value?.let { update ->
            updateManager.startUpdateDownload(update)
            dismissUpdateDialog()
        }
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }

    fun ignoreUpdate() {
        _updateInfo.value?.let { update ->
            updateManager.ignoreVersion(update.versionName)
            dismissUpdateDialog()
        }
    }

    fun requestInstallPermission() {
        updateManager.requestInstallPermission()
    }

    fun checkInstallPermission() {
        _hasInstallPermission.value = updateManager.canInstallUnknownSources()
    }
}
