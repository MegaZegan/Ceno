package com.cennet.app.ui.components

import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

enum class DoggyMood { IDLE, HAPPY, SURPRISED, ANGRY }

@Composable
fun DoggyAnimation(@RawRes videoRes: Int, modifier: Modifier = Modifier) {
    key(videoRes) {
        Box(modifier) {
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { context ->
                    VideoView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        setBackgroundColor(Color.TRANSPARENT)
                        setVideoURI(Uri.parse("android.resource://${context.packageName}/$videoRes"))
                        setOnPreparedListener { player ->
                            player.isLooping = true
                            player.setVolume(0f, 0f)
                            start()
                        }
                        setOnErrorListener { _, _, _ -> true }
                    }
                },
                update = { if (!it.isPlaying) it.start() },
                onRelease = { it.stopPlayback() }
            )
        }
    }
}
