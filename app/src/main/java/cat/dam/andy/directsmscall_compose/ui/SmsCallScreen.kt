package cat.dam.andy.directsmscall_compose.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cat.dam.andy.directsmscall_compose.R
import cat.dam.andy.directsmscall_compose.permissions.PermissionManager
import cat.dam.andy.directsmscall_compose.utils.initiateCall
import cat.dam.andy.directsmscall_compose.utils.sendSms

@Composable
fun SmsCallScreen(context: Context, permissionManager: PermissionManager) {
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    val isLandscape = context.resources.configuration.orientation == 2
    Column(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxWidth()) {
                PhoneNumberField(
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { phoneNumber = it },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                )
                MessageField(
                    message = message,
                    onMessageChange = { message = it },
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
            }
        } else {
            PhoneNumberField(
                phoneNumber = phoneNumber,
                onPhoneNumberChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            MessageField(
                message = message,
                onMessageChange = { message = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }

        NumPad { number ->
            phoneNumber = when (number) {
                "DEL" -> phoneNumber.dropLast(1)
                else -> phoneNumber + number
            }
        }

        ActionButtons(
            phoneNumber = phoneNumber,
            message = message,
            permissionManager = permissionManager,
            context = context,
            modifier = Modifier.fillMaxWidth()
        )
        // Mostrar el diàleg de permisos quan sigui necessari
        permissionManager.ShowPermissionDialog(
            title = context.getString(R.string.permissionRequired),
            confirm = context.getString(R.string.ok),
            cancel = context.getString(R.string.cancel)
        )
    }
}

@Composable
fun PhoneNumberField(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = phoneNumber,
        onValueChange = onPhoneNumberChange,
        label = { Text("Phone Number") },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun MessageField(
    message: String,
    onMessageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = message,
        onValueChange = onMessageChange,
        label = { Text("Message") },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun ActionButtons(
    phoneNumber: String,
    message: String,
    permissionManager: PermissionManager,
    context: Context,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(
            modifier = Modifier.weight(1f).padding(8.dp),
            onClick = {
                if (permissionManager.hasPermission(android.Manifest.permission.SEND_SMS)) {
                    sendSms(context, phoneNumber, message)
                } else {
                    permissionManager.askForPermission(
                        context,
                        android.Manifest.permission.SEND_SMS
                    )
                }
            }
        ) {
            Text("Send SMS")
        }

        Button(
            modifier = Modifier.weight(1f).padding(8.dp),
            onClick = {
                if (permissionManager.hasPermission(android.Manifest.permission.CALL_PHONE)) {
                    initiateCall(context, phoneNumber)
                } else {
                    permissionManager.askForPermission(
                        context,
                        android.Manifest.permission.CALL_PHONE
                    )
                }
            }
        ) {
            Text("Call")
        }
    }
}