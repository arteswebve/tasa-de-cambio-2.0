package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyType
import com.example.ui.CalculatorUiState
import com.example.ui.components.ArtesWebVeFooter
import com.example.ui.theme.BrandGoldDark
import com.example.ui.theme.BrandYellow
import com.example.ui.theme.CurrencyVesBlue
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    onInputChanged: (String) -> Unit,
    onQuickAmountClick: (Double) -> Unit,
    onToggleDirection: () -> Unit,
    onInfoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val sourceCurrency = if (!state.isInverseDirection) state.selectedCurrency else CurrencyType.VES
    val targetCurrency = if (!state.isInverseDirection) CurrencyType.VES else state.selectedCurrency

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Direction Header with swap button
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Source badge
                CurrencyBadge(currency = sourceCurrency)

                // Direction invert button
                Surface(
                    shape = CircleShape,
                    color = BrandYellow,
                    modifier = Modifier.size(42.dp)
                ) {
                    IconButton(
                        onClick = onToggleDirection,
                        modifier = Modifier.testTag("swap_direction_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Invertir dirección de conversión",
                            tint = DarkSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Target badge
                CurrencyBadge(currency = targetCurrency)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Amount Input Field (Real-time calculation as user types!)
        Text(
            text = "Monto a calcular (${sourceCurrency.code})",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = state.inputAmountText,
            onValueChange = { newValue ->
                // Allow digits, dots and commas
                val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                onInputChanged(filtered)
            },
            placeholder = { Text("0,00", color = TextMuted) },
            leadingIcon = {
                Text(
                    text = sourceCurrency.symbol,
                    color = BrandYellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceElevated,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = BrandYellow,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("amount_input_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick amount chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickAmounts = if (!state.isInverseDirection) {
                listOf(5.0, 10.0, 20.0, 50.0, 100.0)
            } else {
                listOf(200.0, 500.0, 1000.0, 2500.0, 5000.0)
            }

            quickAmounts.forEach { amount ->
                val label = if (!state.isInverseDirection) {
                    "${sourceCurrency.symbol}${amount.toInt()}"
                } else {
                    "Bs. ${amount.toInt()}"
                }

                FilterChip(
                    selected = false,
                    onClick = { onQuickAmountClick(amount) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurfaceElevated,
                        labelColor = BrandYellow
                    ),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("quick_chip_${amount.toInt()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed Result Breakdown Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.5.dp, BrandYellow),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL A RECIBIR",
                        color = BrandYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        color = DarkSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            text = "Tasa: Bs. ${CurrencyFormatUtils.formatAmount(state.currentRate)}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Formatted Result
                Text(
                    text = "${targetCurrency.symbol} ${CurrencyFormatUtils.formatAmount(state.calculatedResult)}",
                    color = TextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Breakdown description
                Text(
                    text = "Conversión de ${CurrencyFormatUtils.formatAmount(state.parsedInputAmount)} ${sourceCurrency.code} a ${targetCurrency.code}",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Copy & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val textToCopy = "${sourceCurrency.symbol} ${CurrencyFormatUtils.formatAmount(state.parsedInputAmount)} = ${targetCurrency.symbol} ${CurrencyFormatUtils.formatAmount(state.calculatedResult)} (Tasa: Bs. ${CurrencyFormatUtils.formatAmount(state.currentRate)} - TasaCambio VE)"
                            copyToClipboard(context, textToCopy)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceElevated,
                            contentColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, DarkBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_result_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val shareText = "💱 *Cálculo de TasaCambio VE*\n\n" +
                                    "• Monto: ${sourceCurrency.symbol} ${CurrencyFormatUtils.formatAmount(state.parsedInputAmount)} ${sourceCurrency.code}\n" +
                                    "• Tasa aplicada: Bs. ${CurrencyFormatUtils.formatAmount(state.currentRate)}\n" +
                                    "• Total a recibir: *${targetCurrency.symbol} ${CurrencyFormatUtils.formatAmount(state.calculatedResult)} ${targetCurrency.code}*\n\n" +
                                    "Desarrollada por ArtesWebVE | https://arteswebve.com/"
                            shareCalculation(context, shareText)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandYellow,
                            contentColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_result_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Footer Section
        ArtesWebVeFooter(onInfoClick = onInfoClick)

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun CurrencyBadge(
    currency: CurrencyType,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(text = currency.flagEmoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = currency.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = currency.code, color = currency.accentColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("TasaCambio VE", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "¡Cálculo copiado al portapapeles!", Toast.LENGTH_SHORT).show()
}

private fun shareCalculation(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir cálculo TasaCambio VE")
    context.startActivity(shareIntent)
}
