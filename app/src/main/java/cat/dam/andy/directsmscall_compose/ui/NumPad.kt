package cat.dam.andy.directsmscall_compose.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumPad(onNumberClicked: (String) -> Unit) {
    val numPad = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("DEL", "0", "+")
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        numPad.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { number ->
                    Button(
                        onClick = { onNumberClicked(number) },
                        modifier = Modifier.weight(1f).padding(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (number == "DEL") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(number)
                    }
                }
            }
        }
    }
}