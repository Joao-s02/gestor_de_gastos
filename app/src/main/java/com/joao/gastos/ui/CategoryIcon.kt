package com.joao.gastos.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.joao.gastos.model.Category

fun Category.icon(): ImageVector {
    return when (this) {
        Category.Comida -> Icons.Rounded.Restaurant
        Category.Transporte -> Icons.Rounded.DirectionsBus
        Category.Hogar -> Icons.Rounded.Home
        Category.Ocio -> Icons.Rounded.SportsEsports
        Category.Salud -> Icons.Rounded.Favorite
        Category.Compras -> Icons.Rounded.ShoppingBag
        Category.Servicios -> Icons.AutoMirrored.Rounded.ReceiptLong
        Category.Salario -> Icons.Rounded.Payments
        Category.Freelance -> Icons.Rounded.Work
        Category.Ventas -> Icons.Rounded.Storefront
        Category.Regalo -> Icons.Rounded.CardGiftcard
        Category.Reembolso -> Icons.Rounded.CurrencyExchange
        Category.Otros -> Icons.Rounded.MoreHoriz
    }
}
