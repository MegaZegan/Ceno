package com.cennet.app.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.cennet.app.ui.components.*
import com.cennet.app.ui.theme.*
import com.cennet.app.data.database.DiaryEntry
import com.cennet.app.data.repository.CennetRepository
import com.cennet.app.widget.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun SettingsScreen(
    repository:CennetRepository,displayName:String,birthday:String,themeIndex:Int,fontIndex:Int,
    onDisplayName:(String)->Unit,onBirthday:(String)->Unit,onTheme:(Int)->Unit,onFont:(Int)->Unit,onReset:suspend ()->Unit
) {
    val context=LocalContext.current
    var name by remember(displayName){mutableStateOf(displayName)}
    var date by remember(birthday){mutableStateOf(birthday)}
    var status by remember{mutableStateOf("")}
    var confirmReset by remember{mutableStateOf(false)}
    val scope=rememberCoroutineScope()
    val widgetManager=remember { AppWidgetManager.getInstance(context) }
    val widgets=remember { listOf(
        "çizim" to DrawingWidgetProvider::class.java, "günlük" to DiaryWidgetProvider::class.java,
        "fotoğraf" to PhotoWidgetProvider::class.java, "Doggy" to PetWidgetProvider::class.java,
        "mektup" to LettersWidgetProvider::class.java, "MOA" to MoaWidgetProvider::class.java
    ) }
    fun pinWidget(provider: Class<*>, label: String) {
        if (widgetManager.isRequestPinAppWidgetSupported) {
            widgetManager.requestPinAppWidget(ComponentName(context, provider), null, null)
            status="$label widget'ı için ana ekran onayı açıldı ♡"
        } else status="ana ekrana uzun basıp Ceno widget'ını seçebilirsin ♡"
    }
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->uri?.let{scope.launch{runCatching{val diary=JSONArray();repository.allDiaryEntries().forEach{entry->diary.put(JSONObject().put("date",entry.date).put("text",entry.text).put("photoUri",entry.photoUri).put("updatedAt",entry.updatedAt))};val root=JSONObject().put("displayName",name).put("birthday",date).put("theme",themeIndex).put("font",fontIndex).put("diary",diary);context.contentResolver.openOutputStream(it)?.bufferedWriter()?.use{writer->writer.write(root.toString(2))}}.onSuccess{status="günlük ve ayarlar yedeği kaydedildi ♡"}.onFailure{status="yedek kaydedilemedi"}}}}
    val restore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->uri?.let{scope.launch{runCatching{context.contentResolver.openInputStream(it)?.bufferedReader()?.use{reader->JSONObject(reader.readText())}}.onSuccess{json->json?.let{name=it.optString("displayName",name);date=it.optString("birthday",date);onDisplayName(name);onBirthday(date);onTheme(it.optInt("theme",themeIndex));onFont(it.optInt("font",fontIndex));val diary=it.optJSONArray("diary");if(diary!=null)for(i in 0 until diary.length()){val entry=diary.getJSONObject(i);repository.saveDiary(DiaryEntry(date=entry.getString("date"),text=entry.getString("text"),photoUri=entry.optString("photoUri").takeUnless{value->value=="null"||value.isBlank()},updatedAt=entry.optLong("updatedAt",System.currentTimeMillis())))};status="günlük ve ayarlar geri yüklendi ♡"}}.onFailure{status="bu yedek okunamadı"}}}}
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DarkSectionHeader("AYARLAR",Modifier.fillMaxWidth())
        CuteCard(Modifier.fillMaxWidth().weight(1f),corner=18.dp,padding=24.dp) {
            DoodleSparkles(Modifier.fillMaxSize())
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text("bu alanı kendine göre düzenle",fontFamily=FontFamily.Cursive,fontSize=30.sp,color=cennetColors.forest)
                    Text("her şey usulca bu cihazda kalır ♡",fontSize=10.sp,color=cennetColors.mutedText)
                    Spacer(Modifier.height(24.dp))
                    SettingRow("görünen ad") { SoftInput(name,"Ceno"){name=it;onDisplayName(it)} }
                    SettingRow("doğum günü") { SoftInput(date,"GG-AA"){value->date=value.filter{it.isDigit()||it=='-'}.take(5);onBirthday(date)} }
                    SettingRow("tema") { Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){CennetColorPalettes.forEachIndexed{i,palette->Box(Modifier.size(if(i==themeIndex)34.dp else 29.dp).background(palette.lightGreen,androidx.compose.foundation.shape.CircleShape).border(if(i==themeIndex)2.dp else .7.dp,palette.forest,androidx.compose.foundation.shape.CircleShape).clickable{onTheme(i)})} } }
                    SettingRow("yazı tipi") { Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){listOf(FontFamily.Cursive,FontFamily.SansSerif,FontFamily.Serif).forEachIndexed{i,font->Box(Modifier.background(if(fontIndex==i)cennetColors.lightGreen else cennetColors.background,RoundedCornerShape(10.dp)).clickable{onFont(i);status="yazı tipi kaydedildi ♡"}.padding(horizontal=18.dp,vertical=8.dp)){Text("Aa",fontFamily=font,fontSize=17.sp)}}} }
                    SettingRow("ana ekran widget'ları") { Column(verticalArrangement=Arrangement.spacedBy(7.dp)) { widgets.chunked(3).forEach { row -> Row(horizontalArrangement=Arrangement.spacedBy(7.dp)) { row.forEach { (label, provider) -> SoftButton(label) { pinWidget(provider,label) } } } } } }
                    SettingRow("günlük ve ayarlar yedeği") { Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){SoftButton("yedekle"){export.launch("ceno-yedek.json")};SoftButton("geri yükle"){restore.launch(arrayOf("application/json"))}} }
                    SettingRow("yerel veriler") { SoftButton("tüm verileri sıfırla"){confirmReset=true} }
                    if(status.isNotBlank())Text(status,fontSize=10.sp,color=cennetColors.midGreen,modifier=Modifier.padding(top=10.dp))
                }
                CuteCard(Modifier.width(255.dp).fillMaxHeight(),background=cennetColors.sage.copy(.44f),corner=20.dp,padding=18.dp) {
                    Mascot(Modifier.size(150.dp).align(Alignment.Center),mood="happy",bounce=true)
                    Text("senin alanın,\nsenin hızın ♡",Modifier.align(Alignment.BottomCenter).padding(bottom=30.dp),fontFamily=FontFamily.Cursive,fontSize=25.sp,color=cennetColors.forest)
                    Text("Ceno  ·  yerel ve çevrimdışı",Modifier.align(Alignment.BottomCenter),fontSize=8.sp,color=cennetColors.mutedText)
                }
            }
        }
    }
    if(confirmReset) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(.3f)).clickable{confirmReset=false},contentAlignment=Alignment.Center) {
            CuteCard(Modifier.width(390.dp).height(190.dp).clickable(enabled=false){},corner=23.dp,padding=22.dp) {
                Column {
                    Text("bu minik alan sıfırlansın mı?",fontFamily=FontFamily.Cursive,fontSize=25.sp,color=cennetColors.forest)
                    Text("Günlük sayfaları, referanslar, minik dost verileri ve tercihler bu cihazdan silinecek.",fontSize=10.sp,lineHeight=17.sp,color=cennetColors.mutedText)
                    Spacer(Modifier.weight(1f))
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
                        SoftButton("her şeyi koru"){confirmReset=false}
                        Spacer(Modifier.width(8.dp))
                        SoftButton("sıfırla"){scope.launch{onReset();confirmReset=false}}
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(label:String,content:@Composable RowScope.()->Unit){Row(Modifier.fillMaxWidth().padding(vertical=11.dp),verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.width(145.dp),fontSize=11.sp,fontWeight=FontWeight.SemiBold,color=cennetColors.text);content()}}

@Composable
private fun SoftInput(value:String,hint:String,onChange:(String)->Unit){BasicTextField(value,onChange,Modifier.width(230.dp).background(cennetColors.background,RoundedCornerShape(10.dp)).padding(horizontal=13.dp,vertical=9.dp),singleLine=true,textStyle=androidx.compose.ui.text.TextStyle(fontSize=11.sp,color=cennetColors.text),decorationBox={inner->if(value.isEmpty())Text(hint,fontSize=10.sp,color=cennetColors.mutedText);inner()})}
