package com.cennet.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.cennet.app.R
import com.cennet.app.model.Letter
import com.cennet.app.ui.components.*
import com.cennet.app.ui.theme.cennetColors

@Composable
fun LettersScreen(birthdayMode: Boolean) {
    val letters = remember { listOf(
        Letter("üzgün\nhissettiğinde aç", "bırak ağır hislerin biraz yanında otursun. hepsinin bir anda kaybolması gerekmiyor. bugünü atlattığın için seninle gurur duyuyorum ♡", 0xFFE7EBD5),
        Letter("motivasyona\nihtiyacın olunca aç", "düne göre daha yakınsın. önce minik bir çizgi, sonra bir tane daha... güzel fikrin şimdiden gerçeğe dönüşüyor ♡", 0xFFDDE8C9),
        Letter("uyuyamadığında\naç", "yavaşça nefes al. omuzlarını gevşet. düşüncelerin sessizleşirken yıldızlar sana göz kulak olabilir ☾", 0xFFF2E6DE),
        Letter("beni özlediğinde\naç", "beni sevdiğin bir şarkıda, yumuşak yeşil bir yaprakta ve seni gülümseten her küçük şeyde ara ♡", 0xFFF0DCD6),
        Letter("doğum gününde\naç ♡", "bugün yalnızca yeni bir yaş değil, seninle güzelleşen yepyeni bir bölüm başlıyor. çizimlerin, sevdiğin şarkılar ve o güzel kalbin hep yanında olsun. en sevdiğin insanlarla kahkaha dolu, yumuşacık ve unutulmaz bir yıl diliyorum. iyi ki varsın, iyi ki doğdun ♡", 0xFF91AF73, true)
    ) }
    var opened by remember { mutableStateOf<Letter?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DarkSectionHeader("MEKTUPLAR",Modifier.fillMaxWidth())
        CuteCard(Modifier.fillMaxWidth().weight(1f),corner=18.dp,padding=18.dp) {
            DoodleSparkles(Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize()) {
                Text("ihtiyaç duyduğun günler için mektuplar",fontFamily=FontFamily.Cursive,fontSize=26.sp,color=cennetColors.forest)
                Text("kalbin ne zaman isterse bir tanesini aç ♡",fontSize=10.sp,color=cennetColors.mutedText)
                Spacer(Modifier.height(22.dp))
                Row(Modifier.fillMaxWidth().weight(1f),horizontalArrangement=Arrangement.spacedBy(17.dp),verticalAlignment=Alignment.CenterVertically) {
                    letters.forEach { letter -> LetterEnvelope(letter, letter.locked && !birthdayMode, Modifier.weight(1f).aspectRatio(.72f)) { opened = letter } }
                }
            }
        }
    }
    opened?.let { letter -> LetterReader(letter, letter.locked && !birthdayMode) { opened=null } }
}

@Composable
private fun LetterEnvelope(letter: Letter, locked: Boolean, modifier: Modifier, onClick:()->Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if(pressed).97f else 1f,tween(120),label="letter press")
    val colors = cennetColors
    Box(modifier.scale(scale).shadow(4.dp,RoundedCornerShape(5.dp)).background(Color(letter.tint),RoundedCornerShape(5.dp)).border(.7.dp,colors.border,RoundedCornerShape(5.dp)).clickable{pressed=true;onClick()},contentAlignment=Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val p=Path().apply{moveTo(0f,size.height*.55f);lineTo(size.width*.5f,size.height*.82f);lineTo(size.width,size.height*.55f);lineTo(size.width,size.height);lineTo(0f,size.height);close()};drawPath(p,Color.White.copy(.16f));drawPath(p,colors.forest.copy(.18f),style=Stroke(1.2f))
            drawLine(colors.forest.copy(.15f),Offset(0f,size.height*.55f),Offset(size.width*.5f,size.height*.82f),1f);drawLine(colors.forest.copy(.15f),Offset(size.width,size.height*.55f),Offset(size.width*.5f,size.height*.82f),1f)
        }
        Text(letter.title,color=if(letter.tint==0xFF91AF73)cennetColors.cream else cennetColors.text,fontSize=11.sp,lineHeight=18.sp,textAlign=TextAlign.Center,modifier=Modifier.padding(bottom=35.dp))
        Text(if(locked)"🔒" else listOf("☁","❀","☾","♡","✉")[letter.title.length%5],Modifier.align(Alignment.BottomCenter).padding(bottom=14.dp),fontSize=24.sp,color=cennetColors.forest)
    }
}

@Composable
private fun LetterReader(letter: Letter, locked: Boolean, close:()->Unit) {
    var open by remember { mutableStateOf(false) }; LaunchedEffect(Unit){open=true}
    val rotation by animateFloatAsState(if(open)0f else -8f,spring(dampingRatio=.72f),label="open letter")
    val birthdayLetter = letter.locked
    Box(Modifier.fillMaxSize().background(Color.Black.copy(.28f)).clickable(onClick=close),contentAlignment=Alignment.Center) {
        Box(Modifier.width(if(birthdayLetter && !locked)760.dp else 520.dp).height(if(birthdayLetter && !locked)540.dp else 390.dp).graphicsLayer{rotationX=rotation;cameraDistance=12f*density}.shadow(8.dp,RoundedCornerShape(8.dp)).background(Color(0xFFFFF9E9),RoundedCornerShape(8.dp)).clickable(enabled=false){}.padding(if(birthdayLetter && !locked)30.dp else 38.dp)) {
            Tape(Modifier.width(72.dp).height(18.dp).align(Alignment.TopCenter).offset(y=(-10).dp))
            DoodleSparkles(Modifier.fillMaxSize())
            if (birthdayLetter && !locked) {
                Row(Modifier.fillMaxSize().padding(top=8.dp),verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.width(272.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().weight(1f).shadow(3.dp,RoundedCornerShape(8.dp)).background(Color.White,RoundedCornerShape(8.dp)).padding(8.dp)) {
                            Image(painterResource(R.drawable.birthday_txt_memory),"TXT temalı doğum günü çizimi",Modifier.fillMaxSize().clip(RoundedCornerShape(5.dp)),contentScale=ContentScale.Crop)
                        }
                        Text("senin için saklanan tatlı bir anı ♡",Modifier.padding(top=10.dp),fontFamily=FontFamily.Cursive,fontSize=15.sp,color=cennetColors.midGreen)
                    }
                    Spacer(Modifier.width(28.dp))
                    Column(Modifier.weight(1f).fillMaxHeight().padding(vertical=18.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        Text("iyi ki doğdun ♡",fontFamily=FontFamily.Cursive,fontSize=32.sp,color=cennetColors.forest,textAlign=TextAlign.Center)
                        Text("♡  ✦  ♡",Modifier.padding(vertical=14.dp),fontSize=18.sp,color=cennetColors.pink)
                        Text(letter.body,fontSize=13.sp,lineHeight=22.sp,textAlign=TextAlign.Center,color=cennetColors.mutedText)
                        Spacer(Modifier.weight(1f))
                        Text("yeni yaşın en sevdiğin şarkı kadar güzel olsun ♡",fontFamily=FontFamily.Cursive,fontSize=18.sp,color=cennetColors.midGreen,textAlign=TextAlign.Center)
                        Text("tüm kalbimle, daima ♡",Modifier.padding(top=8.dp),fontSize=9.sp,color=cennetColors.mutedText)
                    }
                }
            } else {
                Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally) {
                    Text(letter.title.replace("\n"," "),fontFamily=FontFamily.Cursive,fontSize=29.sp,color=cennetColors.forest,textAlign=TextAlign.Center)
                    Spacer(Modifier.height(28.dp))
                    Text(if(locked)"Bu mektup henüz hazır değil ♡" else letter.body,fontSize=13.sp,lineHeight=23.sp,textAlign=TextAlign.Center,color=cennetColors.mutedText,modifier=Modifier.width(390.dp))
                    Spacer(Modifier.weight(1f)); Text(if(locked)"doğum gününde tekrar gel" else "tüm kalbimle, daima ♡",fontFamily=FontFamily.Cursive,fontSize=19.sp,color=cennetColors.midGreen)
                }
            }
            Text("×",Modifier.align(Alignment.TopEnd).clickable(onClick=close),fontSize=22.sp)
        }
    }
}
