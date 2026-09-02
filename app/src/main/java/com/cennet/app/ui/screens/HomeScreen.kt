package com.cennet.app.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.cennet.app.data.repository.CennetRepository
import com.cennet.app.model.*
import com.cennet.app.ui.components.*
import com.cennet.app.ui.theme.cennetColors
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

@Composable
fun HomeScreen(
    repository: CennetRepository,
    birthdayMode: Boolean,
    navigate: (CennetScreen) -> Unit
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(); delay(30_000) } }
    var showBirthday by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp)) {
        HomeHeader(now)
        if (birthdayMode) {
            Spacer(Modifier.height(10.dp))
            BirthdayBanner { showBirthday = true }
        }
        Spacer(Modifier.height(12.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val roomy = maxWidth >= 740.dp
            if (roomy) LandscapeHomeGrid(repository) else CompactHomeGrid(repository)
        }
        Spacer(Modifier.height(14.dp))
        Text("✦   en sevdiğim insansın ♡   ·   hayatımda olduğun için teşekkür ederim   ✦", Modifier.fillMaxWidth(), color = cennetColors.mutedText, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
    AnimatedVisibility(showBirthday, enter = fadeIn(), exit = fadeOut()) {
        BirthdayPackage(onClose = { showBirthday = false }, onLetter = { showBirthday = false; navigate(CennetScreen.LETTERS) })
    }
}

@Composable
private fun HomeHeader(now: LocalDateTime) {
    val greeting = when (now.hour) {
        in 5..11 -> "Günaydın! ♡"
        in 12..17 -> "Güzel bir gün! ♡"
        in 18..22 -> "İyi akşamlar! ♡"
        else -> "Tatlı rüyalar! ♡"
    }
    Row(Modifier.fillMaxWidth().height(78.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Ceno ♡", color = cennetColors.forest, style = androidx.compose.material3.MaterialTheme.typography.displayLarge)
            Text("yalnızca sana ait minik bir dünya ♡", color = cennetColors.mutedText, fontSize = 11.sp)
        }
        Box(Modifier.width(250.dp).fillMaxHeight()) {
            DoodleSparkles(Modifier.fillMaxSize())
            Mascot(Modifier.size(63.dp).align(Alignment.BottomCenter).offset(x = (-38).dp), bounce = true)
            Box(Modifier.align(Alignment.Center).offset(x = 46.dp, y = (-8).dp).shadow(2.dp, RoundedCornerShape(50)).background(cennetColors.cream, RoundedCornerShape(50)).padding(horizontal = 17.dp, vertical = 9.dp)) {
                Text(greeting, fontSize = 11.sp, color = cennetColors.mutedText)
            }
        }
        Column(Modifier.width(100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(now.format(DateTimeFormatter.ofPattern("HH:mm")), fontSize = 20.sp, color = cennetColors.darkForest)
            Text(now.format(DateTimeFormatter.ofPattern("d MMM, EEE", Locale.forLanguageTag("tr-TR"))), fontSize = 10.sp, color = cennetColors.mutedText)
        }
    }
}

@Composable
private fun LandscapeHomeGrid(repository: CennetRepository) {
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Row(Modifier.fillMaxWidth().height(194.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            MoodCard(Modifier.weight(.8f).fillMaxHeight())
            ReminderCard(Modifier.weight(1.25f).fillMaxHeight())
        }
        Row(Modifier.fillMaxWidth().height(174.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            PaletteCard(Modifier.weight(1.0f).fillMaxHeight())
            InspirationCard(Modifier.weight(1.22f).fillMaxHeight())
            LittleNoteCard(Modifier.weight(.82f).fillMaxHeight())
        }
        Row(Modifier.fillMaxWidth().height(158.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            PhotoCard(repository, Modifier.weight(1.16f).fillMaxHeight())
            InstalledDaysCard(Modifier.weight(1f).fillMaxHeight())
            WeekCard(Modifier.weight(1.1f).fillMaxHeight())
        }
    }
}

@Composable
private fun CompactHomeGrid(repository: CennetRepository) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth().height(190.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { MoodCard(Modifier.weight(1f)); ReminderCard(Modifier.weight(1.2f)) }
        Row(Modifier.fillMaxWidth().height(170.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { PaletteCard(Modifier.weight(1f)); LittleNoteCard(Modifier.weight(1f)) }
        InspirationCard(Modifier.fillMaxWidth().height(170.dp))
        PhotoCard(repository, Modifier.fillMaxWidth().height(170.dp))
        Row(Modifier.fillMaxWidth().height(155.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { InstalledDaysCard(Modifier.weight(1f)); WeekCard(Modifier.weight(1.1f)) }
    }
}

@Composable
private fun MoodCard(modifier: Modifier) {
    var moodIndex by remember { mutableIntStateOf(1) }
    val mood = Mood.entries[moodIndex]
    val colors = cennetColors
    CuteCard(modifier, padding = 15.dp, onClick = { moodIndex = (moodIndex + 1) % Mood.entries.size }) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            SectionTitle("bugünkü ruh halim", Modifier.align(Alignment.Start))
            Box(Modifier.size(78.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val cloud = Path().apply {
                        moveTo(size.width*.18f,size.height*.68f); cubicTo(size.width*.03f,size.height*.58f,size.width*.12f,size.height*.38f,size.width*.31f,size.height*.40f)
                        cubicTo(size.width*.36f,size.height*.15f,size.width*.69f,size.height*.15f,size.width*.72f,size.height*.42f)
                        cubicTo(size.width*.96f,size.height*.40f,size.width*.98f,size.height*.72f,size.width*.77f,size.height*.75f)
                        lineTo(size.width*.25f,size.height*.75f); close()
                    }
                    drawPath(cloud, Color.White); drawPath(cloud, colors.lightGreen, style=androidx.compose.ui.graphics.drawscope.Stroke(2f))
                    drawCircle(colors.text, 2.2f, Offset(size.width*.43f,size.height*.57f)); drawCircle(colors.text,2.2f,Offset(size.width*.59f,size.height*.57f))
                    drawCircle(colors.pink,4f,Offset(size.width*.35f,size.height*.63f)); drawCircle(colors.pink,4f,Offset(size.width*.67f,size.height*.63f))
                }
                Text(mood.face, fontSize = 7.sp, modifier = Modifier.offset(y = 10.dp), color = cennetColors.text)
            }
            Text(mood.label + "   ⌄", color = cennetColors.darkForest, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ReminderCard(modifier: Modifier) {
    var items by remember { mutableStateOf(listOf(
        ReminderItem(1,"su iç",true), ReminderItem(2,"çizimini bitir"), ReminderItem(3,"TXT dinle"), ReminderItem(4,"kendine nazik davran")
    )) }
    var editing by remember { mutableStateOf(false) }
    PaperNote(modifier) {
        SectionTitle("hatırlatıcı")
        Spacer(Modifier.height(8.dp))
        items.forEachIndexed { index, item ->
            Row(Modifier.fillMaxWidth().clickable { items = items.toMutableList().also { it[index] = item.copy(done = !item.done) } }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(15.dp).border(1.dp, cennetColors.midGreen, RoundedCornerShape(3.dp)).background(if (item.done) cennetColors.lightGreen else Color.Transparent, RoundedCornerShape(3.dp)), contentAlignment = Alignment.Center) { if(item.done) Text("✓", fontSize = 10.sp, color = cennetColors.forest) }
                Spacer(Modifier.width(9.dp)); Text(item.text, fontSize = 10.sp, color = cennetColors.text, maxLines = 1)
            }
        }
        Text("＋ listeyi düzenle", Modifier.clickable { editing = true }.padding(top = 4.dp), fontSize = 9.sp, color = cennetColors.midGreen)
    }
    if (editing) TextEditor("hatırlatıcılar (her satıra bir tane)", items.joinToString("\n") { it.text }, { editing = false }, multiline = true) { value ->
        items = value.lines().filter { it.isNotBlank() }.take(6).mapIndexed { i, s -> ReminderItem(i, s.trim()) }; editing = false
    }
}

@Composable
private fun PaletteCard(modifier: Modifier) {
    val palettes = listOf(
        listOf(0xFF355E3B,0xFF527847,0xFF79A268,0xFFA6CF98,0xFFDDE8D2),
        listOf(0xFF6F897A,0xFF9BB6A6,0xFFC8D9CC,0xFFF1D8C8,0xFFEFD5D1),
        listOf(0xFF526B4A,0xFF8FAD79,0xFFC6DBB7,0xFFF0D7AE,0xFFDBA6A1)
    )
    var palette by remember { mutableIntStateOf(0) }; var selected by remember { mutableIntStateOf(3) }
    CuteCard(modifier, padding = 15.dp) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { SectionTitle("bugünün paleti"); Text("↻", Modifier.clickable { palette = (palette+1)%palettes.size }, color = cennetColors.midGreen) }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                palettes[palette].forEachIndexed { i, value -> HeartSwatch(Color(value), i == selected) { selected = i } }
            }
            Spacer(Modifier.weight(1f))
            Text("#${palettes[palette][selected].toString(16).takeLast(6).uppercase()}   ▢", color = cennetColors.mutedText, fontSize = 11.sp, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
private fun HeartSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val outline = cennetColors.forest
    Canvas(Modifier.size(if(selected) 37.dp else 33.dp).clickable(onClick = onClick)) {
        val p=Path().apply { moveTo(size.width*.5f,size.height*.88f); cubicTo(size.width*.40f,size.height*.76f,size.width*.10f,size.height*.56f,size.width*.12f,size.height*.31f); cubicTo(size.width*.13f,size.height*.08f,size.width*.42f,size.height*.05f,size.width*.5f,size.height*.25f); cubicTo(size.width*.58f,size.height*.05f,size.width*.87f,size.height*.08f,size.width*.88f,size.height*.31f); cubicTo(size.width*.90f,size.height*.56f,size.width*.60f,size.height*.76f,size.width*.5f,size.height*.88f); close() }
        drawPath(p,color); if(selected) drawPath(p,outline,style=androidx.compose.ui.graphics.drawscope.Stroke(2f))
    }
}

@Composable
private fun InspirationCard(modifier: Modifier) {
    var prompt by remember { mutableIntStateOf(Random.nextInt(drawingPrompts.size)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(8_000)
            var next = Random.nextInt(drawingPrompts.size)
            while (next == prompt && drawingPrompts.size > 1) next = Random.nextInt(drawingPrompts.size)
            prompt = next
        }
    }
    CuteCard(modifier, padding = 15.dp) {
        Column(Modifier.fillMaxSize()) {
            SectionTitle("çizim ilhamı")
            Spacer(Modifier.height(11.dp))
            Row(Modifier.weight(1f)) {
                Text(drawingPrompts[prompt], Modifier.weight(.64f), fontSize = 11.sp, lineHeight = 18.sp, color = cennetColors.mutedText)
                MiniLandscape(Modifier.weight(.58f).fillMaxHeight())
            }
            SoftButton("başka bir fikir  ↻") { prompt = drawingPrompts.indices.filter { it != prompt }.random() }
        }
    }
}

@Composable
private fun MiniLandscape(modifier: Modifier) {
    val colors = cennetColors
    Canvas(modifier) {
        drawOval(colors.sage, Offset(0f,size.height*.66f), Size(size.width,size.height*.32f))
        drawCircle(Color.White, size.width*.09f, Offset(size.width*.65f,size.height*.25f))
        drawLine(colors.forest,Offset(size.width*.45f,size.height*.75f),Offset(size.width*.45f,size.height*.35f),4f)
        drawCircle(colors.midGreen,size.width*.13f,Offset(size.width*.45f,size.height*.37f))
        drawLine(colors.forest,Offset(size.width*.78f,size.height*.78f),Offset(size.width*.78f,size.height*.48f),3f)
        drawCircle(colors.lightGreen,size.width*.10f,Offset(size.width*.78f,size.height*.48f))
    }
}

@Composable
private fun LittleNoteCard(modifier: Modifier) {
    var note by remember { mutableIntStateOf(0) }
    CuteCard(modifier, padding = 15.dp, onClick = { note = (note + 1) % littleNotes.size }) {
        SectionTitle("minik not")
        Text(littleNotes[note], Modifier.align(Alignment.Center).padding(bottom = 15.dp), fontSize = 10.sp, lineHeight = 17.sp, color = cennetColors.mutedText, textAlign = TextAlign.Center)
        Mascot(Modifier.size(58.dp).align(Alignment.BottomEnd).offset(x = 9.dp, y = 11.dp), mood = "happy")
        Text("✧", Modifier.align(Alignment.TopEnd), color = cennetColors.lightGreen)
    }
}

@Composable
private fun PhotoCard(repository: CennetRepository, modifier: Modifier) {
    var pool by remember { mutableStateOf(repository.photoPool) }
    var uri by remember { mutableStateOf(pool.randomOrNull() ?: repository.photoOfDay) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { picked ->
        if (picked.isNotEmpty()) {
            picked.forEach { runCatching { context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } }
            pool = (pool + picked.map { it.toString() }).distinct()
            repository.photoPool = pool
            uri = pool.randomOrNull()
            repository.photoOfDay = uri
        }
    }
    CuteCard(modifier, padding = 0.dp, onClick = { if(pool.isEmpty()) launcher.launch(arrayOf("image/*")) else uri = pool.randomOrNull() }) {
        Text("günün fotoğrafı", Modifier.padding(15.dp).align(Alignment.TopStart).zIndex(2f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = cennetColors.darkForest)
        UriImage(uri, Modifier.fillMaxSize().padding(top = 39.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("❀", fontSize = 34.sp, color = cennetColors.midGreen); Text("galeriden birkaç anı seç", fontSize = 9.sp, color = cennetColors.mutedText) }
        }
        Row(Modifier.align(Alignment.TopEnd).padding(9.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if(pool.isNotEmpty()) Text("↻", Modifier.clickable { uri = pool.randomOrNull() }, color = cennetColors.midGreen)
            Text("＋", Modifier.clickable { launcher.launch(arrayOf("image/*")) }, color = cennetColors.midGreen)
        }
    }
}

@Composable
private fun InstalledDaysCard(modifier: Modifier) {
    val context = LocalContext.current
    val days = remember(context) {
        runCatching {
            val installedAt = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
            val installedDate = Instant.ofEpochMilli(installedAt).atZone(ZoneId.systemDefault()).toLocalDate()
            java.time.temporal.ChronoUnit.DAYS.between(installedDate, LocalDate.now()).coerceAtLeast(0) + 1
        }.getOrDefault(1)
    }
    CuteCard(modifier, padding = 15.dp) {
        SectionTitle("birlikte geçen zaman")
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Ceno seninle", fontSize = 11.sp)
            Row(verticalAlignment = Alignment.Bottom) { Text(days.toString(), color = cennetColors.midGreen, fontSize = 43.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive); Spacer(Modifier.width(8.dp)); Text("gündür burada ♡", fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp)) }
        }
        Text("♡   ✦   ♡", Modifier.align(Alignment.BottomStart), color = cennetColors.peach)
        Text("🌱", Modifier.align(Alignment.CenterEnd), fontSize = 29.sp)
    }
}

@Composable
private fun WeekCard(modifier: Modifier) {
    val values = listOf(.35f,.22f,.27f,.55f,.31f,.75f,.42f)
    CuteCard(modifier, padding = 15.dp) {
        SectionTitle("bu hafta")
        Text("çizim süresi", Modifier.padding(top = 31.dp), fontSize = 9.sp)
        Text("12sa 45dk", Modifier.padding(top = 46.dp), fontSize = 17.sp, color = cennetColors.darkForest)
        Row(Modifier.fillMaxWidth().height(60.dp).align(Alignment.BottomCenter), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.Bottom) {
            val gunler = listOf("P","S","Ç","P","C","C","P")
            values.forEachIndexed { i, v -> Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.width(10.dp).height((45*v).dp).background(if(i%3==0)cennetColors.midGreen else cennetColors.lightGreen,RoundedCornerShape(5.dp))); Text(gunler[i],fontSize=7.sp) } }
        }
    }
}

@Composable
private fun BirthdayBanner(onClick: () -> Unit) {
    CuteCard(Modifier.fillMaxWidth().height(56.dp), background = cennetColors.lightGreen.copy(.7f), corner = 16.dp, padding = 10.dp, onClick = onClick) {
        Text("🎁", fontSize = 25.sp, modifier = Modifier.align(Alignment.CenterStart)); Text("Sana bir paket geldi ♡", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = cennetColors.darkForest, modifier = Modifier.align(Alignment.Center).padding(start=25.dp)); Text("aç  →", fontSize = 10.sp, modifier=Modifier.align(Alignment.CenterEnd),color=cennetColors.forest)
    }
}

@Composable
private fun BirthdayPackage(onClose: () -> Unit, onLetter: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(.28f)).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
        CuteCard(Modifier.width(480.dp).height(390.dp).clickable(enabled=false){}, background=cennetColors.cream, corner=26.dp, padding=26.dp) {
            Confetti(Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally) {
                Text("sana özel minik bir doğum günü bahçesi ♡",style=androidx.compose.material3.MaterialTheme.typography.headlineLarge,color=cennetColors.forest)
                Mascot(Modifier.size(120.dp),mood="happy",bounce=true)
                Text("iyi ki doğdun! bu yılın yumuşacık, aydınlık ve minik mucizelerle dolu olsun.",textAlign=TextAlign.Center,fontSize=13.sp,lineHeight=20.sp,color=cennetColors.mutedText)
                Spacer(Modifier.height(18.dp)); SoftButton("doğum günü mektubunu aç  ✉",onClick=onLetter)
            }
            Text("×",Modifier.align(Alignment.TopEnd).clickable(onClick=onClose),fontSize=22.sp)
        }
    }
}

@Composable
private fun Confetti(modifier: Modifier) {
    val t=rememberInfiniteTransition(label="confetti"); val phase by t.animateFloat(0f,1f,infiniteRepeatable(tween(2400),RepeatMode.Restart),label="fall")
    val colors=listOf(cennetColors.midGreen,cennetColors.peach,cennetColors.pink,cennetColors.lightGreen)
    Canvas(modifier) { repeat(24){ i -> val x=((i*83)%100)/100f*size.width; val y=((i*37)/100f*size.height+phase*size.height)%size.height; drawCircle(colors[i%colors.size],3f+(i%3),Offset(x,y)) } }
}

@Composable
private fun TextEditor(title: String, initial: String, onDismiss: () -> Unit, multiline: Boolean = false, onSave: (String) -> Unit) {
    var value by remember { mutableStateOf(initial) }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(.3f)).clickable(onClick=onDismiss).zIndex(20f),contentAlignment=Alignment.Center) {
        CuteCard(Modifier.width(390.dp).height(if(multiline)300.dp else 190.dp).clickable(enabled=false){},corner=24.dp,padding=22.dp) {
            Column { SectionTitle(title); Spacer(Modifier.height(18.dp)); BasicTextField(value,{value=it},Modifier.fillMaxWidth().weight(1f).background(cennetColors.sage.copy(.45f),RoundedCornerShape(12.dp)).padding(12.dp),textStyle=androidx.compose.ui.text.TextStyle(fontSize=12.sp,color=cennetColors.text)); Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){ SoftButton("vazgeç",onClick=onDismiss); Spacer(Modifier.width(8.dp)); SoftButton("kaydet ♡"){onSave(value)} } }
        }
    }
}
