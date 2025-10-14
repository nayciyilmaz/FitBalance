package com.example.fitbalance.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitbalance.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.res.stringResource

@Composable
fun DetailsScreenEditableMealCard(
    title: String,
    items: List<Pair<String, String>>,
    totalCalories: Int,
    onItemNameChange: (Int, String) -> Unit,
    onItemCaloriesChange: (Int, String) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onAddItem: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.green)
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = modifier.padding(bottom = 8.dp)
            )

            Text(
                text = stringResource(R.string.add_kalori),
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.error_color),
                modifier = modifier.padding(bottom = 12.dp)
            )

            items.forEachIndexed { index, (name, calories) ->
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomOutlinedTextField(
                        value = name,
                        onValueChange = { onItemNameChange(index, it) },
                        placeholder = stringResource(R.string.yiyecek_adi),
                        modifier = modifier.weight(1f),
                        keyboardType = KeyboardType.Text
                    )

                    CustomOutlinedTextField(
                        value = calories,
                        onValueChange = { onItemCaloriesChange(index, it) },
                        placeholder = stringResource(R.string.kalori),
                        modifier = modifier.weight(0.5f),
                        keyboardType = KeyboardType.Number
                    )

                    IconButton(
                        onClick = { onRemoveItem(index) },
                        modifier = modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = modifier.padding(vertical = 12.dp),
                color = Color.White,
                thickness = 2.dp
            )

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.total_kalori),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = "$totalCalories kcal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomButton(
                    onClick = onAddItem,
                    text = stringResource(R.string.ekle),
                    modifier = modifier.weight(1f)
                )

                CustomButton(
                    onClick = onConfirm,
                    text = stringResource(R.string.onayla),
                    modifier = modifier.weight(1f)
                )

                CustomButton(
                    onClick = onCancel,
                    text = stringResource(R.string.iptal),
                    modifier = modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.8f)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.8f),
            cursorColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
fun CustomButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = colorResource(R.color.green)
        )
    ) {
        Text(text = text)
    }
}