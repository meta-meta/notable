package com.olup.notable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun PenToolbarButton(
    pen: Pen,
    icon: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    sizes: List<Pair<String, Float>>,
    penSetting: PenSetting,
    onChangeSetting: (PenSetting) -> Unit,
    onStrokeMenuOpenChange: ((Boolean) -> Unit)? = null
) {
    var isStrokeMenuOpen by remember { mutableStateOf(false) }

    if (onStrokeMenuOpenChange != null) {
        LaunchedEffect(isStrokeMenuOpen) {
            onStrokeMenuOpenChange(isStrokeMenuOpen)
        }
    }


    Box {

        ToolbarButton(
            isSelected = isSelected,
            onSelect = {
                if (isSelected) isStrokeMenuOpen = !isStrokeMenuOpen
                else onSelect()
            },
            penColor = Color(penSetting.color),
            iconId = icon,
            contentDescription = pen.penName
        )

        if (isStrokeMenuOpen) {
            StrokeMenu(
                value = penSetting,
                onChange = { onChangeSetting(it) },
                onClose = { isStrokeMenuOpen = false },
                options = sizes
            )
        }
    }
}