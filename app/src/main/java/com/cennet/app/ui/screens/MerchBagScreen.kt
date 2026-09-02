package com.cennet.app.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cennet.app.R
import com.cennet.app.data.repository.CennetRepository
import com.cennet.app.model.MerchSlot
import com.cennet.app.ui.theme.cennetColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val BAG_ASPECT_RATIO = 1.5f
private const val GRID_SIZE = 4
private val overlayGeometry = BagOverlayGeometry(0.4004f, 0.5313f, 0.2435f, 0.3496f)

private data class BagOverlayGeometry(
    val xPercent: Float,
    val yPercent: Float,
    val widthPercent: Float,
    val heightPercent: Float
)

@Composable
fun MerchBagScreen(repository: CennetRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var slots by remember { mutableStateOf(repository.loadMerchSlots()) }
    var selectedSlot by remember { mutableStateOf<Int?>(null) }
    var editorSlot by remember { mutableStateOf<Int?>(null) }
    var pendingSlot by remember { mutableStateOf<Int?>(null) }

    fun persist(updated: List<MerchSlot>) {
        slots = updated
        repository.saveMerchSlots(updated)
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val slotIndex = pendingSlot
        pendingSlot = null
        if (uri != null && slotIndex != null) {
            scope.launch {
                copyIntoPrivateStorage(context, uri, slotIndex)?.let { path ->
                    val old = slots[slotIndex]
                    persist(slots.toMutableList().also { it[slotIndex] = old.copy(localImagePath = path) })
                    selectedSlot = slotIndex
                    editorSlot = slotIndex
                }
            }
        }
    }

    fun pickFor(index: Int) {
        selectedSlot = index
        editorSlot = null
        pendingSlot = index
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun remove(index: Int) {
        removePrivateImage(context, slots[index].localImagePath)
        persist(slots.toMutableList().also { it[index] = MerchSlot(index + 1) })
        selectedSlot = null
        editorSlot = null
    }

    fun resize(index: Int, columns: Int, rows: Int) {
        if (!isSpanValid(slots, index, columns, rows)) return
        persist(slots.toMutableList().also { it[index] = it[index].copy(columnSpan = columns, rowSpan = rows) })
        selectedSlot = index
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        MerchBagCanvas(
            slots = slots,
            selectedSlot = selectedSlot,
            editorSlot = editorSlot,
            onEmptySlot = ::pickFor,
            onEdit = { selectedSlot = it; editorSlot = it },
            onDismissEditor = { editorSlot = null },
            onPick = ::pickFor,
            onRemove = ::remove,
            onResize = ::resize,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MerchBagCanvas(
    slots: List<MerchSlot>,
    selectedSlot: Int?,
    editorSlot: Int?,
    onEmptySlot: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    onDismissEditor: () -> Unit,
    onPick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onResize: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val imageWidth = if (maxWidth / maxHeight > BAG_ASPECT_RATIO) maxHeight * BAG_ASPECT_RATIO else maxWidth
        val imageHeight = imageWidth / BAG_ASPECT_RATIO
        Box(Modifier.width(imageWidth).height(imageHeight)) {
            MerchBagImage(Modifier.fillMaxSize())
            MerchSlotOverlay(
                slots, selectedSlot, editorSlot, onEmptySlot, onEdit, onDismissEditor, onPick, onRemove, onResize,
                Modifier.offset(imageWidth * overlayGeometry.xPercent, imageHeight * overlayGeometry.yPercent)
                    .width(imageWidth * overlayGeometry.widthPercent)
                    .height(imageHeight * overlayGeometry.heightPercent)
            )
        }
    }
}

@Composable
private fun MerchBagImage(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.merch_bag), null, modifier, contentScale = ContentScale.Fit)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MerchSlotOverlay(
    slots: List<MerchSlot>,
    selectedSlot: Int?,
    editorSlot: Int?,
    onEmptySlot: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    onDismissEditor: () -> Unit,
    onPick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onResize: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val gap = 5.dp
        val cellWidth = (maxWidth - gap * 3) / GRID_SIZE
        val cellHeight = (maxHeight - gap * 3) / GRID_SIZE

        repeat(16) { index ->
            if (coveringSlot(slots, index) == null) {
                val row = index / GRID_SIZE
                val column = index % GRID_SIZE
                Box(
                    Modifier.offset((cellWidth + gap) * column, (cellHeight + gap) * row)
                        .size(cellWidth, cellHeight)
                        .combinedClickable(onClick = { onEmptySlot(index) }, onLongClick = {})
                )
            }
        }

        slots.forEachIndexed { index, slot ->
            if (slot.localImagePath != null) {
                val row = index / GRID_SIZE
                val column = index % GRID_SIZE
                val tileWidth = cellWidth * slot.columnSpan + gap * (slot.columnSpan - 1)
                val tileHeight = cellHeight * slot.rowSpan + gap * (slot.rowSpan - 1)
                MerchImageTile(
                    slot = slot,
                    selected = selectedSlot == index,
                    editorOpen = editorSlot == index,
                    canResize = { columns, rows -> isSpanValid(slots, index, columns, rows) },
                    onEdit = { onEdit(index) },
                    onDismissEditor = onDismissEditor,
                    onPick = { onPick(index) },
                    onRemove = { onRemove(index) },
                    onResize = { columns, rows -> onResize(index, columns, rows) },
                    modifier = Modifier.offset((cellWidth + gap) * column, (cellHeight + gap) * row)
                        .size(tileWidth, tileHeight).zIndex(if (selectedSlot == index) 2f else 1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MerchImageTile(
    slot: MerchSlot,
    selected: Boolean,
    editorOpen: Boolean,
    canResize: (Int, Int) -> Boolean,
    onEdit: () -> Unit,
    onDismissEditor: () -> Unit,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    onResize: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier.clip(shape)
            .then(if (selected) Modifier.border(2.dp, cennetColors.forest, shape) else Modifier)
            .combinedClickable(onClick = onEdit, onLongClick = onEdit)
    ) {
        LocalSlotImage(slot.localImagePath, Modifier.fillMaxSize())
        if (selected) {
            Text(
                "${slot.columnSpan}×${slot.rowSpan}",
                Modifier.align(Alignment.TopEnd).background(cennetColors.forest, RoundedCornerShape(bottomStart = 8.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold
            )
        }
        DropdownMenu(expanded = editorOpen, onDismissRequest = onDismissEditor, modifier = Modifier.background(cennetColors.cream)) {
            Text("kapladığı alan  ${slot.columnSpan} × ${slot.rowSpan}", Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = cennetColors.forest)
            Text("genişlik", Modifier.padding(horizontal = 14.dp), fontSize = 9.sp, color = cennetColors.mutedText)
            Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                (1..4).forEach { columns -> SizeChoice(columns, columns == slot.columnSpan, canResize(columns, slot.rowSpan)) { onResize(columns, slot.rowSpan) } }
            }
            Text("yükseklik", Modifier.padding(horizontal = 14.dp), fontSize = 9.sp, color = cennetColors.mutedText)
            Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                (1..4).forEach { rows -> SizeChoice(rows, rows == slot.rowSpan, canResize(slot.columnSpan, rows)) { onResize(slot.columnSpan, rows) } }
            }
            Text("sağa ve aşağı doğru büyür ♡", Modifier.padding(horizontal = 14.dp, vertical = 7.dp), fontSize = 8.sp, color = cennetColors.mutedText)
            HorizontalDivider(color = cennetColors.border)
            DropdownMenuItem(text = { Text("görseli değiştir", fontSize = 11.sp) }, onClick = onPick)
            DropdownMenuItem(text = { Text("slotu boşalt", fontSize = 11.sp) }, onClick = onRemove)
        }
    }
}

@Composable
private fun SizeChoice(value: Int, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(34.dp).alpha(if (enabled) 1f else .28f).clip(RoundedCornerShape(9.dp))
            .background(if (selected) cennetColors.lightGreen else cennetColors.cream)
            .border(if (selected) 1.5.dp else .7.dp, if (selected) cennetColors.forest else cennetColors.border, RoundedCornerShape(9.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(value.toString(), fontSize = 11.sp, color = cennetColors.text) }
}

private fun coveringSlot(slots: List<MerchSlot>, cellIndex: Int): Int? {
    val cellRow = cellIndex / GRID_SIZE
    val cellColumn = cellIndex % GRID_SIZE
    return slots.indexOfFirst { slot ->
        if (slot.localImagePath == null) false else {
            val index = slot.id - 1
            val row = index / GRID_SIZE
            val column = index % GRID_SIZE
            cellRow in row until row + slot.rowSpan && cellColumn in column until column + slot.columnSpan
        }
    }.takeIf { it >= 0 }
}

private fun isSpanValid(slots: List<MerchSlot>, index: Int, columns: Int, rows: Int): Boolean {
    val anchorRow = index / GRID_SIZE
    val anchorColumn = index % GRID_SIZE
    if (columns !in 1..4 || rows !in 1..4 || anchorColumn + columns > GRID_SIZE || anchorRow + rows > GRID_SIZE) return false
    return slots.withIndex().filter { it.index != index && it.value.localImagePath != null }.none { other ->
        val otherRow = other.index / GRID_SIZE
        val otherColumn = other.index % GRID_SIZE
        anchorColumn < otherColumn + other.value.columnSpan && anchorColumn + columns > otherColumn &&
            anchorRow < otherRow + other.value.rowSpan && anchorRow + rows > otherRow
    }
}

@Composable
private fun LocalSlotImage(path: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(path) { path?.let { runCatching { decodeSlotBitmap(context, it) }.getOrNull() } }
    if (bitmap != null) Image(bitmap.asImageBitmap(), null, modifier.clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
}

private suspend fun copyIntoPrivateStorage(context: Context, uri: Uri, slotIndex: Int): String? = withContext(Dispatchers.IO) {
    runCatching {
        val directory = File(context.filesDir, "merch_slots").apply { mkdirs() }
        val target = File(directory, "slot_${slotIndex + 1}.img")
        val temporary = File(directory, "slot_${slotIndex + 1}.tmp")
        context.contentResolver.openInputStream(uri)?.use { input -> temporary.outputStream().use { output -> input.copyTo(output) } } ?: return@runCatching null
        temporary.copyTo(target, overwrite = true); temporary.delete(); target.absolutePath
    }.getOrNull()
}

private fun removePrivateImage(context: Context, path: String?) {
    if (path == null) return
    runCatching { val directory = File(context.filesDir, "merch_slots"); val target = File(path); if (target.parentFile?.canonicalPath == directory.canonicalPath) target.delete() }
}

private fun decodeSlotBitmap(context: Context, value: String): android.graphics.Bitmap? {
    fun sampleSize(width: Int, height: Int): Int { var sample = 1; while (width / sample > 512 || height / sample > 512) sample *= 2; return sample }
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    if (value.startsWith("content://")) {
        val uri = Uri.parse(value); context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight) }) }
    }
    BitmapFactory.decodeFile(value, bounds)
    return BitmapFactory.decodeFile(value, BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight) })
}
