package com.cennet.app

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cennet.app.data.repository.CennetRepository
import com.cennet.app.model.CennetScreen
import com.cennet.app.ui.components.*
import com.cennet.app.ui.screens.*
import com.cennet.app.ui.theme.CennetTheme
import com.cennet.app.ui.theme.cennetColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val requestedScreen = mutableStateOf(CennetScreen.HOME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedScreen.value = screenFromIntent(intent)
        setContent {
            val repository = remember { CennetRepository(applicationContext) }
            var themeIndex by remember { mutableIntStateOf(repository.themeIndex) }
            CennetTheme(themeIndex) {
                CennetApp(repository, themeIndex, requestedScreen.value) {
                    themeIndex = it
                    repository.themeIndex = it
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedScreen.value = screenFromIntent(intent)
    }

    private fun screenFromIntent(intent: Intent?): CennetScreen =
        intent?.getStringExtra(EXTRA_SCREEN)?.let { value ->
            CennetScreen.entries.firstOrNull { it.name == value }
        } ?: CennetScreen.HOME

    companion object {
        const val EXTRA_SCREEN = "ceno_screen"
    }

}

@Composable
private fun CennetApp(repository: CennetRepository, themeIndex: Int, requestedScreen: CennetScreen, onThemeChange: (Int) -> Unit) {
    var screen by remember { mutableStateOf(requestedScreen) }
    LaunchedEffect(requestedScreen) { screen = requestedScreen }
    var displayName by remember { mutableStateOf("Ceno") }
    val initialBirthday = remember { repository.birthday.takeUnless { it.isBlank() || it == "09-18" } ?: "03-09" }
    var birthday by remember { mutableStateOf(initialBirthday) }
    LaunchedEffect(initialBirthday) { repository.birthday = initialBirthday; repository.displayName = "Ceno" }
    val today = remember { LocalDate.now() }
    val birthdayMode = birthday == today.format(DateTimeFormatter.ofPattern("MM-dd"))

    BoxWithConstraints(Modifier.fillMaxSize().background(cennetColors.background)) {
        val landscape = maxWidth > maxHeight
        Row(Modifier.fillMaxSize()) {
            Sidebar(
                selected = screen,
                onSelect = { screen = it },
                name = displayName,
                compact = !landscape,
                modifier = Modifier.width(if (landscape) 178.dp else 126.dp).fillMaxHeight()
            )
            Box(Modifier.weight(1f).fillMaxHeight().background(cennetColors.sage.copy(.48f))) {
                when (screen) {
                    CennetScreen.HOME -> HomeScreen(repository, birthdayMode) { screen = it }
                    CennetScreen.DIARY -> DiaryScreen(repository)
                    CennetScreen.GARDEN -> MoodGardenScreen()
                    CennetScreen.REFERENCES -> ReferenceShelfScreen()
                    CennetScreen.MOA -> MoaCornerScreen()
                    CennetScreen.LETTERS -> LettersScreen(birthdayMode)
                    CennetScreen.MERCH -> MerchBagScreen(repository)
                    CennetScreen.SETTINGS -> SettingsScreen(
                        repository, displayName, birthday, themeIndex,
                        onDisplayName = { displayName = it; repository.displayName = it },
                        onBirthday = { birthday = it; repository.birthday = it },
                        onTheme = onThemeChange,
                        onReset = { repository.resetAll(); displayName = "Ceno"; birthday = "03-09"; onThemeChange(0) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Sidebar(
    selected: CennetScreen,
    onSelect: (CennetScreen) -> Unit,
    name: String,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier.background(cennetColors.background).padding(horizontal = if (compact) 10.dp else 18.dp, vertical = 18.dp)) {
        Text("Ceno ♡", color = cennetColors.forest, fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive, fontSize = if (compact) 25.sp else 32.sp)
        if (!compact) Text("yalnızca sana ait minik bir dünya ♡", color = cennetColors.mutedText, fontSize = 10.sp)
        Spacer(Modifier.height(20.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            CennetScreen.entries.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(13.dp))
                        .background(if (item == selected) cennetColors.lightGreen.copy(.8f) else Color.Transparent)
                        .then(Modifier)
                        .padding(horizontal = 11.dp, vertical = 10.dp)
                        .noRippleClickable { onSelect(item) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.glyph, color = cennetColors.darkForest, fontSize = 18.sp, modifier = Modifier.width(27.dp))
                    Text(item.label, color = cennetColors.text, fontSize = if (compact) 10.sp else 12.sp, fontWeight = if (item == selected) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        CuteCard(Modifier.fillMaxWidth().height(if (compact) 92.dp else 105.dp), corner = 13.dp, padding = 9.dp) {
            Mascot(Modifier.size(if (compact) 42.dp else 54.dp).align(Alignment.BottomStart), bounce = true)
            if (!compact) Text("bugün de\nharikaydın!", fontSize = 10.sp, lineHeight = 14.sp, color = cennetColors.mutedText, modifier = Modifier.align(Alignment.CenterEnd))
            Text("✦", color = cennetColors.midGreen, modifier = Modifier.align(Alignment.TopStart))
        }
    }
}

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = this.clickable(
    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
    indication = null,
    onClick = onClick
)
