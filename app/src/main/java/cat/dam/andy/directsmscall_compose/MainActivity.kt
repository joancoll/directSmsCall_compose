package cat.dam.andy.directsmscall_compose

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cat.dam.andy.directsmscall_compose.permissions.PermissionManager
import cat.dam.andy.directsmscall_compose.ui.SmsCallScreen

class MainActivity : ComponentActivity() {
    private val permissionManager by lazy { PermissionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPermissions()
        enableEdgeToEdge()
        val context = this
        setContent { SmsCallScreen(context, permissionManager) }
    }

    private fun initPermissions() {
        permissionManager.addPermission(
            Manifest.permission.SEND_SMS,
            getString(R.string.smsPermissionInfo),
            getString(R.string.smsPermissionGranted),
            getString(R.string.smsPermissionDenied),
            getString(R.string.smsPermissionPermanentDenied)
        )

        permissionManager.addPermission(
            Manifest.permission.CALL_PHONE,
            getString(R.string.callPermissionInfo),
            getString(R.string.callPermissionGranted),
            getString(R.string.callPermissionDenied),
            getString(R.string.callPermissionPermanentDenied)
        )
    }
}