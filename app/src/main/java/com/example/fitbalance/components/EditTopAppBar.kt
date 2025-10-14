package com.example.fitbalance.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitbalance.R

@Composable
fun EditTopAppBar(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 42.dp, start = 42.dp, end = 42.dp),
        shadowElevation = 12.dp,
        color = colorResource(R.color.surface_color)
    ) {
        Text(
            text = title,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}
