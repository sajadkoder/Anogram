package com.anogram.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anogram.app.domain.model.Message
import com.anogram.app.domain.model.MessageStatus
import com.anogram.app.domain.model.Reaction
import com.anogram.app.presentation.ui.theme.MessageReceived
import com.anogram.app.presentation.ui.theme.MessageSent
import com.anogram.app.presentation.ui.theme.TelegramBlue
import com.anogram.app.presentation.ui.theme.TelegramLightBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: Message,
    onLongClick: () -> Unit = {},
    onReplyClick: () -> Unit = {},
    showReplyPreview: Boolean = true,
    modifier: Modifier = Modifier
) {
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    val bubbleColor = if (message.isOutgoing) MessageSent else MessageReceived
    val alignment = if (message.isOutgoing) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        if (showReplyPreview && message.replyToId != null && message.replyToContent != null) {
            ReplyPreviewBar(
                content = message.replyToContent,
                isOutgoing = message.isOutgoing,
                onClick = onReplyClick,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .clickable(onClick = onLongClick)
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )

                if (message.isEdited) {
                    Text(
                        text = "edited",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    if (message.isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        StatusIcon(status = message.status)
                    }
                }
            }
        }

        if (message.reactions.isNotEmpty()) {
            ReactionsRow(
                reactions = message.reactions,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ReplyPreviewBar(
    content: String,
    isOutgoing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isOutgoing) TelegramBlue else TelegramLightBlue

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(borderColor.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(borderColor)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.Reply,
            contentDescription = "Reply",
            modifier = Modifier.size(14.dp),
            tint = borderColor
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReactionsRow(
    reactions: List<Reaction>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        reactions.forEach { reaction ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (reaction.isSelected) TelegramLightBlue.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .clickable { }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = reaction.emoji, style = MaterialTheme.typography.bodySmall)
                if (reaction.count > 1) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = reaction.count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (reaction.isSelected) TelegramBlue else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusIcon(status: MessageStatus) {
    val tint = when (status) {
        MessageStatus.READ -> TelegramBlue
        MessageStatus.DELIVERED -> Color.Gray
        MessageStatus.SENT -> Color.Gray
        MessageStatus.SENDING -> Color.Gray
        MessageStatus.FAILED -> Color.Red
    }

    Icon(
        imageVector = when (status) {
            MessageStatus.READ -> Icons.Default.DoneAll
            MessageStatus.DELIVERED -> Icons.Default.DoneAll
            MessageStatus.SENT -> Icons.Default.Done
            MessageStatus.SENDING -> Icons.Default.Check
            MessageStatus.FAILED -> Icons.Default.Check
        },
        contentDescription = status.name,
        modifier = Modifier.size(14.dp),
        tint = tint
    )
}

@Composable
fun DateSeparator(
    date: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun TypingIndicator(
    userName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .width(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MessageReceived)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TelegramBlue.copy(alpha = 0.6f + (index * 0.1f)))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$userName is typing...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

private fun formatMessageTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}
