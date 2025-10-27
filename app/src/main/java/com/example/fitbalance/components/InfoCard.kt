package com.example.fitbalance.components

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.example.fitbalance.R

@Composable
fun InfoCard(
    messages: Array<String>,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.blue)
        )
    ) {
        EditHorizontalPager(
            messages = messages,
            pagerState = pagerState
        )
    }
}