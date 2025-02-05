package cat.dam.andy.directsmscall_compose.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast

fun sendSms(context: Context, phoneNumber: String, message: String) {
    Toast.makeText(context, "Enviant SMS a $phoneNumber", Toast.LENGTH_SHORT).show()
    try {
        val smsManager = context.getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    } catch (e: Exception) {
        Toast.makeText(context, "Error enviant SMS: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun initiateCall(context: Context, phoneNumber: String) {
    Toast.makeText(context, "Iniciant trucada a $phoneNumber", Toast.LENGTH_SHORT).show()
    try {
        val callIntent = Intent(Intent.ACTION_CALL).apply { data = Uri.parse("tel:$phoneNumber") }
        context.startActivity(callIntent)
    } catch (e: SecurityException) {
        Toast.makeText(context, "Permís denegat per trucades", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error en la trucada: ${e.message}", Toast.LENGTH_LONG).show()
    }
}