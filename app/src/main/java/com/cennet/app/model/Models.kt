package com.cennet.app.model

enum class CennetScreen(val label: String, val glyph: String) {
    HOME("ana sayfa", "⌂"), DIARY("günlük", "▣"), GARDEN("minik dostum", "♧"),
    REFERENCES("referanslar", "▥"), MOA("moa köşesi", "♡"), LETTERS("mektuplar", "✉"),
    MERCH("merch çantam", "◇"), SETTINGS("ayarlar", "⚙")
}

enum class Mood(val label: String, val face: String) {
    HAPPY("mutlu", "˶ᵔ ᵕ ᵔ˶"), COZY("huzurlu", "˘ ᵕ ˘"), TIRED("yorgun", "－ ᵕ －"),
    SAD("üzgün", "｡•́︿•̀｡"), EXCITED("heyecanlı", "✧▽✧"), MEH("eh işte", "• ︵ •")
}

data class ReminderItem(val id: Int, val text: String, val done: Boolean = false)
data class MerchSlot(
    val id: Int,
    val localImagePath: String? = null,
    val columnSpan: Int = 1,
    val rowSpan: Int = 1
)
data class GardenDay(val day: String, val mood: Mood, val stage: Int)
data class Letter(val title: String, val body: String, val tint: Long, val locked: Boolean = false)

val drawingPrompts = listOf(
    "en sevdiğin TXT şarkısını büyülü bir yer olarak çiz",
    "bugünkü ruh halini minik bir orman ruhuna dönüştür",
    "yumuşacık bir bulutun içine saklanmış oda tasarla",
    "beş farklı anıyı beş çiçek olarak çiz",
    "yıldızların arasında giden sessiz bir tren hayal et",
    "yağmur damlalarından yapılmış sevimli bir kasaba çiz",
    "en sevdiğin rengi koruyan minik bir yaratık tasarla"
)

val littleNotes = listOf(
    "harika olmak için kusursuz olman gerekmiyor. çok güzel gidiyorsun ♡",
    "minik adımlar da seni güzel yerlere götürür ♡",
    "dinlenmek de büyümenin bir parçası, minik filiz ♡",
    "yumuşacık kalbin yumuşacık günleri hak ediyor ♡"
)
