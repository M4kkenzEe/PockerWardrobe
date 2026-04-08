package com.ownstd.project.core.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ownstd.project.core.compose.theme.Theme
import com.ownstd.project.core.resources.MR
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MarketplaceLinkRow(
    url: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = remember(url) { marketplaceLabel(url) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Theme.colors.background.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(MR.images.external_link),
            contentDescription = null,
            tint = Theme.colors.label.secondary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = Theme.typography.body,
            color = Theme.colors.label.primary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(MR.images.chevron_right),
            contentDescription = null,
            tint = Theme.colors.label.muted,
            modifier = Modifier.size(16.dp),
        )
    }
}

private fun marketplaceLabel(url: String): String {
    val host = runCatching {
        val withScheme = if (url.startsWith("http")) url else "https://$url"
        val start = withScheme.indexOf("://") + 3
        val end = withScheme.indexOf("/", start).takeIf { it >= 0 } ?: withScheme.length
        withScheme.substring(start, end).removePrefix("www.")
    }.getOrDefault(url)

    return when {
        host.contains("wildberries") -> "Wildberries"
        host.contains("ozon") -> "Ozon"
        else -> host
    }
}

@Preview
@Composable
private fun MarketplaceLinkRowPreview() {
    MarketplaceLinkRow(
        url = "https://wildberries.ru/catalog/123",
        onClick = {},
    )
}
