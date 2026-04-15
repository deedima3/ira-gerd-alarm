package com.example.cutesyalarm.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.cutesyalarm.BuildConfig
import com.example.cutesyalarm.service.UpdateDownloadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {

    companion object {
        // GitHub repository configuration
        // ⚠️ UPDATE THESE VALUES after creating your GitHub repository
        const val GITHUB_OWNER = "YourUsername"  // Replace with your GitHub username
        const val GITHUB_REPO = "iras-cute-alarm"
        
        // GitHub Releases API URL (fetches latest release)
        const val GITHUB_RELEASES_API = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
        
        // Fallback: Raw URL to update.json in the repo main branch
        const val FALLBACK_UPDATE_URL = "https://raw.githubusercontent.com/$GITHUB_OWNER/$GITHUB_REPO/main/update.json"

        // Preference keys
        private const val PREFS_NAME = "update_prefs"
        private const val KEY_LAST_CHECK = "last_check_time"
        private const val KEY_IGNORE_VERSION = "ignore_version"
        private const val CHECK_INTERVAL_HOURS = 24 // Check once per day
        
        // GitHub tag prefix to extract version
        private const val TAG_PREFIX = "v"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val downloadUrl: String,
        val releaseNotes: String,
        val forceUpdate: Boolean
    )

    /**
     * Check for updates from GitHub Releases API
     * Returns null if no update available, UpdateInfo if update is available
     */
    suspend fun checkForUpdate(): UpdateInfo? {
        return withContext(Dispatchers.IO) {
            try {
                // Try GitHub Releases API first
                val updateFromGitHub = checkGitHubReleases()
                if (updateFromGitHub != null) {
                    return@withContext updateFromGitHub
                }
                
                // Fallback to update.json in repo
                return@withContext checkUpdateJson(FALLBACK_UPDATE_URL)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                // Save check time
                prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
            }
        }
    }

    /**
     * Check GitHub Releases API for the latest release
     */
    private fun checkGitHubReleases(): UpdateInfo? {
        val url = URL(GITHUB_RELEASES_API)
        val connection = url.openConnection() as HttpURLConnection
        
        connection.apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            // GitHub API requires a User-Agent header
            setRequestProperty("User-Agent", "IrasCuteAlarm-OTA-Checker")
        }

        return try {
            val responseCode = connection.responseCode
            if (responseCode != 200) {
                connection.disconnect()
                return null
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val json = JSONObject(response)
            
            // Extract version from tag_name (e.g., "v1.1.0" -> versionCode 1001000)
            val tagName = json.getString("tag_name")
            val versionName = tagName.removePrefix(TAG_PREFIX)
            val serverVersionCode = versionNameToCode(versionName)
            val currentVersionCode = getCurrentVersionCode()

            if (serverVersionCode > currentVersionCode) {
                // Find APK asset
                val assets = json.getJSONArray("assets")
                var apkUrl = ""
                
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk")) {
                        apkUrl = asset.getString("browser_download_url")
                        break
                    }
                }
                
                if (apkUrl.isNotEmpty()) {
                    UpdateInfo(
                        versionCode = serverVersionCode,
                        versionName = versionName,
                        downloadUrl = apkUrl,
                        releaseNotes = json.getString("body"),
                        forceUpdate = false
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            connection.disconnect()
            throw e
        }
    }

    /**
     * Check update.json file in the repository
     */
    private fun checkUpdateJson(updateUrl: String): UpdateInfo? {
        val url = URL(updateUrl)
        val connection = url.openConnection() as HttpURLConnection
        
        connection.apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Accept", "application/json")
        }

        return try {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val json = JSONObject(response)
            val serverVersionCode = json.getInt("versionCode")
            val currentVersionCode = getCurrentVersionCode()

            if (serverVersionCode > currentVersionCode) {
                UpdateInfo(
                    versionCode = serverVersionCode,
                    versionName = json.getString("versionName"),
                    downloadUrl = json.getString("downloadUrl"),
                    releaseNotes = json.optString("releaseNotes", ""),
                    forceUpdate = json.optBoolean("forceUpdate", false)
                )
            } else {
                null
            }
        } catch (e: Exception) {
            connection.disconnect()
            throw e
        }
    }

    /**
     * Convert version name (e.g., "1.1.0") to version code (e.g., 1001000)
     */
    private fun versionNameToCode(versionName: String): Int {
        val parts = versionName.split(".")
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
        return major * 1000000 + minor * 1000 + patch
    }

    /**
     * Check if we should check for updates (based on interval)
     */
    fun shouldCheckForUpdate(): Boolean {
        val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0)
        val intervalMillis = CHECK_INTERVAL_HOURS * 60 * 60 * 1000
        return System.currentTimeMillis() - lastCheck > intervalMillis
    }

    /**
     * Check if user has ignored this version
     */
    fun isVersionIgnored(versionName: String): Boolean {
        return prefs.getString(KEY_IGNORE_VERSION, "") == versionName
    }

    /**
     * Ignore this version (don't prompt again)
     */
    fun ignoreVersion(versionName: String) {
        prefs.edit().putString(KEY_IGNORE_VERSION, versionName).apply()
    }

    /**
     * Get current app version code
     */
    fun getCurrentVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }

    /**
     * Get current app version name
     */
    fun getCurrentVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    /**
     * Check if app can install unknown sources (required for OTA updates)
     */
    fun canInstallUnknownSources(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Request permission to install unknown sources
     */
    fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }

    /**
     * Start downloading the update
     */
    fun startUpdateDownload(updateInfo: UpdateInfo) {
        UpdateDownloadService.startDownload(context, updateInfo.downloadUrl, updateInfo.versionName)
    }

    /**
     * Open app settings to allow install from this source
     */
    fun openInstallSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
    
    /**
     * Get the update URL for manual checking
     */
    fun getUpdateUrl(): String {
        return GITHUB_RELEASES_API
    }
}
