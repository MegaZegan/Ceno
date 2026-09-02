package com.cennet.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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

private data class RefItem(val uri: String, val note: String, val folder: String)
private data class StarterRef(val title: String, val art: Int)

private val referenceFolders = listOf("pozlar", "kıyafetler", "renkler", "karakterler", "çalışmalar")
private val folderGlyphs = listOf("♧", "♙", "●", "◎", "▤")
private val starterCounts = listOf(12, 23, 18, 31, 15)
private val starterTitles = mapOf(
    "pozlar" to listOf("sessiz duruş", "hareket çizgisi", "oturan figür", "dans pozu", "el çalışması", "yürüyüş", "ikili poz", "profil", "kumaş hareketi", "minik jestler", "ışık pozu", "hızlı eskiz"),
    "kıyafetler" to listOf("katmanlı kombin", "yeşil elbise", "okul stili", "yumuşak kazak", "kurdele detayı", "bahar ceketi", "sahne kıyafeti", "vintage gömlek", "rahat gün", "çantalar", "ayakkabı fikri", "renkli aksesuar"),
    "renkler" to listOf("orman paleti", "adaçayı", "şeftali tonu", "yağmur mavisi", "krem kağıt", "çiçek pembesi", "gece yeşili", "bahar ışığı", "soluk sarı", "mint rüyası", "toprak tonu", "pastel karışım"),
    "karakterler" to listOf("orman dostu", "utangaç kahraman", "minik cadı", "çiçek bekçisi", "bulut çocuk", "yıldız gezgini", "çay perisi", "uykulu çizer", "bahçe ruhu", "mektup taşıyıcı", "ay tavşanı", "yağmur arkadaşı"),
    "çalışmalar" to listOf("wip eskizi", "sayfa düzeni", "renk denemesi", "karakter notu", "arka plan taslağı", "ifade sayfası", "ışık testi", "prop çizimleri", "kompozisyon", "hikâye karesi", "temiz çizgi", "son dokunuş")
)

@Composable
fun ReferenceShelfScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("references", Context.MODE_PRIVATE) }
    fun readItems(): List<RefItem> = prefs.all.mapNotNull { (key, value) ->
        if (!key.startsWith("ref_")) null else (value as? String)?.split("¦", limit = 3)?.takeIf { it.size == 3 }?.let { RefItem(it[0], it[1], it[2]) }
    }
    var selected by remember { mutableStateOf("pozlar") }
    var items by remember { mutableStateOf(readItems()) }
    var pendingUri by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; pendingUri = it.toString() }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DarkSectionHeader("REFERANS RAFI", Modifier.fillMaxWidth())
        CuteCard(Modifier.fillMaxWidth().weight(1f), corner = 18.dp, padding = 18.dp) {
            DoodleSparkles(Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize()) {
                ReferenceHeader { picker.launch(arrayOf("image/*")) }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    referenceFolders.forEachIndexed { index, folder ->
                        FolderCard(folder, folderGlyphs[index], folder == selected, starterCounts[index] + items.count { it.folder == folder }, Modifier.weight(1f)) { selected = folder }
                    }
                }
                Spacer(Modifier.height(16.dp))
                val userItems = items.filter { it.folder == selected }
                val cards = userItems.map { Pair(it, null) } + starterTitles.getValue(selected).mapIndexed { index, title -> Pair(null, StarterRef(title, index)) }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(cards) { index, card ->
                        ReferenceThumbnail(card.first, card.second, index, Modifier.fillMaxWidth().aspectRatio(.82f)) {
                            card.first?.let { item ->
                                prefs.all.entries.firstOrNull { it.value == "${item.uri}¦${item.note}¦${item.folder}" }?.key?.let { prefs.edit().remove(it).apply() }
                                CenoWidgets.refreshAll(context)
                                items = readItems()
                            }
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center) {
                    Text("✦   ilham her yerde; önemli olan onu fark etmek ♡   ✦", fontSize = 10.sp, color = cennetColors.mutedText)
                }
            }
        }
    }

    pendingUri?.let { uri ->
        Box(Modifier.fillMaxSize().background(Color.Black.copy(.28f)).clickable { pendingUri = null }, contentAlignment = Alignment.Center) {
            CuteCard(Modifier.width(430.dp).height(330.dp).clickable(enabled = false) {}, corner = 24.dp, padding = 22.dp) {
                Column {
                    Text("rafına kaydet ♡", fontFamily = FontFamily.Cursive, fontSize = 26.sp, color = cennetColors.forest)
                    Spacer(Modifier.height(12.dp)); UriImage(uri, Modifier.fillMaxWidth().height(140.dp)) { Text("görsel") }; Spacer(Modifier.height(10.dp))
                    BasicTextField(note, { note = it }, Modifier.fillMaxWidth().background(cennetColors.sage.copy(.45f), RoundedCornerShape(10.dp)).padding(10.dp), textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = cennetColors.text), decorationBox = { inner -> if (note.isEmpty()) Text("kısa bir not...", fontSize = 10.sp, color = cennetColors.mutedText); inner() })
                    Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { referenceFolders.forEach { folder -> Box(Modifier.background(if (folder == selected) cennetColors.lightGreen else cennetColors.background, RoundedCornerShape(8.dp)).clickable { selected = folder }.padding(7.dp)) { Text(folder, fontSize = 8.sp) } } }
                    Spacer(Modifier.weight(1f)); SoftButton("referansı kaydet ♡") { prefs.edit().putString("ref_${System.currentTimeMillis()}", "$uri¦${note.replace("¦", "")}¦$selected").apply(); CenoWidgets.refreshAll(context); items = readItems(); pendingUri = null; note = "" }
                }
            }
        }
    }
}

@Composable
private fun ReferenceHeader(onAdd: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(92.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("referans rafı", fontFamily = FontFamily.Cursive, fontSize = 31.sp, color = cennetColors.forest)
            Text("sana çizme isteği veren şeyler için düzenli bir yuva ♡", fontSize = 10.sp, color = cennetColors.mutedText)
        }
        Mascot(Modifier.size(70.dp), mood = "happy")
        CuteCard(Modifier.width(205.dp).height(64.dp), background = cennetColors.cream.copy(.9f), corner = 13.dp, padding = 10.dp) {
            Tape(Modifier.width(48.dp).height(12.dp).align(Alignment.TopCenter).offset(y = (-15).dp))
            Text("ilham birikti,\nçizimlerine güç katsın ♡", Modifier.align(Alignment.Center), fontFamily = FontFamily.Cursive, fontSize = 14.sp, color = cennetColors.mutedText)
        }
        Spacer(Modifier.width(18.dp)); SoftButton("＋ görsel ekle", onClick = onAdd)
    }
}

@Composable
private fun FolderCard(name: String, glyph: String, active: Boolean, count: Int, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.clickable(onClick = onClick)) {
        Box(Modifier.fillMaxWidth(.42f).height(23.dp).background(if (active) cennetColors.midGreen else cennetColors.lightGreen, RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp)))
        Box(Modifier.fillMaxSize().padding(top = 13.dp).background(if (active) cennetColors.lightGreen else Color(0xFFF5F1DF), RoundedCornerShape(10.dp)).border(.8.dp, cennetColors.midGreen, RoundedCornerShape(10.dp)).padding(14.dp)) {
            Column(Modifier.align(Alignment.CenterStart)) { Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = cennetColors.text); Text("$count fikir", fontSize = 8.sp, color = cennetColors.mutedText) }
            Text(glyph, Modifier.align(Alignment.BottomEnd), fontSize = 30.sp, color = cennetColors.forest.copy(.68f))
            Text(listOf("♡", "◇", "●", "❀", "✦")[referenceFolders.indexOf(name)], Modifier.align(Alignment.TopEnd), fontSize = 17.sp, color = cennetColors.midGreen)
        }
    }
}

@Composable
private fun ReferenceThumbnail(item: RefItem?, starter: StarterRef?, index: Int, modifier: Modifier, onDelete: () -> Unit) {
    var favorite by remember { mutableStateOf(index % 3 == 1) }
    CuteCard(modifier, corner = 10.dp, padding = 8.dp) {
        Column {
            if (item != null) UriImage(item.uri, Modifier.fillMaxWidth().weight(1f)) { ReferenceArt(index) }
            else Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(8.dp)).background(cennetColors.sage.copy(.42f))) { ReferenceArt(starter?.art ?: index) }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item?.note?.ifBlank { "isimsiz ilham" } ?: starter?.title.orEmpty(), fontSize = 8.sp, maxLines = 1, color = cennetColors.mutedText, modifier = Modifier.weight(1f))
                if (item != null) Text("×", Modifier.clickable(onClick = onDelete), color = cennetColors.mutedText)
                else Text(if (favorite) "★" else "☆", Modifier.clickable { favorite = !favorite }, color = cennetColors.midGreen, fontSize = 13.sp)
            }
        }
        if (index % 4 == 0) Tape(Modifier.width(42.dp).height(9.dp).align(Alignment.TopCenter).offset(y = (-11).dp), cennetColors.midGreen.copy(.65f))
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
