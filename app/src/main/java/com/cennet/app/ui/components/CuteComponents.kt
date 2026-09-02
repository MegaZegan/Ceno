package com.cennet.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.cennet.app.ui.theme.cennetColors
import com.cennet.app.R

@Composable
fun CuteCard(
    modifier: Modifier = Modifier,
    background: Color = cennetColors.cream,
    corner: Dp = 20.dp,
    padding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(corner)
    val base = modifier
        .shadow(3.dp, shape, ambientColor = Color.Black.copy(.10f), spotColor = Color.Black.copy(.07f))
        .clip(shape)
        .background(background)
        .border(.7.dp, cennetColors.border.copy(.72f), shape)
    Box(
        modifier = if (onClick == null) base.padding(padding) else base.clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick
        ).padding(padding),
        content = content
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, heart: Boolean = false) {
    Text(
        text = text + if (heart) "  ♥" else "",
        color = cennetColors.darkForest,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        modifier = modifier
    )
}

@Composable
fun DarkSectionHeader(text: String, modifier: Modifier = Modifier) {
    Box(modifier.background(cennetColors.forest, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)).padding(horizontal = 18.dp, vertical = 10.dp)) {
        Text(text.lowercase() + "  ♥", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun Mascot(modifier: Modifier = Modifier, mood: String = "idle", bounce: Boolean = false) {
    val transition = rememberInfiniteTransition(label = "mascot")
    val offset by transition.animateFloat(
        0f, if (bounce) -3f else 0f,
        infiniteRepeatable(tween(1300, easing = EaseInOut), RepeatMode.Reverse), label = "bounce"
    )
    val resource = when (mood) {
        "happy" -> R.drawable.mascot_happy
        "sleep" -> R.drawable.mascot_sleep
        "birthday" -> R.drawable.mascot_birthday
        else -> R.drawable.mascot_idle
    }
    Image(
        painter = painterResource(resource),
        contentDescription = "Ceno'nun minik tilki maskotu",
        modifier = modifier.offset(y = offset.dp).aspectRatio(1f),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun DoodleSparkles(modifier: Modifier = Modifier, tint: Color = cennetColors.midGreen) {
    Canvas(modifier) {
        listOf(Offset(size.width*.15f,size.height*.35f) to 5f, Offset(size.width*.62f,size.height*.18f) to 7f, Offset(size.width*.86f,size.height*.68f) to 4f).forEach { (p,r) ->
            val path = Path().apply { moveTo(p.x,p.y-r); lineTo(p.x+r*.35f,p.y-r*.25f); lineTo(p.x+r,p.y); lineTo(p.x+r*.35f,p.y+r*.25f); lineTo(p.x,p.y+r); lineTo(p.x-r*.35f,p.y+r*.25f); lineTo(p.x-r,p.y); lineTo(p.x-r*.35f,p.y-r*.25f); close() }
            drawPath(path,tint.copy(.55f),style=Stroke(1.3f))
        }
    }
}

@Composable
fun UriImage(uri: String?, modifier: Modifier, placeholder: @Composable BoxScope.() -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        uri?.let { value -> runCatching {
            if (value.startsWith("content://") || value.startsWith("file://")) {
                context.contentResolver.openInputStream(android.net.Uri.parse(value))?.use { android.graphics.BitmapFactory.decodeStream(it) }
            } else android.graphics.BitmapFactory.decodeFile(value)
        }.getOrNull() }
    }
    Box(modifier.clip(RoundedCornerShape(10.dp)).background(cennetColors.sage), contentAlignment = Alignment.Center) {
        if (bitmap != null) Image(bitmap.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) else placeholder()
    }
}

@Composable
fun Tape(modifier: Modifier = Modifier, color: Color = cennetColors.midGreen.copy(.72f)) {
    Box(modifier.rotate(-2f).background(color, RoundedCornerShape(2.dp)))
}

@Composable
fun PaperNote(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.rotate(-1.5f)) {
        CuteCard(Modifier.fillMaxSize(), background = Color(0xFFFFF9ED), corner = 5.dp, padding = 16.dp) { Column(content = content) }
        Tape(Modifier.width(48.dp).height(14.dp).align(Alignment.TopCenter).offset(y = (-8).dp))
    }
}

@Composable
fun SoftButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(cennetColors.sage).clickable(onClick = onClick).padding(horizontal = 13.dp, vertical = 7.dp)) {
        Text(text, fontSize = 11.sp, color = cennetColors.text)
    }
}
