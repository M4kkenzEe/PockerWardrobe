package com.ownstd.project.tiktok_feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class for feed items
data class FeedItem(
    val id: Int,
    val username: String,
    val description: String,
)

// Main composable for the TikTok-like feed screen
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TikTokFeedScreen() {
    // Sample feed data
    val feedItems = listOf(
        FeedItem(1, "@user1", "This is an awesome video! #fyp"),
        FeedItem(2, "@user2", "Check this out! #trending"),
        FeedItem(3, "@user3", "Fun times! #viral"),
        FeedItem(4, "@user3", "Fun times! #viral"),
        FeedItem(5, "@user3", "Fun times! #viral"),
        FeedItem(6, "@user3", "Fun times! #viral"),
        FeedItem(7, "@user3", "Fun times! #viral"),
        FeedItem(8, "@user3", "Fun times! #viral"),
        FeedItem(9, "@user3", "Fun times! #viral"),
    )

    // Pager state to manage current page
    val pagerState = rememberPagerState(pageCount = { feedItems.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Vertical pager for snapping feed items
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { feedItems[it].id } // Ensure stable keys for items
        ) { page ->
            FeedItemCard(feedItems[page])
        }
    }
}

// Composable for each feed item
@Composable
fun FeedItemCard(item: FeedItem) {
    var isPlaying by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .aspectRatio(9f / 16f) // Standard vertical video aspect ratio
    ) {
        // Placeholder for video player
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Video Placeholder\n${item.description}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Play/Pause button
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Text("GGG")
        }

        // Right-side action buttons (like, share)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionButton(
                icon = Icons.Default.Favorite,
                count = "12.3K",
                contentDescription = "Like"
            )
            ActionButton(
                icon = Icons.Default.Share,
                count = "1.2K",
                contentDescription = "Share"
            )
        }

        // Bottom-left user info and description
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .fillMaxWidth(0.7f)
        ) {
            Text(
                text = item.username,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.description,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Reusable composable for action buttons
@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: String,
    contentDescription: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = { /* Handle action */ }) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = count,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}