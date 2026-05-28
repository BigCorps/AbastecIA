package com.abastecia.frentista.presentation.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val ColorScheme = lightColorScheme(
    primary   = AzulPosto,
    secondary = VerdeLiberar,
    error     = VermelhoErro,
    background = CinzaFundo,
    surface    = CinzaFundo
)

@Composable
fun AbasteciaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography  = Typography(),
        content     = content
    )
}
