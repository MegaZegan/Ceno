package com.cennet.app.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cennet.app.data.database.DiaryEntry
import com.cennet.app.data.repository.CennetRepository
import com.cennet.app.R
import com.cennet.app.ui.components.*
import com.cennet.app.ui.theme.cennetColors
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DiaryScreen(repository: CennetRepository) {
    val entries by repository.entries.collectAsState(initial = emptyList())
    var selected by remember(entries) { mutableStateOf(entries.firstOrNull()) }
    var editing by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DarkSectionHeader("GÜNLÜK", Modifier.fillMaxWidth())
        CuteCard(Modifier.fillMaxWidth().weight(1f), corner = 18.dp, padding = 0.dp) {
            DoodleSparkles(Modifier.fillMaxSize())
            BoxWithConstraints(Modifier.fillMaxSize().padding(20.dp)) {
                val wide = maxWidth > 620.dp
                if (wide) {
                    Row(Modifier.fillMaxSize()) {
                        DiaryTimeline(entries, selected, { selected = it }, { editing = true }, Modifier.width(165.dp).fillMaxHeight())
                        Spacer(Modifier.width(18.dp))
                        if (selected == null) DiaryEmptyPage(Modifier.weight(1f)) { editing = true }
                        else DiaryPage(repository, selected!!, Modifier.weight(1f), onEdit = { editing = true }, onDelete = { selected = null })
                    }
                } else {
                    Column {
                        DiaryTimeline(entries, selected, { selected = it }, { editing = true }, Modifier.fillMaxWidth().height(100.dp))
                        Spacer(Modifier.height(12.dp))
                        if (selected == null) DiaryEmptyPage(Modifier.fillMaxWidth().weight(1f)) { editing = true }
                        else DiaryPage(repository, selected!!, Modifier.fillMaxWidth().weight(1f), onEdit={editing=true}, onDelete={selected=null})
                    }
                }
            }
        }
    }
    if (editing) DiaryEditor(selected, onDismiss = { editing = false }) { editing = false; selected = it }
}

@Composable
private fun DiaryEmptyPage(modifier: Modifier, onAdd: () -> Unit) {
    Box(modifier.background(Color(0xFFFEFAED), RoundedCornerShape(8.dp)).border(.7.dp, cennetColors.border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
        Tape(Modifier.width(54.dp).height(16.dp).align(Alignment.TopCenter).offset(y=(-8).dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("henüz yazılmış bir sayfa yok ♡", fontFamily = FontFamily.Cursive, fontSize = 26.sp, color = cennetColors.forest)
            Spacer(Modifier.height(8.dp))
            Text("ilk anını kendi fotoğrafın ve sözlerinle sakla", fontSize = 10.sp, color = cennetColors.mutedText)
            Spacer(Modifier.height(18.dp))
            SoftButton("＋ ilk sayfamı yaz", onClick = onAdd)
        }
    }
}

@Composable
private fun DiaryTimeline(entries: List<DiaryEntry>, selected: DiaryEntry?, onSelect: (DiaryEntry) -> Unit, onAdd: () -> Unit, modifier: Modifier) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("minik anılar", fontFamily = FontFamily.Cursive, fontSize = 20.sp, color = cennetColors.forest)
            SoftButton("＋ yeni", onClick = onAdd)
        }
        Spacer(Modifier.height(10.dp))
        if(entries.isEmpty()) Text("sayfaların burada birikecek ♡",fontSize=10.sp,color=cennetColors.mutedText)
        entries.forEach { entry ->
            val active = entry.id == selected?.id
            Box(Modifier.fillMaxWidth().padding(vertical=3.dp).background(if(active)cennetColors.lightGreen else Color.Transparent,RoundedCornerShape(11.dp)).clickable{onSelect(entry)}.padding(9.dp)) {
                Column { Text(formatDiaryDate(entry.date),fontSize=11.sp,fontWeight=FontWeight.SemiBold,color=cennetColors.darkForest); Text(entry.text.replace("\n"," "),maxLines=1,fontSize=9.sp,color=cennetColors.mutedText) }
            }
        }
    }
}

@Composable
private fun DiaryPage(repository: CennetRepository, entry: DiaryEntry, modifier: Modifier, onEdit: () -> Unit, onDelete: (DiaryEntry) -> Unit) {
    val scope = rememberCoroutineScope()
    Box(modifier.background(Color(0xFFFEFAED),RoundedCornerShape(8.dp)).border(.7.dp,cennetColors.border,RoundedCornerShape(8.dp)).padding(18.dp)) {
        Tape(Modifier.width(54.dp).height(16.dp).align(Alignment.TopCenter).offset(y=(-26).dp))
        Row(Modifier.fillMaxSize(),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.width(80.dp).align(Alignment.Top)) {
                Text(runCatching{LocalDate.parse(entry.date).format(DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("tr-TR")))}.getOrDefault("May"),fontSize=11.sp,color=cennetColors.mutedText)
                Text(runCatching{LocalDate.parse(entry.date).dayOfMonth.toString()}.getOrDefault("25"),fontSize=27.sp,color=cennetColors.midGreen)
                Text(runCatching{LocalDate.parse(entry.date).format(DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("tr-TR")))}.getOrDefault("Cmt"),fontSize=9.sp)
            }
            Box(Modifier.weight(.9f).padding(8.dp).rotate(-2f).background(Color.White).padding(9.dp)) {
                UriImage(entry.photoUri, Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(R.drawable.diary_default_memory),
                        contentDescription = "TXT temalı günlük hatırası",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(Modifier.width(20.dp))
            Box(Modifier.weight(1.25f).fillMaxHeight(.84f).background(cennetColors.cream,RoundedCornerShape(20.dp)).border(.6.dp,cennetColors.border,RoundedCornerShape(20.dp)).padding(20.dp)) {
                Text(entry.text,fontSize=12.sp,lineHeight=22.sp,color=cennetColors.text)
                Mascot(Modifier.size(78.dp).align(Alignment.BottomEnd).offset(15.dp,18.dp))
            }
        }
        Row(Modifier.align(Alignment.TopEnd),horizontalArrangement=Arrangement.spacedBy(8.dp)) { Text("✎",Modifier.clickable(onClick=onEdit),fontSize=18.sp,color=cennetColors.forest); if(entry.id!=0L) Text("×",Modifier.clickable { scope.launch { repository.deleteDiary(entry); onDelete(entry) } },fontSize=19.sp,color=cennetColors.mutedText) }
    }
}

@Composable
private fun DiaryEditor(existing: DiaryEntry?, onDismiss: () -> Unit, onSave: (DiaryEntry) -> Unit) {
    val context=LocalContext.current
    val repo = remember { CennetRepository(context.applicationContext) }
    var date by remember { mutableStateOf(existing?.date ?: LocalDate.now().toString()) }
    var text by remember { mutableStateOf(existing?.text ?: "") }
    var photo by remember { mutableStateOf(existing?.photoUri) }
    val scope=rememberCoroutineScope()
    val photoPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri-> uri?.let{runCatching{context.contentResolver.takePersistableUriPermission(it,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)};photo=it.toString()} }
    val chooseDate = { val d=runCatching{LocalDate.parse(date)}.getOrDefault(LocalDate.now()); DatePickerDialog(context,{_,y,m,day->date=LocalDate.of(y,m+1,day).toString()},d.year,d.monthValue-1,d.dayOfMonth).show() }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(.3f)).clickable(onClick=onDismiss),contentAlignment=Alignment.Center) {
        CuteCard(Modifier.width(560.dp).height(430.dp).clickable(enabled=false){},corner=25.dp,padding=24.dp) {
            Column(Modifier.fillMaxSize()) {
                Text(if(existing==null)"yeni bir günlük sayfası ♡" else "bu anıyı düzenle ♡",fontFamily=FontFamily.Cursive,fontSize=27.sp,color=cennetColors.forest)
                Spacer(Modifier.height(14.dp))
                Row(Modifier.weight(1f)) {
                    Column(Modifier.width(170.dp)) { UriImage(photo,Modifier.fillMaxWidth().height(190.dp)){Text("fotoğraf seç\n♡",textAlign=TextAlign.Center,color=cennetColors.mutedText)}; Spacer(Modifier.height(8.dp)); SoftButton("fotoğraf seç"){photoPicker.launch(arrayOf("image/*"))}; Spacer(Modifier.height(8.dp)); SoftButton("tarih: $date",onClick=chooseDate) }
                    Spacer(Modifier.width(18.dp))
                    BasicTextField(text,{text=it},Modifier.weight(1f).fillMaxHeight().background(cennetColors.sage.copy(.38f),RoundedCornerShape(14.dp)).padding(16.dp),textStyle=androidx.compose.ui.text.TextStyle(fontSize=13.sp,lineHeight=21.sp,color=cennetColors.text))
                }
                Spacer(Modifier.height(14.dp)); Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){SoftButton("vazgeç",onClick=onDismiss);Spacer(Modifier.width(9.dp));SoftButton("sayfayı kaydet ♡"){ if(text.isNotBlank()) scope.launch{val result=(existing?:DiaryEntry(date=date,text=text)).copy(date=date,text=text,photoUri=photo,updatedAt=System.currentTimeMillis());val id=repo.saveDiary(result);onSave(result.copy(id=if(result.id==0L)id else result.id))} }}
            }
        }
    }
}

private fun formatDiaryDate(value:String):String = runCatching{LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("tr-TR")))}.getOrDefault(value)
