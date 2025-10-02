package com.example.fitbalance.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.fitbalance.R

@Composable
fun ValidationErrorText(
    error: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = error,
        style = MaterialTheme.typography.bodySmall,
        color = colorResource(R.color.error_color),
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 2.dp)
    )
}