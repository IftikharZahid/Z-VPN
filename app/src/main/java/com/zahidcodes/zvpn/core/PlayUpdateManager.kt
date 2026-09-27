package com.zahidcodes.zvpn.core

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.firestore.FirebaseFirestore
import com.zahidcodes.zvpn.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Information regarding Google Play Console update status.
 */
data class PlayAppUpdateDetails(
  val isForceUpdate: Boolean = false,
  val currentVersionCode: Int = BuildConfig.VERSION_CODE,
  val currentVersionName: String = BuildConfig.VERSION_NAME,
  val latestVersionCode: Int = BuildConfig.VERSION_CODE,
  val latestVersionName: String = BuildConfig.VERSION_NAME,
  val releaseNotes: String = "Important security fixes, VPN tunnel speed optimizations, and enhanced connectivity.",
  val playStoreUrl: String = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}",
  val isPlayNativeAllowed: Boolean = false
)

sealed class PlayUpdateStatus {
  object Idle : PlayUpdateStatus()
  object Checking : PlayUpdateStatus()
  object UpToDate : PlayUpdateStatus()
  data class ForceUpdateRequired(val details: PlayAppUpdateDetails) : PlayUpdateStatus()
  data class UpdateAvailable(val details: PlayAppUpdateDetails) : PlayUpdateStatus()
  data class Error(val message: String) : PlayUpdateStatus()
}

/**
 * Singleton manager for Google Play In-App Updates and forced version synchronization.
 */
object PlayUpdateManager {
  private const val TAG = "PlayUpdateManager"
  const val REQUEST_CODE_IMMEDIATE_UPDATE = 8899

  private var appUpdateManager: AppUpdateManager? = null
  private var cachedAppUpdateInfo: AppUpdateInfo? = null

  private val _updateStatus = MutableStateFlow<PlayUpdateStatus>(PlayUpdateStatus.Idle)
  val updateStatus: StateFlow<PlayUpdateStatus> = _updateStatus.asStateFlow()

  // For testing / simulation purposes
  private val _isSimulatedForceUpdate = MutableStateFlow(false)
  val isSimulatedForceUpdate: StateFlow<Boolean> = _isSimulatedForceUpdate.asStateFlow()

  fun init(context: Context) {
    if (appUpdateManager == null) {
      appUpdateManager = AppUpdateManagerFactory.create(context.applicationContext)
    }
  }

  fun setSimulatedForceUpdate(enabled: Boolean) {
    _isSimulatedForceUpdate.value = enabled
    if (enabled) {
      _updateStatus.value = PlayUpdateStatus.ForceUpdateRequired(
        PlayAppUpdateDetails(
          isForceUpdate = true,
          currentVersionCode = BuildConfig.VERSION_CODE,
          currentVersionName = BuildConfig.VERSION_NAME,
          latestVersionCode = BuildConfig.VERSION_CODE + 1,
          latestVersionName = "8.0.0",
          releaseNotes = "⚡ Major Google Play Console Update: New high-speed VLESS/Trojan tunnel engine, ultra-low latency ping resolver, and zero-leak DNS protection.",
          isPlayNativeAllowed = false
        )
      )
    } else {
      _updateStatus.value = PlayUpdateStatus.UpToDate
    }
  }

  /**
   * Automatically checks Google Play Console for updates.
   * If the installed version is less than what is available on Google Play Console,
   * triggers an immediate forced update flow.
   */
  fun checkForUpdates(
    context: Context,
    activity: Activity? = null,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    onResult: ((PlayUpdateStatus) -> Unit)? = null
  ) {
    init(context)
    if (_isSimulatedForceUpdate.value) {
      val status = PlayUpdateStatus.ForceUpdateRequired(
        PlayAppUpdateDetails(
          isForceUpdate = true,
          currentVersionCode = BuildConfig.VERSION_CODE,
          currentVersionName = BuildConfig.VERSION_NAME,
          latestVersionCode = BuildConfig.VERSION_CODE + 1,
          latestVersionName = "8.0.0",
          releaseNotes = "⚡ Major Google Play Console Update: High-speed VLESS/Trojan tunnel engine & zero-leak DNS protection.",
          isPlayNativeAllowed = false
        )
      )
      _updateStatus.value = status
      onResult?.invoke(status)
      return
    }

    _updateStatus.value = PlayUpdateStatus.Checking

    val manager = appUpdateManager ?: run {
      _updateStatus.value = PlayUpdateStatus.Error("Play Update Manager not initialized")
      return
    }

    val appUpdateInfoTask = manager.appUpdateInfo

    appUpdateInfoTask.addOnSuccessListener { info ->
      cachedAppUpdateInfo = info
      val currentCode = BuildConfig.VERSION_CODE
      val availableCode = info.availableVersionCode()
      val isImmediateAllowed = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
      val updateAvailability = info.updateAvailability()

      Log.d(
        TAG,
        "Play update check: currentCode=$currentCode, availableCode=$availableCode, " +
          "availability=$updateAvailability, isImmediateAllowed=$isImmediateAllowed"
      )

      if (updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
        // Play Console reports an update is available!
        val details = PlayAppUpdateDetails(
          isForceUpdate = true, // Force to update if app version is less than play console
          currentVersionCode = currentCode,
          currentVersionName = BuildConfig.VERSION_NAME,
          latestVersionCode = availableCode,
          latestVersionName = "${availableCode / 1000}.${(availableCode % 1000) / 100}.${availableCode % 100}",
          releaseNotes = "A new mandatory version is available on Google Play Console. Update is required to ensure secure VPN tunneling.",
          isPlayNativeAllowed = isImmediateAllowed
        )

        _updateStatus.value = PlayUpdateStatus.ForceUpdateRequired(details)
        onResult?.invoke(_updateStatus.value)

        // If activity is provided and immediate update is allowed, launch native Google Play UI immediately
        if (activity != null && isImmediateAllowed) {
          launchPlayImmediateUpdate(activity, info)
        }
      } else if (updateAvailability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
        // An in-app update is already running, resume it
        val details = PlayAppUpdateDetails(
          isForceUpdate = true,
          currentVersionCode = currentCode,
          latestVersionCode = availableCode,
          isPlayNativeAllowed = true
        )
        _updateStatus.value = PlayUpdateStatus.ForceUpdateRequired(details)
        if (activity != null) {
          launchPlayImmediateUpdate(activity, info)
        }
      } else {
        // Native Play check did not return UPDATE_AVAILABLE (could be running sideloaded / debug build)
        // Check secondary remote configuration (Firestore app_config) for Play Console version
        checkRemotePlayConsoleVersion(currentCode, activity, scope, onResult)
      }
    }.addOnFailureListener { exception ->
      Log.w(TAG, "Native Play in-app update check failed (likely sideloaded/debug build): ${exception.message}")
      // Fallback to remote version check
      checkRemotePlayConsoleVersion(BuildConfig.VERSION_CODE, activity, scope, onResult)
    }
  }

  /**
   * Fallback query against remote Play Console version metadata in Firestore
   * to ensure forced updates work across all installation types and testing environments.
   */
  private fun checkRemotePlayConsoleVersion(
    currentCode: Int,
    activity: Activity?,
    scope: CoroutineScope,
    onResult: ((PlayUpdateStatus) -> Unit)?
  ) {
    scope.launch {
      try {
        val firestore = FirebaseFirestore.getInstance()
        val doc = withContext(Dispatchers.IO) {
          try {
            val task = firestore.collection("app_config").document("play_console_version").get()
            var waited = 0
            while (!task.isComplete && waited < 40) {
              kotlinx.coroutines.delay(50)
              waited++
            }
            if (task.isSuccessful) task.result else null
          } catch (t: Throwable) {
            null
          }
        }

        if (doc != null && doc.exists()) {
          val latestCode = doc.getLong("latest_version_code")?.toInt() ?: currentCode
          val minRequiredCode = doc.getLong("min_required_version_code")?.toInt() ?: currentCode
          val latestName = doc.getString("latest_version_name") ?: BuildConfig.VERSION_NAME
          val isForced = doc.getBoolean("force_update") ?: (currentCode < minRequiredCode)
          val notes = doc.getString("release_notes")
            ?: "Critical security and stability updates are now available on Google Play Console."

          if (currentCode < latestCode && (isForced || currentCode < minRequiredCode)) {
            val details = PlayAppUpdateDetails(
              isForceUpdate = true,
              currentVersionCode = currentCode,
              currentVersionName = BuildConfig.VERSION_NAME,
              latestVersionCode = latestCode,
              latestVersionName = latestName,
              releaseNotes = notes,
              isPlayNativeAllowed = false
            )
            _updateStatus.value = PlayUpdateStatus.ForceUpdateRequired(details)
            onResult?.invoke(_updateStatus.value)
            return@launch
          }
        }

        // Everything up to date!
        _updateStatus.value = PlayUpdateStatus.UpToDate
        onResult?.invoke(PlayUpdateStatus.UpToDate)
      } catch (e: Exception) {
        Log.e(TAG, "Error checking remote version: ${e.message}")
        _updateStatus.value = PlayUpdateStatus.UpToDate
        onResult?.invoke(PlayUpdateStatus.UpToDate)
      }
    }
  }

  /**
   * Starts the Google Play In-App Immediate Update flow.
   */
  fun launchPlayImmediateUpdate(activity: Activity, info: AppUpdateInfo? = cachedAppUpdateInfo) {
    val manager = appUpdateManager ?: return
    val updateInfo = info ?: return

    try {
      val options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
      manager.startUpdateFlowForResult(
        updateInfo,
        activity,
        options,
        REQUEST_CODE_IMMEDIATE_UPDATE
      )
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start Play immediate update flow: ${e.message}")
      openPlayStore(activity)
    }
  }

  /**
   * Resumes immediate update if it was in progress when user switched back to app.
   */
  fun resumeUpdateIfInProgress(activity: Activity) {
    val manager = appUpdateManager ?: return
    manager.appUpdateInfo.addOnSuccessListener { info ->
      if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
        launchPlayImmediateUpdate(activity, info)
      }
    }
  }

  /**
   * Opens the Google Play Store app directly to the app's listing page.
   * If the Play Store app is not installed, falls back to the web browser.
   */
  fun openPlayStore(context: Context) {
    val packageName = context.packageName
    try {
      val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
      }
      context.startActivity(marketIntent)
    } catch (e: ActivityNotFoundException) {
      val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
      }
      context.startActivity(webIntent)
    } catch (e: Exception) {
      Log.e(TAG, "Could not open Play Store: ${e.message}")
    }
  }
}
