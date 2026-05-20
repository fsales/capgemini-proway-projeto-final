package com.app.gerenciadorcartoes.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.ui.theme.GerenciadorCartoesTheme

// =============================================================================
// Template definitions
// =============================================================================

data class CartaoTemplateConfig(
    val id            : String,
    val nomeExibicao  : String,
    val gradientStart : Color,
    val gradientEnd   : Color,
    val accentColor   : Color,
    val bankLabel     : String,
)

val todosTemplates: List<CartaoTemplateConfig> = listOf(
    CartaoTemplateConfig(
        id            = "default",
        nomeExibicao  = "Padrão",
        gradientStart = Color(0xFF3A1580),
        gradientEnd   = Color(0xFF6A35BF),
        accentColor   = Color(0xFFBB86FC),
        bankLabel     = "Cartão",
    )
)

fun templateConfigById(id: String): CartaoTemplateConfig =
    todosTemplates.find { it.id == id } ?: todosTemplates.first()

// =============================================================================
// CartaoTemplateCard — full-size card visual with data overlaid
// =============================================================================

@Composable
fun CartaoTemplateCard(
    cartao   : Cartao,
    modifier : Modifier = Modifier,
    onClick  : (() -> Unit)? = null,
) {
    val template  = templateConfigById(cartao.template)
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .shadow(elevation = 8.dp, shape = cardShape)
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(template.gradientStart, template.gradientEnd),
                    start  = Offset(0f, 0f),
                    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(20.dp),
    ) {
        // Decorative glowing circles in the background
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color  = template.accentColor.copy(alpha = 0.15f),
                radius = size.width * 0.65f,
                center = Offset(size.width * 1.05f, -size.height * 0.15f),
            )
            drawCircle(
                color  = template.accentColor.copy(alpha = 0.10f),
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.85f, size.height * 0.85f),
            )
        }

        // Bank label — top left
        Text(
            text       = template.bankLabel,
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize   = 18.sp,
            modifier   = Modifier.align(Alignment.TopStart),
        )

        // Contactless arcs — top right
        Canvas(
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.TopEnd),
        ) {
            val cx      = size.width / 2f
            val cy      = size.height
            val strokeW = size.width * 0.10f
            for (i in 1..3) {
                val r = size.width * 0.20f * i
                drawArc(
                    color      = Color.White.copy(alpha = 0.75f),
                    startAngle = 225f,
                    sweepAngle = 90f,
                    useCenter  = false,
                    topLeft    = Offset(cx - r, cy - r),
                    size       = Size(r * 2f, r * 2f),
                    style      = Stroke(width = strokeW),
                )
            }
        }

        // EMV chip — center left
        Canvas(
            modifier = Modifier
                .size(40.dp, 32.dp)
                .align(Alignment.CenterStart),
        ) {
            val cr = CornerRadius(5.dp.toPx())
            drawRoundRect(color = Color(0xFFD4AF37), cornerRadius = cr)
            val lc = Color(0xFFB8962E)
            val sw = 1.5.dp.toPx()
            drawLine(lc, Offset(size.width * 0.33f, 0f), Offset(size.width * 0.33f, size.height), strokeWidth = sw)
            drawLine(lc, Offset(size.width * 0.66f, 0f), Offset(size.width * 0.66f, size.height), strokeWidth = sw)
            drawLine(lc, Offset(0f, size.height * 0.40f), Offset(size.width, size.height * 0.40f), strokeWidth = sw)
            drawLine(lc, Offset(0f, size.height * 0.60f), Offset(size.width, size.height * 0.60f), strokeWidth = sw)
        }

        // Bottom section: number + owner + expiry + network
        Column(
            modifier            = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Card number
            Text(
                text          = "•••• •••• •••• ${cartao.finalNumero.ifBlank { "????" }}",
                color         = Color.White,
                fontSize      = 17.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 2.sp,
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom,
            ) {
                // Titular
                Column {
                    Text(
                        text          = "TITULAR",
                        color         = Color.White.copy(alpha = 0.60f),
                        fontSize      = 9.sp,
                        letterSpacing = 0.5.sp,
                    )
                    Text(
                        text       = cartao.nomeTitular.uppercase().ifBlank { "—" },
                        color      = Color.White,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                    )
                }

                // Validade
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text          = "VALIDADE",
                        color         = Color.White.copy(alpha = 0.60f),
                        fontSize      = 9.sp,
                        letterSpacing = 0.5.sp,
                    )
                    Text(
                        text       = cartao.validade.ifBlank { "--/--" },
                        color      = Color.White,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Bandeira (network)
                Text(
                    text       = cartao.bandeira.uppercase(),
                    color      = Color.White,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// =============================================================================
// CartaoTemplateMini — small preview used in the template picker
// =============================================================================

@Composable
fun CartaoTemplateMini(
    config     : CartaoTemplateConfig,
    selected   : Boolean,
    onClick    : () -> Unit,
    modifier   : Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .size(width = 80.dp, height = 50.dp)
            .shadow(if (selected) 6.dp else 2.dp, shape)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(config.gradientStart, config.gradientEnd),
                )
            )
            .then(
                if (selected) Modifier.border(2.dp, config.accentColor, shape)
                else          Modifier
            )
            .clickable { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            text       = config.bankLabel,
            color      = Color.White,
            fontSize   = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
        )
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true, name = "Template – Padrão")
@Composable
private fun TemplatePadraoPreview() {
    GerenciadorCartoesTheme {
        CartaoTemplateCard(
            cartao   = Cartao(nomeTitular = "Carlos Lima", finalNumero = "9999", bandeira = "Elo", validade = "03/29"),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Template Mini – selecionado")
@Composable
private fun TemplateMiniPreview() {
    GerenciadorCartoesTheme {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            todosTemplates.take(3).forEachIndexed { index, config ->
                CartaoTemplateMini(
                    config   = config,
                    selected = index == 1,
                    onClick  = {},
                )
            }
        }
    }
}
