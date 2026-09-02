package com.cennet.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as rowItemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.cennet.app.ui.components.*
import com.cennet.app.ui.theme.cennetColors
import com.cennet.app.widget.CenoWidgets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private data class RefItem(val id: String, val imagePath: String, val note: String, val folder: String)
private data class RefDraft(val id: String?, val imagePath: String, val note: String, val folder: String)

private val defaultReferenceFolders = listOf("pozlar", "kıyafetler", "renkler", "karakterler", "çalışmalar")
private val folderGlyphs = listOf("♧", "♙", "●", "◎", "▤", "♡", "◇", "✦")
private val folderMarks = listOf("♡", "◇", "●", "❀", "✦", "☆", "♧", "☾")

@Composable
fun ReferenceShelfScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("references", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    fun readItems(): List<RefItem> = prefs.all.mapNotNull { (key, value) ->
        if (!key.startsWith("ref_")) null
        else (value as? String)?.split("¦", limit = 3)?.takeIf { it.size == 3 }
            ?.let { RefItem(key, it[0], it[1], it[2]) }
    }.sortedByDescending { it.id }

    fun readFolders(currentItems: List<RefItem>): List<String> {
        val custom = prefs.getString("folders_custom", "").orEmpty().lines().map(String::trim).filter(String::isNotBlank)
        return (defaultReferenceFolders + custom + currentItems.map { it.folder }).distinct()
    }

    var items by remember { mutableStateOf(readItems()) }
    var folders by remember { mutableStateOf(readFolders(items)) }
    var selected by remember { mutableStateOf(folders.first()) }
    var editor by remember { mutableStateOf<RefDraft?>(null) }
    var showFolderCreator by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    fun reload() {
        items = readItems()
        folders = readFolders(items)
        if (selected !in folders) selected = folders.first()
    }

    fun persistFolders(updated: List<String>) {
        val custom = updated.filterNot { it in defaultReferenceFolders }
        prefs.edit().putString("folders_custom", custom.joinToString("\n")).apply()
        folders = defaultReferenceFolders + custom
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            val localPath = copyReferenceImage(context, uri)
            val stablePath = localPath ?: run {
                runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                uri.toString()
            }
            editor = RefDraft(null, stablePath, "", selected)
            busy = false
            status = "görsel hazır; klasörünü seçip kaydet ♡"
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DarkSectionHeader("REFERANS RAFI", Modifier.fillMaxWidth())
        CuteCard(Modifier.fillMaxWidth().weight(1f), corner = 18.dp, padding = 18.dp) {
            DoodleSparkles(Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize()) {
                ReferenceHeader(onAdd = { picker.launch(arrayOf("image/*")) }, onAddFolder = { showFolderCreator = true })
                if (busy || status.isNotBlank()) Text(if (busy) "görsel güvenle kaydediliyor..." else status, fontSize = 9.sp, color = cennetColors.midGreen)
                Spacer(Modifier.height(10.dp))
                LazyRow(Modifier.fillMaxWidth().height(116.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItemsIndexed(folders, key = { _, folder -> folder }) { index, folder ->
                        val count = items.count { it.folder == folder }
                        FolderCard(folder, folderGlyphs[index % folderGlyphs.size], folderMarks[index % folderMarks.size], folder == selected, count, Modifier.width(205.dp).fillMaxHeight()) { selected = folder }
                    }
                }
                Spacer(Modifier.height(15.dp))
                val userItems = items.filter { it.folder == selected }
                if (userItems.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("bu klasör şimdilik bomboş ♡", fontFamily = FontFamily.Cursive, fontSize = 22.sp, color = cennetColors.forest)
                            Spacer(Modifier.height(8.dp)); SoftButton("＋ ilk görseli ekle") { picker.launch(arrayOf("image/*")) }
                        }
                    }
                } else LazyVerticalGrid(
                    columns = GridCells.Fixed(6), modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    gridItemsIndexed(userItems, key = { _, item -> item.id }) { index, item ->
                        ReferenceThumbnail(
                            item = item, index = index,
                            modifier = Modifier.fillMaxWidth().aspectRatio(.82f),
                            onOpen = { item -> editor = RefDraft(item.id, item.imagePath, item.note, item.folder) },
                            onDelete = { item ->
                                prefs.edit().remove(item.id).apply(); deleteReferenceImage(context, item.imagePath)
                                reload(); CenoWidgets.refreshAll(context); status = "görsel kaldırıldı"
                            }
                        )
                    }
                }
                Box(Modifier.fillMaxWidth().padding(top = 9.dp), contentAlignment = Alignment.Center) {
                    Text("✦   görsellere dokunarak notunu ve klasörünü değiştirebilirsin ♡   ✦", fontSize = 9.sp, color = cennetColors.mutedText)
                }
            }
        }
    }

    editor?.let { draft ->
        ReferenceEditor(
            draft = draft, folders = folders,
            onDismiss = {
                if (draft.id == null) deleteReferenceImage(context, draft.imagePath)
                editor = null
            },
            onSave = { updated ->
                val id = updated.id ?: "ref_${System.currentTimeMillis()}"
                val safeNote = updated.note.replace("¦", " ").trim()
                prefs.edit().putString(id, "${updated.imagePath}¦$safeNote¦${updated.folder}").apply()
                selected = updated.folder; editor = null; reload(); CenoWidgets.refreshAll(context)
                status = if (updated.id == null) "görsel $selected klasörüne eklendi ♡" else "görselin klasörü ve notu güncellendi ♡"
            }
        )
    }

    if (showFolderCreator) FolderManager(
        folders = folders, name = newFolderName, onName = { newFolderName = it }, onDismiss = { showFolderCreator = false; newFolderName = "" },
        onCreate = {
            val clean = newFolderName.replace("¦", " ").replace("\n", " ").trim().take(24)
            if (clean.isNotBlank() && clean !in folders) {
                persistFolders(folders + clean); selected = clean; newFolderName = ""; showFolderCreator = false; status = "$clean klasörü hazır ♡"
            }
        },
        onDelete = { folder ->
            val fallback = "çalışmalar"
            items.filter { it.folder == folder }.forEach { item -> prefs.edit().putString(item.id, "${item.imagePath}¦${item.note.replace("¦", " ")}¦$fallback").apply() }
            persistFolders(folders - folder); if (selected == folder) selected = fallback; reload(); status = "$folder kaldırıldı; görseller çalışmalara taşındı"
        }
    )
}

@Composable
private fun ReferenceHeader(onAdd: () -> Unit, onAddFolder: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(86.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("referans rafı", fontFamily = FontFamily.Cursive, fontSize = 31.sp, color = cennetColors.forest)
            Text("her görsel kendi klasöründe, her fikir güvende ♡", fontSize = 10.sp, color = cennetColors.mutedText)
        }
        Mascot(Modifier.size(66.dp), mood = "happy")
        Spacer(Modifier.width(14.dp)); SoftButton("＋ klasör", onClick = onAddFolder)
        Spacer(Modifier.width(8.dp)); SoftButton("＋ görsel ekle", onClick = onAdd)
    }
}

@Composable
private fun FolderCard(name: String, glyph: String, mark: String, active: Boolean, count: Int, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.clickable(onClick = onClick)) {
        Box(Modifier.fillMaxWidth(.42f).height(23.dp).background(if (active) cennetColors.midGreen else cennetColors.lightGreen, RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp)))
        Box(Modifier.fillMaxSize().padding(top = 13.dp).background(if (active) cennetColors.lightGreen else Color(0xFFF5F1DF), RoundedCornerShape(10.dp)).border(.8.dp, cennetColors.midGreen, RoundedCornerShape(10.dp)).padding(14.dp)) {
            Column(Modifier.align(Alignment.CenterStart)) { Text(name, fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.SemiBold, color = cennetColors.text); Text("$count görsel", fontSize = 8.sp, color = cennetColors.mutedText) }
            Text(glyph, Modifier.align(Alignment.BottomEnd), fontSize = 29.sp, color = cennetColors.forest.copy(.68f))
            Text(mark, Modifier.align(Alignment.TopEnd), fontSize = 16.sp, color = cennetColors.midGreen)
        }
    }
}

@Composable
private fun ReferenceThumbnail(item: RefItem, index: Int, modifier: Modifier, onOpen: (RefItem) -> Unit, onDelete: (RefItem) -> Unit) {
    CuteCard(modifier.clickable { onOpen(item) }, corner = 10.dp, padding = 8.dp) {
        Column {
            UriImage(item.imagePath, Modifier.fillMaxWidth().weight(1f)) { ReferenceArt(index) }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.note.ifBlank { "isimsiz ilham" }, fontSize = 8.sp, maxLines = 1, color = cennetColors.mutedText, modifier = Modifier.weight(1f))
                Text("×", Modifier.clickable { onDelete(item) }.padding(start = 5.dp), color = cennetColors.mutedText)
            }
        }
        if (index % 4 == 0) Tape(Modifier.width(42.dp).height(9.dp).align(Alignment.TopCenter).offset(y = (-11).dp), cennetColors.midGreen.copy(.65f))
    }
}

@Composable
private fun ReferenceEditor(draft: RefDraft, folders: List<String>, onDismiss: () -> Unit, onSave: (RefDraft) -> Unit) {
    var note by remember(draft) { mutableStateOf(draft.note) }
    var folder by remember(draft) { mutableStateOf(draft.folder.takeIf { it in folders } ?: folders.first()) }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(.28f)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
        CuteCard(Modifier.width(540.dp).height(500.dp).clickable { }, corner = 24.dp, padding = 22.dp) {
            Column(Modifier.fillMaxSize()) {
                Text(if (draft.id == null) "yeni görselini yerleştir ♡" else "görselini düzenle ♡", fontFamily = FontFamily.Cursive, fontSize = 26.sp, color = cennetColors.forest)
                Spacer(Modifier.height(10.dp)); UriImage(draft.imagePath, Modifier.fillMaxWidth().height(185.dp)) { Text("görsel açılamadı", color = cennetColors.mutedText) }
                Spacer(Modifier.height(9.dp))
                BasicTextField(note, { note = it }, Modifier.fillMaxWidth().background(cennetColors.sage.copy(.45f), RoundedCornerShape(10.dp)).padding(10.dp), textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = cennetColors.text), decorationBox = { inner -> if (note.isEmpty()) Text("bu görsele kısa bir not...", fontSize = 10.sp, color = cennetColors.mutedText); inner() })
                Spacer(Modifier.height(9.dp)); Text("hangi klasöre gitsin?", fontSize = 9.sp, color = cennetColors.mutedText)
                LazyRow(Modifier.fillMaxWidth().height(38.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowItemsIndexed(folders) { _, value -> Box(Modifier.background(if (folder == value) cennetColors.lightGreen else cennetColors.background, RoundedCornerShape(9.dp)).clickable { folder = value }.padding(horizontal = 10.dp, vertical = 7.dp)) { Text(value, fontSize = 8.sp) } }
                }
                Spacer(Modifier.weight(1f)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { SoftButton("vazgeç", onClick = onDismiss); Spacer(Modifier.width(8.dp)); SoftButton(if (draft.id == null) "görseli kaydet ♡" else "değişiklikleri kaydet ♡") { onSave(draft.copy(note = note, folder = folder)) } }
            }
        }
    }
}

@Composable
private fun FolderManager(folders: List<String>, name: String, onName: (String) -> Unit, onDismiss: () -> Unit, onCreate: () -> Unit, onDelete: (String) -> Unit) {
    val custom = folders.filterNot { it in defaultReferenceFolders }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(.28f)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
        CuteCard(Modifier.width(430.dp).height(285.dp).clickable { }, corner = 24.dp, padding = 22.dp) {
            Column(Modifier.fillMaxSize()) {
                Text("yeni bir klasör ♡", fontFamily = FontFamily.Cursive, fontSize = 27.sp, color = cennetColors.forest)
                Text("kendi raflarını oluştur; görsellerini sonra istediğin klasöre taşı", fontSize = 9.sp, color = cennetColors.mutedText)
                Spacer(Modifier.height(13.dp)); BasicTextField(name, onName, Modifier.fillMaxWidth().background(cennetColors.sage.copy(.45f), RoundedCornerShape(10.dp)).padding(11.dp), singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = cennetColors.text), decorationBox = { inner -> if (name.isEmpty()) Text("ör. arka planlar", fontSize = 10.sp, color = cennetColors.mutedText); inner() })
                if (custom.isNotEmpty()) { Spacer(Modifier.height(12.dp)); Text("eklediğin klasörler", fontSize = 9.sp, color = cennetColors.mutedText); LazyRow(Modifier.fillMaxWidth().height(38.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) { rowItemsIndexed(custom) { _, folder -> Box(Modifier.background(cennetColors.background, RoundedCornerShape(9.dp)).padding(horizontal = 9.dp, vertical = 6.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(folder, fontSize = 8.sp); Text("  ×", Modifier.clickable { onDelete(folder) }, color = cennetColors.mutedText) } } } } }
                Spacer(Modifier.weight(1f)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { SoftButton("kapat", onClick = onDismiss); Spacer(Modifier.width(8.dp)); SoftButton("klasörü oluştur ♡", onClick = onCreate) }
            }
        }
    }
}

private suspend fun copyReferenceImage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
    runCatching {
        val directory = File(context.filesDir, "reference_images").apply { mkdirs() }
        val target = File(directory, "reference_${System.currentTimeMillis()}.img")
        val temporary = File(directory, "${target.name}.tmp")
        context.contentResolver.openInputStream(uri)?.use { input -> temporary.outputStream().use { output -> input.copyTo(output) } } ?: return@runCatching null
        temporary.copyTo(target, overwrite = true); temporary.delete(); target.absolutePath
    }.getOrNull()
}

private fun deleteReferenceImage(context: Context, value: String) {
    if (value.startsWith("content://") || value.startsWith("file://")) return
    runCatching {
        val directory = File(context.filesDir, "reference_images").canonicalFile
        val target = File(value).canonicalFile
        if (target.parentFile == directory) target.delete()
    }
}

@Composable
private fun BoxScope.ReferenceArt(index: Int) {
    val colors = cennetColors
    Canvas(Modifier.fillMaxSize().padding(8.dp)) {
        drawRoundRect(listOf(colors.sage, colors.peach.copy(.6f), colors.lightGreen.copy(.7f))[index % 3], cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f))
        when (index % 6) {
            0 -> repeat(5) { i -> val x = size.width * (.18f + i * .16f); drawLine(colors.forest, Offset(x, size.height*.83f), Offset(x, size.height*(.45f-(i%2)*.1f)), 3f); drawCircle(colors.midGreen, size.width*.07f, Offset(x,size.height*(.42f-(i%2)*.1f))) }
            1 -> { drawCircle(colors.cream,size.minDimension*.22f,Offset(size.width*.5f,size.height*.36f)); drawRoundRect(colors.forest.copy(.72f),Offset(size.width*.28f,size.height*.53f),Size(size.width*.44f,size.height*.34f),androidx.compose.ui.geometry.CornerRadius(20f)); drawArc(colors.darkForest,180f,180f,false,Offset(size.width*.3f,size.height*.17f),Size(size.width*.4f,size.height*.35f),style=Stroke(8f)) }
            2 -> { drawCircle(Color.White.copy(.8f),size.width*.12f,Offset(size.width*.72f,size.height*.22f)); val hill=Path().apply{moveTo(0f,size.height*.8f);quadraticTo(size.width*.45f,size.height*.48f,size.width,size.height*.72f);lineTo(size.width,size.height);lineTo(0f,size.height);close()};drawPath(hill,colors.midGreen.copy(.7f));drawLine(colors.forest,Offset(size.width*.52f,size.height*.75f),Offset(size.width*.52f,size.height*.42f),4f);drawCircle(colors.forest,size.width*.1f,Offset(size.width*.52f,size.height*.4f)) }
            3 -> repeat(9){i->drawCircle(listOf(colors.forest,colors.midGreen,colors.lightGreen)[i%3],size.width*.07f,Offset(size.width*(.25f+(i%3)*.25f),size.height*(.25f+(i/3)*.25f)))}
            4 -> { repeat(3){i->drawRoundRect(colors.cream.copy(.9f),Offset(size.width*(.13f+i*.24f),size.height*(.18f+i*.08f)),Size(size.width*.38f,size.height*.55f),androidx.compose.ui.geometry.CornerRadius(8f),style=Stroke(2f))}; drawCircle(colors.pink,size.width*.09f,Offset(size.width*.72f,size.height*.7f)) }
            else -> { drawOval(colors.cream.copy(.85f),Offset(size.width*.2f,size.height*.2f),Size(size.width*.6f,size.height*.55f));drawCircle(colors.text,3f,Offset(size.width*.43f,size.height*.44f));drawCircle(colors.text,3f,Offset(size.width*.57f,size.height*.44f));drawArc(colors.text,15f,150f,false,Offset(size.width*.43f,size.height*.46f),Size(size.width*.14f,size.height*.12f),style=Stroke(2f)) }
        }
    }
}
