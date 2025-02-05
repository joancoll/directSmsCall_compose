package cat.dam.andy.directsmscall_compose.permissions

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.activity.ComponentActivity

class PermissionManager(private val activityContext: ComponentActivity) {

    private val permissionsRequired = mutableListOf<PermissionData>()
    private var permissionRequired: String = ""
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
        val permissionData = permissionsRequired.firstOrNull { it.permission == permissionRequired }
        if (isGranted) {
            permissionData?.let {
                showToast(it.permissionGrantedMessage)
            }
        } else {
            permissionData?.let {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activityContext, permissionRequired)) {
                    // Mostrar una explicació raonable abans de demanar el permís
                    showAlert(
                        it.permissionNeededMessage,
                        { _, _ -> askForPermission(it) },
                        { dialogInterface, _ -> dialogInterface.dismiss() }
                    )
                } else {
                    // Si el permís ha estat denegat permanentment, redirigir a la configuració
                    showAlert(
                        it.permissionPermanentDeniedMessage,
                        { _, _ -> openAppSettings() },
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
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activityContext, permission)) {
                        showAlert(
                            it.permissionNeededMessage,
                            { _, _ -> askForPermission(it) },
                            { dialogInterface, _ -> dialogInterface.dismiss() }
                        )
                    } else {
                        showAlert(
                            it.permissionPermanentDeniedMessage,
                            { _, _ -> openAppSettings() },
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

    private fun showAlert(message: String, positiveAction: (DialogInterface, Int) -> Unit, negativeAction: (DialogInterface, Int) -> Unit) {
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

    fun addPermission(permission: String, permissionNeededMessage: String, permissionGrantedMessage: String, permissionPermanentDeniedMessage: String) {
        permissionsRequired.add(PermissionData(permission, permissionNeededMessage, permissionGrantedMessage, permissionPermanentDeniedMessage))
    }

    private fun askForPermission(permission: PermissionData) {
        permissionRequired = permission.permission

        // Comprovem si el permís ja ha estat sol·licitat
        if (ActivityCompat.checkSelfPermission(activityContext, permissionRequired) == PackageManager.PERMISSION_GRANTED) {
            // Si el permís ja ha estat concedit, només es mostra el missatge
            showToast(permission.permissionGrantedMessage)
        } else {
            // Si el permís no ha estat concedit, demanem-lo
            singlePermissionResultLauncher?.launch(permissionRequired)
        }
    }

    fun askForPermissionWithDialog(permission: PermissionData) {
        val isGranted = ActivityCompat.checkSelfPermission(activityContext, permission.permission) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            // Si el permís ja està concedit
            showToast(permission.permissionGrantedMessage)
        } else {
            // Si el permís no està concedit, mostrar el diàleg amb l'explicació
            showAlert(
                permission.permissionNeededMessage,
                { _, _ -> askForPermission(permission) },
                { dialogInterface, _ -> dialogInterface.dismiss() }
            )
        }
    }

    fun askForAllNeededPermissions() {
        val permissions = permissionsRequired.map { it.permission }.toTypedArray()
        multiplePermissionResultLauncher?.launch(permissions)
    }

    fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(activityContext, permission) == PackageManager.PERMISSION_GRANTED
    }
}

data class PermissionData(
    val permission: String,
    val permissionNeededMessage: String,
    val permissionGrantedMessage: String,
    val permissionPermanentDeniedMessage: String
)
