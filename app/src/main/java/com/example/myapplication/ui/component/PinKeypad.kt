package com.example.myapplication.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun PinDotIndicator(count: Int, maxDots: Int = 4, hasError: Boolean = false) {
    val offsetX by animateDpAsState(
        targetValue = if (hasError) 10.dp else 0.dp,
        animationSpec = spring(),
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.offset(x = offsetX)
    ) {
        repeat(maxDots) { index ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < count) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keys = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
        listOf(-1, 0, -2),
    )
    Column(modifier = modifier.fillMaxWidth()) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach { key ->
                    when {
                        key == -1 -> Spacer(modifier = Modifier.size(72.dp))
                        key == -2 -> TextButton(
                            onClick = onDelete,
                            modifier = Modifier.size(72.dp),
                        ) { Text("⌫", style = MaterialTheme.typography.headlineSmall) }
                        else -> TextButton(
                            onClick = { onDigit(key) },
                            modifier = Modifier.size(72.dp),
                        ) { Text("$key", style = MaterialTheme.typography.headlineSmall) }
                    }
                }
            }
        }
    }
}
