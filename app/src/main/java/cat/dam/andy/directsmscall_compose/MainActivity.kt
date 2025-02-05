package cat.dam.andy.directsmscall_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cat.dam.andy.directsmscall_compose.permissions.PermissionManager
import cat.dam.andy.directsmscall_compose.ui.SmsCallScreen

class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)
        setContent { SmsCallScreen(permissionManager) }
    }
}