package com.cennet.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.cennet.app.MainActivity
import com.cennet.app.R
import com.cennet.app.data.repository.CennetRepository
import com.cennet.app.data.repository.PetStateStore
import com.cennet.app.model.CennetScreen
import com.cennet.app.model.drawingPrompts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

enum class WidgetKind { DRAWING, DIARY, PHOTO, PET, LETTERS, MOA }

abstract class CenoWidgetProvider(private val kind: WidgetKind) : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val result = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                ids.forEach { updateWidget(context, manager, it, kind) }
            } finally {
                result.finish()
            }
        }
    }
}

class DrawingWidgetProvider : CenoWidgetProvider(WidgetKind.DRAWING) {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_NEW_IDEA) {
            val manager = AppWidgetManager.getInstance(context)
            onUpdate(context, manager, manager.getAppWidgetIds(ComponentName(context, javaClass)))
        }
    }
}
class DiaryWidgetProvider : CenoWidgetProvider(WidgetKind.DIARY)
class PhotoWidgetProvider : CenoWidgetProvider(WidgetKind.PHOTO)
class PetWidgetProvider : CenoWidgetProvider(WidgetKind.PET)
class LettersWidgetProvider : CenoWidgetProvider(WidgetKind.LETTERS)
class MoaWidgetProvider : CenoWidgetProvider(WidgetKind.MOA)

private suspend fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int, kind: WidgetKind) {
    val views = RemoteViews(context.packageName, R.layout.widget_ceno_card)
    views.setViewVisibility(R.id.widget_art, View.VISIBLE)
    views.setViewVisibility(R.id.widget_refresh, View.GONE)
    views.setImageViewResource(R.id.widget_art, R.drawable.ceno_kitsune_foreground)

    val screen = when (kind) {
        WidgetKind.DRAWING, WidgetKind.PHOTO -> CennetScreen.HOME
        WidgetKind.DIARY -> CennetScreen.DIARY
        WidgetKind.PET -> CennetScreen.GARDEN
        WidgetKind.LETTERS -> CennetScreen.LETTERS
        WidgetKind.MOA -> CennetScreen.MOA
    }

    when (kind) {
        WidgetKind.DRAWING -> {
            views.setTextViewText(R.id.widget_eyebrow, "çizim ilhamı ✦")
            views.setTextViewText(R.id.widget_title, "bugünün minik fikri")
            views.setTextViewText(R.id.widget_body, drawingPrompts[Random.nextInt(drawingPrompts.size)])
            views.setTextViewText(R.id.widget_footer, "Ceno'da çizim köşesini aç ♡")
            views.setViewVisibility(R.id.widget_refresh, View.VISIBLE)
            views.setOnClickPendingIntent(R.id.widget_refresh, refreshIntent(context))
        }
        WidgetKind.DIARY -> {
            val latest = CennetRepository(context).allDiaryEntries().firstOrNull()
            views.setTextViewText(R.id.widget_eyebrow, "günlük ♡")
            views.setTextViewText(R.id.widget_title, latest?.date?.let(::friendlyDate) ?: "bugünün sayfası")
            views.setTextViewText(R.id.widget_body, latest?.text?.replace('\n', ' ') ?: "henüz yazılmış bir sayfa yok ♡")
            views.setTextViewText(R.id.widget_footer, if (latest == null) "ilk anını yaz" else "anını aç")
            setWidgetBitmap(context, views, latest?.photoUri, if (latest == null) R.drawable.ceno_kitsune_foreground else R.drawable.diary_default_memory)
        }
        WidgetKind.PHOTO -> {
            val repository = CennetRepository(context)
            val pool = repository.photoPool
            val selected = pool.randomOrNull() ?: repository.photoOfDay
            views.setTextViewText(R.id.widget_eyebrow, "günün fotoğrafı ❀")
            views.setTextViewText(R.id.widget_title, "minik bir hatıra")
            views.setTextViewText(R.id.widget_body, if (selected == null) "galeri izni verince anını kendisi seçer ♡" else "galerinden rastgele seçilen bugünün anısı")
            views.setTextViewText(R.id.widget_footer, "fotoğraf köşesini aç")
            setWidgetBitmap(context, views, selected, R.drawable.ceno_kitsune_foreground)
        }
        WidgetKind.PET -> {
            val stats = PetStateStore.loadAndDecay(context)
            val hunger = stats.hunger
            val happiness = stats.happiness
            val energy = stats.energy
            views.setTextViewText(R.id.widget_eyebrow, "minik dostum ♧")
            views.setTextViewText(R.id.widget_title, "Doggy seni bekliyor")
            views.setTextViewText(R.id.widget_body, "tokluk %$hunger  ·  mutluluk %$happiness  ·  enerji %$energy")
            views.setTextViewText(R.id.widget_footer, "besle, sev veya oyun oyna ♡")
            loadRawVideoFrame(context, R.raw.doggy_idle)?.let {
                views.setImageViewBitmap(R.id.widget_art, fitForWidget(it))
            } ?: views.setImageViewResource(R.id.widget_art, R.drawable.mascot_happy)
        }
        WidgetKind.LETTERS -> {
            val birthday = CennetRepository(context).birthday
            val unlocked = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM")) == birthday
            views.setTextViewText(R.id.widget_eyebrow, "mektuplar ✉")
            views.setTextViewText(R.id.widget_title, if (unlocked) "bir mektup seni bekliyor ♡" else "doğum gününde aç")
            views.setTextViewText(R.id.widget_body, if (unlocked) "iyi ki doğdun! özel mektubun artık açık." else "bu mektup henüz hazır değil ♡")
            views.setTextViewText(R.id.widget_footer, if (unlocked) "mektubu aç" else "kilitli · $birthday")
            views.setImageViewResource(R.id.widget_art, if (unlocked) R.drawable.mascot_birthday else R.drawable.mascot_sleep)
        }
        WidgetKind.MOA -> {
            val prefs = context.getSharedPreferences("moa", Context.MODE_PRIVATE)
            val song = prefs.getString("song", "").orEmpty()
            val rating = prefs.getInt("rating", 0).coerceIn(0, 5)
            views.setTextViewText(R.id.widget_eyebrow, "moa köşem ♡")
            views.setTextViewText(R.id.widget_title, song.ifBlank { "favorilerini seç" })
            views.setTextViewText(R.id.widget_body, if (rating == 0) "puan ve favori şarkı henüz seçilmedi" else "${"★".repeat(rating)}${"☆".repeat(5 - rating)}  ·  $rating / 5")
            views.setTextViewText(R.id.widget_footer, "MOA köşeni doldur ♡")
            views.setImageViewResource(R.id.widget_art, if (song.isBlank()) R.drawable.ceno_kitsune_foreground else songArtwork(song))
        }
    }

    views.setOnClickPendingIntent(R.id.widget_root, openScreenIntent(context, screen))
    manager.updateAppWidget(widgetId, views)
}

private fun openScreenIntent(context: Context, screen: CennetScreen): PendingIntent {
    val intent = Intent(context, MainActivity::class.java)
        .putExtra(MainActivity.EXTRA_SCREEN, screen.name)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    return PendingIntent.getActivity(context, 100 + screen.ordinal, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

private fun refreshIntent(context: Context): PendingIntent {
    val intent = Intent(context, DrawingWidgetProvider::class.java).setAction(ACTION_NEW_IDEA)
    return PendingIntent.getBroadcast(context, 501, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

private fun setWidgetBitmap(context: Context, views: RemoteViews, value: String?, fallback: Int) {
    val bitmap = value?.let { loadBitmap(context, it) }
        ?: BitmapFactory.decodeResource(context.resources, fallback)
    views.setImageViewBitmap(R.id.widget_art, fitForWidget(bitmap))
}

private fun loadBitmap(context: Context, value: String): Bitmap? = runCatching {
    when {
        value.startsWith("content:") || value.startsWith("file:") ->
            context.contentResolver.openInputStream(Uri.parse(value))?.use(BitmapFactory::decodeStream)
        else -> File(value).takeIf(File::exists)?.inputStream()?.use(BitmapFactory::decodeStream)
    }
}.getOrNull()

private fun fitForWidget(bitmap: Bitmap): Bitmap {
    val maxSide = 420
    if (bitmap.width <= maxSide && bitmap.height <= maxSide) return bitmap
    val scale = maxSide.toFloat() / maxOf(bitmap.width, bitmap.height)
    return Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt().coerceAtLeast(1), (bitmap.height * scale).toInt().coerceAtLeast(1), true)
}

private fun loadRawVideoFrame(context: Context, resource: Int): Bitmap? = runCatching {
    context.resources.openRawResourceFd(resource).use { descriptor ->
        MediaMetadataRetriever().run {
            try {
                setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            } finally {
                release()
            }
        }
    }
}.getOrNull()

private fun friendlyDate(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMMM"))
}.getOrDefault(value)

private fun songArtwork(song: String): Int = when (song) {
    "Sugar Rush Ride", "Farewell, Neverland" -> R.drawable.album_temptation
    "0X1=LOVESONG" -> R.drawable.song_0x1_lovesong
    "Blue Hour" -> R.drawable.album_blue_hour
    "Chasing That Feeling" -> R.drawable.song_chasing_that_feeling
    "CROWN" -> R.drawable.song_crown
    "LO\$ER=LO♡ER" -> R.drawable.song_loser_lover
    "Run Away" -> R.drawable.song_run_away
    "Good Boy Gone Bad" -> R.drawable.song_good_boy_gone_bad
    "Our Summer", "Magic" -> R.drawable.album_magic
    else -> R.drawable.song_deja_vu
}

object CenoWidgets {
    private val providers = listOf(
        DrawingWidgetProvider::class.java to WidgetKind.DRAWING,
        DiaryWidgetProvider::class.java to WidgetKind.DIARY,
        PhotoWidgetProvider::class.java to WidgetKind.PHOTO,
        PetWidgetProvider::class.java to WidgetKind.PET,
        LettersWidgetProvider::class.java to WidgetKind.LETTERS,
        MoaWidgetProvider::class.java to WidgetKind.MOA
    )

    fun refreshAll(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val manager = AppWidgetManager.getInstance(appContext)
            providers.forEach { (provider, kind) ->
                manager.getAppWidgetIds(ComponentName(appContext, provider)).forEach { id ->
                    updateWidget(appContext, manager, id, kind)
                }
            }
        }
    }
}

private const val ACTION_NEW_IDEA = "com.cennet.app.widget.NEW_IDEA"
