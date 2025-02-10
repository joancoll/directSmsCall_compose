package cat.dam.andy.directsmscall_compose.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow

class PermissionManager(private val activity: ComponentActivity) {



    private val _permissionsState = MutableStateFlow<List<PermissionData>>(emptyList())
    private val permissions = mutableListOf<PermissionData>()
    private val activityResultLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            handlePermissionResult(currentPermission, result)
        }

    private var currentPermission: PermissionData? = null
    private var showDialog by mutableStateOf(false)
    private var dialogMessage by mutableStateOf("")
    private var onPositiveAction: (() -> Unit)? = null
    private var onNegativeAction: (() -> Unit)? = null

    fun addPermission(
        permission: String,
        permissionInfo: String,
        grantedMessage: String,
        deniedMessage: String,
        permanentDeniedMessage: String
    ) {
        permissions.add(
            PermissionData(
                permission,
                permissionInfo,
                grantedMessage,
                deniedMessage,
                permanentDeniedMessage
            )
        )
        _permissionsState.value = permissions.toList()
    }

    fun askForPermission(context: Context, permission: String) {
        val permissionData = permissions.find { it.permission == permission }
        if (permissionData != null) {
            currentPermission = permissionData
            if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, permissionData.grantedMessage, Toast.LENGTH_SHORT).show()
            } else {
                activityResultLauncher.launch(permission)
            }
        }
    }

    fun hasPermission(permission: String): Boolean {
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun handlePermissionResult(permissionData: PermissionData?, isGranted: Boolean) {
        if (permissionData == null) return
        if (isGranted) {
            Toast.makeText(activity, permissionData.grantedMessage, Toast.LENGTH_SHORT).show()
        } else {
            if (activity.shouldShowRequestPermissionRationale(permissionData.permission)) {
                showDialog(permissionData.deniedMessage)
            } else {
                showDialog(
                    permissionData.permanentDeniedMessage,
                    positiveAction = {
                        openAppSettings() // Obrir configuració quan es prem el botó d'acció
                    }
                )
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }

    private fun showDialog(
        message: String,
        positiveAction: (() -> Unit)? = null,
        negativeAction: (() -> Unit)? = {
            // Tancar el diàleg
            showDialog = false
        }
    ) {
        dialogMessage = message
        onPositiveAction = positiveAction
        onNegativeAction = negativeAction
        showDialog = true
    }

    @Composable
    fun ShowPermissionDialog(title: String, confirm: String, cancel: String) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(title) },
                text = { Text(dialogMessage) },
                confirmButton = {
                    Button(onClick = {
                        onPositiveAction?.invoke()
                        showDialog = false
                    }) {
                        Text(confirm)
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        onNegativeAction?.invoke()
                        showDialog = false
                    }) {
                        Text(cancel)
                    }
                }
            )
        }
    }
}