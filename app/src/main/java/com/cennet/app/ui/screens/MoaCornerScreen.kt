package com.cennet.app.ui.screens

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.cennet.app.R
import com.cennet.app.ui.components.*
import com.cennet.app.ui.theme.cennetColors
import com.cennet.app.widget.CenoWidgets

private data class MoaMember(val name: String, val image: Int)
private val members = listOf(
    MoaMember("yeonjun", R.drawable.member_yeonjun), MoaMember("soobin", R.drawable.member_soobin),
    MoaMember("beomgyu", R.drawable.member_beomgyu), MoaMember("taehyun", R.drawable.member_taehyun),
    MoaMember("hueningkai", R.drawable.member_hueningkai)
)
private val eras = listOf("MAGIC", "STAR", "ETERNITY", "BLUE HOUR", "FIGHT OR ESCAPE", "THURSDAY'S CHILD", "TEMPTATION", "SANCTUARY")
private val eraImages = listOf(
    R.drawable.album_magic, R.drawable.album_star, R.drawable.album_eternity, R.drawable.album_blue_hour,
    R.drawable.album_fight_or_escape, R.drawable.album_thursday_child, R.drawable.album_temptation, R.drawable.album_sanctuary
)
private val songs = listOf("Deja Vu", "Sugar Rush Ride", "0X1=LOVESONG", "Blue Hour", "Chasing That Feeling", "CROWN", "LO\$ER=LO♡ER", "Run Away", "Good Boy Gone Bad", "Farewell, Neverland", "Our Summer", "Magic")
private val songImages = listOf(
    R.drawable.song_deja_vu, R.drawable.album_temptation, R.drawable.song_0x1_lovesong, R.drawable.album_blue_hour,
    R.drawable.song_chasing_that_feeling, R.drawable.song_crown, R.drawable.song_loser_lover, R.drawable.song_run_away,
    R.drawable.song_good_boy_gone_bad, R.drawable.song_farewell_neverland, R.drawable.song_our_summer, R.drawable.album_magic
)

@Composable fun MoaCornerScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("moa", Context.MODE_PRIVATE) }
    var tab by remember { mutableStateOf("favoriler") }
    var rating by remember { mutableIntStateOf(prefs.getInt("rating", 5).coerceIn(1, 5)) }
    var member by remember { mutableIntStateOf(prefs.getInt("member", 4).coerceIn(0, 4)) }
    var song by remember { mutableStateOf(prefs.getString("song", "Deja Vu")?.takeIf { it in songs } ?: "Deja Vu") }
    var order by remember { mutableStateOf(readOrder(prefs.getString("era_order", null))) }
    var owned by remember { mutableStateOf(readSet(prefs.getString("collection_owned", "6"))) }
    var note by remember { mutableStateOf("seçimlerin otomatik kaydedilir ♡") }
    fun rate(v: Int) { rating=v; prefs.edit().putInt("rating",v).apply(); CenoWidgets.refreshAll(context); note="puan kaydedildi: $v / 5 ♡" }
    fun chooseMember(v: Int) { member=v; prefs.edit().putInt("member",v).apply(); CenoWidgets.refreshAll(context); note="${members[v].name} favorin oldu ♡" }
    fun chooseSong(v: String) { song=v; prefs.edit().putString("song",v).apply(); CenoWidgets.refreshAll(context); note="favori şarkın kaydedildi ♡" }
    fun reorder(v: List<Int>) { order=v; prefs.edit().putString("era_order",v.joinToString(",")).apply(); CenoWidgets.refreshAll(context); note="dönem sıralaman kaydedildi ♡" }
    fun collect(v: Set<Int>) { owned=v; prefs.edit().putString("collection_owned",v.sorted().joinToString(",")).apply(); CenoWidgets.refreshAll(context); note="koleksiyonun kaydedildi ♡" }
    val tabs = listOf("☆" to "favoriler", "♫" to "TXT şarkıları", "♧" to "üyeler", "▣" to "dönemler", "▧" to "koleksiyon")
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DarkSectionHeader("MOA KÖŞESİ", Modifier.fillMaxWidth())
        CuteCard(Modifier.fillMaxWidth().weight(1f), corner=18.dp, padding=0.dp) {
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.width(178.dp).fillMaxHeight().background(cennetColors.sage.copy(.43f)).padding(17.dp)) {
                    Text("moa köşem ♡", fontFamily=FontFamily.Cursive, fontSize=26.sp, color=cennetColors.forest)
                    Text("her bölüm gerçekten senin ♡", fontSize=8.sp, color=cennetColors.mutedText)
                    Spacer(Modifier.height(16.dp))
                    tabs.forEach { (icon,label) ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if(label==tab)cennetColors.lightGreen else Color.Transparent).clickable{tab=label}.padding(horizontal=12.dp,vertical=11.dp), verticalAlignment=Alignment.CenterVertically) {
                            Text(icon,fontSize=14.sp,color=cennetColors.forest); Spacer(Modifier.width(10.dp)); Text(label,fontSize=10.sp,color=cennetColors.text)
                        }; Spacer(Modifier.height(4.dp))
                    }
                    Spacer(Modifier.weight(1f)); DoodleSparkles(Modifier.fillMaxWidth().height(48.dp)); Mascot(Modifier.size(105.dp).align(Alignment.CenterHorizontally), mood="happy")
                }
                Column(Modifier.weight(1f).padding(22.dp)) {
                    MoaHeader(tab); Spacer(Modifier.height(10.dp))
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when(tab) {
                            "favoriler" -> FavoritesTab(member,rating,song,order,::rate){tab=it}
                            "TXT şarkıları" -> SongsTab(song,::chooseSong)
                            "üyeler" -> MembersTab(member,::chooseMember)
                            "dönemler" -> ErasTab(order,::reorder)
                            else -> CollectionTab(owned,::collect)
                        }
                    }
                    Text("✦   $note   ✦",Modifier.fillMaxWidth().padding(top=7.dp),textAlign=TextAlign.Center,fontSize=8.sp,color=cennetColors.mutedText)
                }
            }
        }
    }
}

@Composable private fun MoaHeader(tab:String) {
    val sub=mapOf("favoriler" to "en sevdiklerin bir bakışta burada", "TXT şarkıları" to "kalbine en çok dokunan şarkıyı seç", "üyeler" to "favori üyenin kartına dokun", "dönemler" to "kartları sağa sola sürükleyerek sırala", "koleksiyon" to "sende olan dönemleri işaretle")
    Row(Modifier.fillMaxWidth().height(70.dp),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)){Text(tab,fontFamily=FontFamily.Cursive,fontSize=28.sp,color=cennetColors.forest);Text(sub[tab].orEmpty()+" ♡",fontSize=9.sp,color=cennetColors.mutedText)}
        Mascot(Modifier.size(62.dp),mood="happy")
        Box(Modifier.background(cennetColors.cream,RoundedCornerShape(50)).border(.7.dp,cennetColors.border,RoundedCornerShape(50)).padding(horizontal=14.dp,vertical=8.dp)){Text("moa love! ♡",fontSize=9.sp,color=cennetColors.mutedText)}
    }
}

@Composable private fun FavoritesTab(member:Int,rating:Int,song:String,order:List<Int>,onRating:(Int)->Unit,open:(String)->Unit) {
    Column(Modifier.fillMaxSize()) {
        CuteCard(Modifier.fillMaxWidth().weight(1.15f),background=cennetColors.background.copy(.88f),corner=17.dp,padding=14.dp) {
            Tape(Modifier.width(50.dp).height(12.dp).align(Alignment.TopEnd).offset(x=(-35).dp,y=(-8).dp)); Text("✦  favori üyen  ✦",Modifier.align(Alignment.TopCenter),fontSize=12.sp,color=cennetColors.darkForest)
            Row(Modifier.fillMaxSize().padding(top=27.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){members.indices.forEach{index->MemberCard(index,index==member,Modifier.weight(1f).fillMaxHeight()){open("üyeler")}}}
        }
        Spacer(Modifier.height(11.dp));Row(Modifier.fillMaxWidth().height(115.dp),horizontalArrangement=Arrangement.spacedBy(11.dp)){RatingCard(rating,Modifier.weight(1f),onRating);FavoriteSongCard(song,Modifier.weight(1f)){open("TXT şarkıları")}}
        Spacer(Modifier.height(11.dp));CuteCard(Modifier.fillMaxWidth().weight(.85f),corner=16.dp,padding=13.dp,onClick={open("dönemler")}){
            Text("favori dönem sıram  ♡",fontFamily=FontFamily.Cursive,fontSize=19.sp,color=cennetColors.forest);Text("sıralamak için aç  →",Modifier.align(Alignment.TopEnd),fontSize=8.sp,color=cennetColors.mutedText)
            Row(Modifier.fillMaxSize().padding(top=28.dp),horizontalArrangement=Arrangement.spacedBy(9.dp)){order.forEachIndexed{position,id->EraCard(id,eras[id],position==0,Modifier.weight(1f).fillMaxHeight()){open("dönemler")}}}
        }
    }
}

@Composable private fun SongsTab(selected:String,onSong:(String)->Unit) {
    Column(Modifier.fillMaxSize()) {
        Text("favori şarkını seç",fontFamily=FontFamily.Cursive,fontSize=22.sp,color=cennetColors.forest)
        Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){songs.take(6).forEachIndexed{i,n->SongRow(n,i,n==selected){onSong(n)}}}
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){songs.drop(6).forEachIndexed{i,n->SongRow(n,i+6,n==selected){onSong(n)}}}
        }
    }
}
@Composable private fun SongRow(name:String,index:Int,selected:Boolean,onClick:()->Unit){
    CuteCard(Modifier.fillMaxWidth().height(66.dp),background=if(selected)cennetColors.lightGreen.copy(.72f) else cennetColors.cream,corner=14.dp,padding=9.dp,onClick=onClick){
        Image(painterResource(songImages[index]),name,Modifier.size(47.dp).clip(RoundedCornerShape(10.dp)),contentScale=ContentScale.Crop)
        Column(Modifier.align(Alignment.CenterStart).padding(start=59.dp)){Text(name,fontSize=11.sp,color=cennetColors.darkForest,fontWeight=FontWeight.Medium);Text("TOMORROW X TOGETHER",fontSize=7.sp,color=cennetColors.mutedText)}
        Text(if(selected)"♥" else "♡",Modifier.align(Alignment.CenterEnd).padding(end=8.dp),fontSize=19.sp,color=cennetColors.forest)
    }
}

@Composable private fun MembersTab(selected:Int,onMember:(Int)->Unit){
    Column(Modifier.fillMaxSize()){Text("bir karta dokun; seçimin anında kaydolur ♡",fontSize=9.sp,color=cennetColors.mutedText);Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth().weight(1f),horizontalArrangement=Arrangement.spacedBy(13.dp)){members.indices.forEach{i->MemberCard(i,i==selected,Modifier.weight(1f).fillMaxHeight()){onMember(i)}}};Spacer(Modifier.height(12.dp));CuteCard(Modifier.fillMaxWidth().height(78.dp),background=cennetColors.sage.copy(.43f),corner=15.dp,padding=12.dp){Text("favori üyem",fontSize=8.sp,color=cennetColors.mutedText);Text(members[selected].name,Modifier.align(Alignment.CenterStart).padding(start=55.dp),fontFamily=FontFamily.Cursive,fontSize=23.sp,color=cennetColors.forest);Text("★  seçildi ve kaydedildi",Modifier.align(Alignment.CenterEnd),fontSize=10.sp,color=cennetColors.darkForest)}}
}

@Composable private fun ErasTab(order:List<Int>,onOrder:(List<Int>)->Unit){
    var selected by remember{mutableIntStateOf(0)}
    fun move(from:Int,to:Int){if(to !in order.indices||from==to)return;val x=order.toMutableList();val item=x.removeAt(from);x.add(to,item);selected=to;onOrder(x)}
    Column(Modifier.fillMaxSize()){
        CuteCard(Modifier.fillMaxWidth().height(82.dp),background=cennetColors.sage.copy(.4f),corner=15.dp,padding=13.dp){Text("favoriden daha az favorite doğru sırala",fontFamily=FontFamily.Cursive,fontSize=19.sp,color=cennetColors.forest);Text("kartı yatay sürükle veya okları kullan • her değişiklik kalıcıdır",Modifier.align(Alignment.BottomStart),fontSize=8.sp,color=cennetColors.mutedText);Row(Modifier.align(Alignment.CenterEnd),horizontalArrangement=Arrangement.spacedBy(7.dp)){MoveButton("←",selected>0){move(selected,selected-1)};Text("${selected+1} / 8",fontSize=9.sp,color=cennetColors.text);MoveButton("→",selected<7){move(selected,selected+1)}}}
        Spacer(Modifier.height(15.dp));Row(Modifier.fillMaxWidth().weight(1f),horizontalArrangement=Arrangement.spacedBy(11.dp)){order.forEachIndexed{pos,id->DraggableEra(id,pos,pos==selected,Modifier.weight(1f).fillMaxHeight(),{selected=pos}){direction->move(pos,pos+direction)}}};Spacer(Modifier.height(11.dp));Text("1 numaradaki dönem en favorin olarak ana ekranda gösterilir ♡",Modifier.fillMaxWidth(),textAlign=TextAlign.Center,fontSize=9.sp,color=cennetColors.mutedText)
    }
}
@Composable private fun DraggableEra(id:Int,position:Int,selected:Boolean,modifier:Modifier,onSelect:()->Unit,onMove:(Int)->Unit){
    var dx by remember{mutableFloatStateOf(0f)};val threshold=with(LocalDensity.current){34.dp.toPx()};val state=rememberDraggableState{dx+=it}
    Column(modifier.graphicsLayer{translationX=dx.coerceIn(-threshold,threshold)}.draggable(state=state,orientation=Orientation.Horizontal,onDragStopped={if(dx>threshold)onMove(1) else if(dx < -threshold)onMove(-1);dx=0f}).clickable(onClick=onSelect),horizontalAlignment=Alignment.CenterHorizontally){Text("${position+1}",fontFamily=FontFamily.Cursive,fontSize=20.sp,color=if(selected)cennetColors.forest else cennetColors.mutedText);Spacer(Modifier.height(5.dp));EraCard(id,eras[id],selected,Modifier.fillMaxWidth().weight(1f),onSelect);Spacer(Modifier.height(5.dp));Text("↔ sürükle",fontSize=7.sp,color=cennetColors.midGreen)}
}

@Composable private fun CollectionTab(owned:Set<Int>,onOwned:(Set<Int>)->Unit){
    Column(Modifier.fillMaxSize()){
        CuteCard(Modifier.fillMaxWidth().height(72.dp),background=cennetColors.sage.copy(.43f),corner=15.dp,padding=12.dp){Text("koleksiyon rafım",fontFamily=FontFamily.Cursive,fontSize=21.sp,color=cennetColors.forest);Text("sende olan kartlara dokun",Modifier.align(Alignment.BottomStart),fontSize=8.sp,color=cennetColors.mutedText);Text("${owned.size} / ${eras.size} tamamlandı  ♡",Modifier.align(Alignment.CenterEnd),fontSize=12.sp,color=cennetColors.darkForest)}
        Spacer(Modifier.height(12.dp));Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(11.dp)){repeat(2){row->Row(Modifier.fillMaxWidth().weight(1f),horizontalArrangement=Arrangement.spacedBy(11.dp)){repeat(4){column->val id=row*4+column;val has=id in owned;val toggle={onOwned(if(has)owned-id else owned+id)};CuteCard(Modifier.weight(1f).fillMaxHeight(),background=if(has)cennetColors.lightGreen.copy(.56f) else cennetColors.cream.copy(.75f),corner=14.dp,padding=9.dp,onClick=toggle){EraCard(id,eras[id],has,Modifier.fillMaxSize().padding(bottom=29.dp),toggle);Box(Modifier.fillMaxWidth().height(28.dp).align(Alignment.BottomCenter).background(cennetColors.cream.copy(.86f),RoundedCornerShape(8.dp)),contentAlignment=Alignment.Center){Text(if(has)"✓ koleksiyonumda" else "+ koleksiyona ekle",fontSize=8.sp,color=cennetColors.forest)}}}}}}
    }
}

@Composable private fun MemberCard(index:Int,selected:Boolean,modifier:Modifier,onClick:()->Unit){
    CuteCard(modifier,background=cennetColors.sage.copy(.35f),corner=13.dp,padding=6.dp,onClick=onClick){
        Image(painterResource(members[index].image),members[index].name,Modifier.fillMaxSize().padding(bottom=31.dp).clip(RoundedCornerShape(9.dp)),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxWidth().height(32.dp).align(Alignment.BottomCenter).background(cennetColors.cream.copy(.92f),RoundedCornerShape(bottomStart=8.dp,bottomEnd=8.dp))){Text(members[index].name,Modifier.align(Alignment.CenterStart).padding(start=7.dp),fontSize=8.sp,color=cennetColors.text);Text(if(selected)"★" else "☆",Modifier.align(Alignment.CenterEnd).padding(end=6.dp),fontSize=15.sp,color=cennetColors.forest)}
        if(selected)Box(Modifier.fillMaxSize().border(2.dp,cennetColors.forest,RoundedCornerShape(11.dp)))
    }
}
@Composable private fun RatingCard(rating:Int,modifier:Modifier,onRating:(Int)->Unit){CuteCard(modifier.fillMaxHeight(),corner=15.dp,padding=14.dp){Text("puanım • bir yıldıza dokun",fontSize=9.sp,color=cennetColors.text);Row(Modifier.align(Alignment.CenterStart),verticalAlignment=Alignment.CenterVertically){repeat(5){i->Text(if(i<rating)"★" else "☆",Modifier.clip(RoundedCornerShape(8.dp)).clickable{onRating(i+1)}.padding(3.dp),fontSize=25.sp,color=cennetColors.midGreen)};Spacer(Modifier.width(8.dp));Text("$rating.0",fontSize=17.sp,color=cennetColors.darkForest)};Text("dokunduğunda otomatik kaydolur ♡",Modifier.align(Alignment.BottomStart),fontSize=8.sp,color=cennetColors.mutedText)}}
@Composable private fun FavoriteSongCard(song:String,modifier:Modifier,onClick:()->Unit){val image=songImages[songs.indexOf(song).coerceAtLeast(0)];CuteCard(modifier.fillMaxHeight(),corner=15.dp,padding=14.dp,onClick=onClick){Text("favori şarkım",fontSize=9.sp,color=cennetColors.text);Image(painterResource(image),song,Modifier.size(61.dp).align(Alignment.CenterStart).clip(RoundedCornerShape(11.dp)),contentScale=ContentScale.Crop);Column(Modifier.align(Alignment.CenterStart).padding(start=76.dp)){Text(song,fontSize=12.sp,color=cennetColors.darkForest);Text("TOMORROW X TOGETHER",fontSize=7.sp,color=cennetColors.mutedText);Text("şarkı listesini aç  →",fontSize=7.sp,color=cennetColors.midGreen)};Text("♥",Modifier.align(Alignment.TopEnd),color=cennetColors.midGreen)}}
@Composable private fun EraCard(index:Int,name:String,selected:Boolean,modifier:Modifier,onClick:()->Unit){Column(modifier.clickable(onClick=onClick),horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(9.dp)).then(if(selected)Modifier.border(2.dp,cennetColors.forest,RoundedCornerShape(9.dp)) else Modifier),contentAlignment=Alignment.Center){Image(painterResource(eraImages[index]),name,Modifier.fillMaxSize(),contentScale=ContentScale.Crop);if(selected)Text("★",Modifier.align(Alignment.TopEnd).offset(x=3.dp,y=(-6).dp),color=cennetColors.forest,fontSize=15.sp)};Text(name,Modifier.padding(top=5.dp),fontSize=6.sp,lineHeight=7.sp,textAlign=TextAlign.Center,color=cennetColors.mutedText,maxLines=2)}}
@Composable private fun MoveButton(label:String,enabled:Boolean,onClick:()->Unit){Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(if(enabled)cennetColors.lightGreen else cennetColors.sage.copy(.3f)).clickable(enabled=enabled,onClick=onClick),contentAlignment=Alignment.Center){Text(label,fontSize=16.sp,color=if(enabled)cennetColors.forest else cennetColors.mutedText.copy(.4f))}}
private fun readOrder(raw:String?):List<Int>{val p=raw.orEmpty().split(',').mapNotNull(String::toIntOrNull);return if(p.size==eras.size&&p.toSet()==eras.indices.toSet())p else eras.indices.toList()}
private fun readSet(raw:String?):Set<Int> = raw.orEmpty().split(',').mapNotNull(String::toIntOrNull).filter{it in eras.indices}.toSet()
