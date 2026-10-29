package com.example.temacker.core.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.White

// Off reads as a clear grey track with a white thumb, not Material's near-invisible lavender.
@Composable
fun TmkSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = White,
            checkedTrackColor = Amber,
            checkedBorderColor = Amber,
            uncheckedThumbColor = White,
            uncheckedTrackColor = Ink500.copy(alpha = 0.35f),
            uncheckedBorderColor = Ink500.copy(alpha = 0.35f),
            disabledCheckedThumbColor = White,
            disabledCheckedTrackColor = Amber.copy(alpha = 0.45f),
            disabledCheckedBorderColor = Amber.copy(alpha = 0.45f),
            disabledUncheckedThumbColor = White,
            disabledUncheckedTrackColor = Ink500.copy(alpha = 0.18f),
            disabledUncheckedBorderColor = Ink500.copy(alpha = 0.18f)
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun TmkSwitchPreview() {
    TemackerTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TmkSwitch(checked = false, onCheckedChange = {})
            TmkSwitch(checked = true, onCheckedChange = {})
            TmkSwitch(checked = false, onCheckedChange = {}, enabled = false)
            TmkSwitch(checked = true, onCheckedChange = {}, enabled = false)
        }
    }
}
