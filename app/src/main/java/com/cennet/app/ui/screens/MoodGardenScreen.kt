package com.cennet.app.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.cennet.app.ui.components.*
import com.cennet.app.ui.theme.cennetColors
import com.cennet.app.R
import com.cennet.app.widget.CenoWidgets
import com.cennet.app.data.repository.PetStateStore
import com.cennet.app.data.repository.PetStats
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun MoodGardenScreen() {
    val context = LocalContext.current
    val initial = remember { PetStateStore.loadAndDecay(context) }
    var tokluk by remember { mutableIntStateOf(initial.hunger) }
    var mutluluk by remember { mutableIntStateOf(initial.happiness) }
    var enerji by remember { mutableIntStateOf(initial.energy) }
    var mesaj by remember { mutableStateOf("Seni gördüğüme çok sevindim! ♡") }
    var tepki by remember { mutableStateOf(DoggyMood.IDLE) }
    var tepkiSayaci by remember { mutableIntStateOf(0) }
    var etkileşimKilidi by remember { mutableStateOf(false) }

    LaunchedEffect(tokluk, mutluluk, enerji) {
        PetStateStore.save(context, PetStats(tokluk, mutluluk, enerji))
        CenoWidgets.refreshAll(context)
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            val current = PetStateStore.loadAndDecay(context)
            tokluk = current.hunger
            mutluluk = current.happiness
            enerji = current.energy
        }
    }
    fun etkileşim(yeniMesaj: String, yeniTepki: DoggyMood, değiştir: () -> Unit) {
        if (etkileşimKilidi) return
        etkileşimKilidi = true
        değiştir(); mesaj = yeniMesaj; tepki = yeniTepki; tepkiSayaci++
    }

    LaunchedEffect(tepki, tepkiSayaci) {
        if (tepki != DoggyMood.IDLE) {
            delay(2_600)
            tepki = DoggyMood.IDLE
            mesaj = "Yanında olmak çok güzel ♡"
            etkileşimKilidi = false
        }
    }

    val bounce by animateFloatAsState(
        if (tepkiSayaci % 2 == 0) 1f else 1.07f,
        spring(dampingRatio = .38f, stiffness = Spring.StiffnessMediumLow), label = "pet tepkisi"
    )
    val petVideo = when (tepki) {
        DoggyMood.IDLE -> R.raw.doggy_idle
        DoggyMood.HAPPY -> R.raw.doggy_happy
        DoggyMood.SURPRISED -> R.raw.doggy_surprised
        DoggyMood.ANGRY -> R.raw.doggy_angry
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DarkSectionHeader("MİNİK DOSTUM", Modifier.fillMaxWidth())
        CuteCard(Modifier.fillMaxWidth().weight(1f), corner = 18.dp, padding = 22.dp) {
            DoodleSparkles(Modifier.fillMaxSize())
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(1.35f).fillMaxHeight()) {
                    Text("Doggy'nin minik köşesi", fontFamily = FontFamily.Cursive, fontSize = 30.sp, color = cennetColors.forest)
                    Text("onu besle, sev ve birlikte biraz oyun oyna ♡", fontSize = 10.sp, color = cennetColors.mutedText)
                    Spacer(Modifier.height(18.dp))
                    CuteCard(Modifier.fillMaxWidth().weight(1f), background = cennetColors.sage.copy(.38f), corner = 24.dp, padding = 18.dp) {
                        Text("✦     ♡       ✧", color = cennetColors.lightGreen, fontSize = 22.sp, modifier = Modifier.align(Alignment.TopCenter))
                        DoggyAnimation(petVideo, Modifier.size(330.dp).scale(bounce).align(Alignment.Center))
                        Box(Modifier.align(Alignment.TopEnd).background(cennetColors.cream, RoundedCornerShape(50)).padding(horizontal = 16.dp, vertical = 10.dp)) { Text(mesaj, fontSize = 10.sp, color = cennetColors.mutedText) }
                        Text("Doggy ♡", Modifier.align(Alignment.BottomCenter), fontFamily = FontFamily.Cursive, fontSize = 24.sp, color = cennetColors.forest)
                    }
                }
                Spacer(Modifier.width(22.dp))
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Text("bugün nasıl?", fontFamily = FontFamily.Cursive, fontSize = 24.sp, color = cennetColors.forest)
                    Spacer(Modifier.height(15.dp))
                    PetDurum("tokluk", "🍓", tokluk)
                    PetDurum("mutluluk", "♡", mutluluk)
                    PetDurum("enerji", "☾", enerji)
                    Spacer(Modifier.height(20.dp))
                    Text("birlikte ne yapalım?", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = cennetColors.darkForest)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PetEylem("besle", "🍓", Modifier.weight(1f), !etkileşimKilidi) {
                            if (tokluk >= 95) etkileşim("Karnım çok tok, şimdi istemiyorum!", DoggyMood.ANGRY) {}
                            else etkileşim("Mmm, çok lezzetliydi!", DoggyMood.HAPPY) { tokluk = min(100, tokluk + 16); enerji = min(100, enerji + 3) }
                        }
                        PetEylem("oyna", "✦", Modifier.weight(1f), !etkileşimKilidi) {
                            if (enerji < 15) etkileşim("Çok yorgunum, biraz dinlenelim!", DoggyMood.ANGRY) {}
                            else etkileşim("Bir daha oynayalım! ♡", DoggyMood.HAPPY) { mutluluk = min(100, mutluluk + 17); enerji = (enerji - 7).coerceAtLeast(0) }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PetEylem("sev", "♡", Modifier.weight(1f), !etkileşimKilidi) { etkileşim("Aaa! Bu sevgi beni şaşırttı ♡", DoggyMood.SURPRISED) { mutluluk = min(100, mutluluk + 11) } }
                        PetEylem("ödül ver", "♧", Modifier.weight(1f), !etkileşimKilidi) {
                            if (tokluk >= 97 && mutluluk >= 97) etkileşim("Şimdilik yeter, biraz sonra yine verirsin!", DoggyMood.ANGRY) {}
                            else etkileşim("Bu ödüle bayıldım! ♡", DoggyMood.HAPPY) { mutluluk = min(100, mutluluk + 9); tokluk = min(100, tokluk + 5) }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    CuteCard(Modifier.fillMaxWidth().height(82.dp), background = cennetColors.peach.copy(.42f), corner = 15.dp, padding = 13.dp) {
                        Text("minik not", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = cennetColors.darkForest)
                        Text("Değerleri uygulama kapalıyken de yavaşça azalır. Her tepki bitince yeniden oynayabilirsin ♡", Modifier.align(Alignment.BottomStart), fontSize = 9.sp, lineHeight = 14.sp, color = cennetColors.mutedText)
                    }
                }
            }
        }
    }
}

@Composable
private fun PetDurum(ad: String, simge: String, değer: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).background(cennetColors.cream, CircleShape), contentAlignment = Alignment.Center) { Text(simge, fontSize = 16.sp) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(ad, fontSize = 10.sp, color = cennetColors.text); Text("%$değer", fontSize = 9.sp, color = cennetColors.mutedText) }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(8.dp).background(cennetColors.background, RoundedCornerShape(50))) { Box(Modifier.fillMaxWidth(değer / 100f).fillMaxHeight().background(if (değer < 30) cennetColors.peach else cennetColors.midGreen, RoundedCornerShape(50))) }
        }
    }
}

@Composable
private fun PetEylem(ad: String, simge: String, modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    CuteCard(modifier.height(86.dp).alpha(if (enabled) 1f else .55f), background = cennetColors.cream, corner = 16.dp, padding = 9.dp, onClick = { if (enabled) onClick() }) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(simge, fontSize = 23.sp); Text(ad, fontSize = 10.sp, color = cennetColors.darkForest) }
    }
}
