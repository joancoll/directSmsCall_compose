package cat.dam.andy.directsmscall_compose.permissions

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class PermissionManager(private val activityContext: ComponentActivity) {

    private val permissionsRequired = mutableListOf<PermissionData>()
    private var singlePermissionResultLauncher: ActivityResultLauncher<String>? = null
    private var multiplePermissionResultLauncher: ActivityResultLauncher<Array<String>>? = null

    init {
        initPermissionLaunchers()
    }

    private fun initPermissionLaunchers() {
        singlePermissionResultLauncher =
            activityContext.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                handlePermissionResult(isGranted)
            }

        multiplePermissionResultLauncher =
            activityContext.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                handleMultiplePermissionsResult(permissions)
            }
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            // Find the permission data for the granted permission
            val permissionData = permissionsRequired.firstOrNull {
                it.permission == singlePermissionResultLauncher?.contract?.createIntent(
                    activityContext,
                    it.permission
                )?.extras?.getString("android.intent.extra.PERMISSION_NAME")
            }
            permissionData?.let {
                showToast(it.permissionGrantedMessage)
            }
        } else {
            // Find the permission data for the denied permission
            val permissionData = permissionsRequired.firstOrNull {
                it.permission == singlePermissionResultLauncher?.contract?.createIntent(
                    activityContext,
                    it.permission
                )?.extras?.getString("android.intent.extra.PERMISSION_NAME")
            }
            permissionData?.let {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        activityContext,
                        it.permission
                    )
                ) {
                    // Permanent denial: redirect to settings
                    showAlert(
                        it.permissionPermanentDeniedMessage,
                        { _, _ -> openAppSettings() },
                        { dialogInterface, _ -> dialogInterface.dismiss() }
                    )
                } else {
                    // Temporary denial: show rationale and ask again
                    showAlert(
                        it.permissionNeededMessage,
                        { _, _ -> singlePermissionResultLauncher?.launch(it.permission) },
                        { dialogInterface, _ -> dialogInterface.dismiss() }
                    )
                }
            }
        }
    }

    private fun handleMultiplePermissionsResult(permissions: Map<String, Boolean>) {
        permissions.forEach { (permission, isGranted) ->
            val permissionData = permissionsRequired.firstOrNull { it.permission == permission }
            permissionData?.let {
                if (isGranted) {
                    showToast(it.permissionGrantedMessage)
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            activityContext,
                            permission
                        )
                    ) {
                        // Permanent denial: redirect to settings
                        showAlert(
                            it.permissionPermanentDeniedMessage,
                            { _, _ -> openAppSettings() },
                            { dialogInterface, _ -> dialogInterface.dismiss() }
                        )
                    } else {
                        // Temporary denial: show rationale and ask again
                        showAlert(
                            it.permissionNeededMessage,
                            { _, _ -> multiplePermissionResultLauncher?.launch(arrayOf(it.permission)) },
                            { dialogInterface, _ -> dialogInterface.dismiss() }
                        )
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(activityContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAlert(
        message: String,
        positiveAction: (DialogInterface, Int) -> Unit,
        negativeAction: (DialogInterface, Int) -> Unit
    ) {
        AlertDialog.Builder(activityContext)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton("Ok", positiveAction)
            .setNegativeButton("Cancel", negativeAction)
            .create()
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activityContext.packageName, null)
        intent.data = uri
        activityContext.startActivity(intent)
    }

    fun addPermission(
        permission: String,
        permissionNeededMessage: String,
        permissionGrantedMessage: String,
        permissionPermanentDeniedMessage: String
    ) {
        permissionsRequired.add(
            PermissionData(
                permission,
                permissionNeededMessage,
                permissionGrantedMessage,
                permissionPermanentDeniedMessage
            )
        )
    }

    fun askForPermissionWithDialog(permission: PermissionData) {
        val isGranted = ActivityCompat.checkSelfPermission(
            activityContext,
            permission.permission
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            // If permission is already granted
            showToast(permission.permissionGrantedMessage)
        } else {
            // If permission is not granted, show the dialog with explanation
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activityContext,
                    permission.permission
                )
            ) {
                showAlert(
                    permission.permissionNeededMessage,
                    { _, _ -> singlePermissionResultLauncher?.launch(permission.permission) },
                    { dialogInterface, _ -> dialogInterface.dismiss() }
                )
            } else {
                singlePermissionResultLauncher?.launch(permission.permission)
            }
        }
    }

    fun askForAllNeededPermissions() {
        val permissions = permissionsRequired.map { it.permission }.toTypedArray()
        multiplePermissionResultLauncher?.launch(permissions)
    }

    fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            activityContext,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

data class PermissionData(
    val permission: String,
    val permissionNeededMessage: String,
    val permissionGrantedMessage: String,
    val permissionPermanentDeniedMessage: String
)