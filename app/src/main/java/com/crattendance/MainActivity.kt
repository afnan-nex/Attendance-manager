package com.crattendance

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.crattendance.data.model.AppSettingsEntity
import com.crattendance.ui.navigation.AppNavGraph
import com.crattendance.ui.theme.CRAttendanceTheme
import com.crattendance.ui.theme.CrIcons
import com.crattendance.utils.BiometricHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class MainActivity : FragmentActivity() {

    companion object {
        private const val REQ_CREATE_DOC = 1001
        private var onDocCreated: ((Uri?) -> Unit)? = null

        fun launchCreateDoc(
            activity: FragmentActivity,
            fileName: String,
            mimeType: String,
            callback: (Uri?) -> Unit
        ) {
            onDocCreated = callback
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mimeType
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            try {
                activity.startActivityForResult(intent, REQ_CREATE_DOC)
            } catch (e: Exception) {
                callback(null)
                onDocCreated = null
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CREATE_DOC) {
            val uri = if (resultCode == RESULT_OK) data?.data else null
            onDocCreated?.invoke(uri)
            onDocCreated = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CRAttendanceTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val app = LocalContext.current.applicationContext as CRAttendanceApp
    val navController = rememberNavController()
    val activity = LocalContext.current as FragmentActivity

    val settings by app.settingsRepository.settings
        .collectAsStateWithLifecycle(initialValue = AppSettingsEntity())
    var settingsLoaded by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(false) }

    val enabled by rememberUpdatedState(settings.biometricEnabled)

    // Read the real setting once before deciding to lock. The settings flow first
    // emits the default (disabled), so ON_START can fire before the real value is
    // known — that race is why the lock sometimes didn't appear on cold start.
    // Reading it once here also means toggling the setting in-app never re-locks
    // immediately (the user has just authenticated to enable it).
    LaunchedEffect(Unit) {
        if (app.settingsRepository.settings.first().biometricEnabled) locked = true
        settingsLoaded = true
    }

    // Re-lock whenever the app returns to the foreground and biometrics are on.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && enabled) locked = true
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when {
        // Brief blank while the setting loads, so the app content is never
        // flashed before the lock decision is made.
        !settingsLoaded -> {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {}
        }
        locked && settings.biometricEnabled -> {
            LockOverlay(
                activity = activity,
                onUnlocked = { locked = false }
            )
        }
        else -> {
            AppNavGraph(navController = navController)
        }
    }
}

/** Full-screen lock shown on resume while biometric lock is enabled. */
@Composable
private fun LockOverlay(
    activity: FragmentActivity,
    onUnlocked: () -> Unit
) {
    // Only one biometric prompt may be active at a time; calling authenticate()
    // twice crashes with IllegalStateException. Track it so the auto-prompt and
    // the Unlock button can't collide.
    var promptActive by remember { mutableStateOf(false) }
    val finishPrompt = { promptActive = false }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = CrIcons.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Attendance Manager",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Unlock to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = {
                if (!promptActive) {
                    promptActive = true
                    authenticate(activity, onUnlocked, finishPrompt)
                }
            }) {
                Text("Unlock")
            }
        }
    }

    // Auto-prompt once when the overlay appears, after the activity has fully
    // resumed (a prompt shown too early is silently dismissed on some devices).
    LaunchedEffect(Unit) {
        promptActive = true
        delay(250)
        authenticate(activity, onUnlocked, finishPrompt)
    }
}

private fun authenticate(
    activity: FragmentActivity,
    onUnlocked: () -> Unit,
    onFinished: () -> Unit
) {
    try {
        BiometricHelper.showPrompt(
            activity = activity,
            title = "Unlock Attendance Manager",
            subtitle = "Verify your identity to continue",
            onSuccess = { onUnlocked() },
            // Dismissed (swipe/back) or failed — let the user tap Unlock again.
            onError = { _, _ -> onFinished() }
        )
    } catch (e: Exception) {
        // Prompt couldn't be shown (e.g. activity not resumed); retry via button.
        onFinished()
    }
}
