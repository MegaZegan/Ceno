package com.cennet.app.data.repository

import android.content.Context

data class PetStats(val hunger: Int, val happiness: Int, val energy: Int)

object PetStateStore {
    private const val PREFS = "minik_dostum"
    private const val HUNGER_INTERVAL = 30 * 60 * 1000L
    private const val ENERGY_INTERVAL = 60 * 60 * 1000L
    private const val HAPPINESS_INTERVAL = 2 * 60 * 60 * 1000L

    fun loadAndDecay(context: Context, now: Long = System.currentTimeMillis()): PetStats {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val hunger = decay(prefs.getInt("tokluk", 72), prefs.getLong("tokluk_tick", now), HUNGER_INTERVAL, now)
        val happiness = decay(prefs.getInt("mutluluk", 84), prefs.getLong("mutluluk_tick", now), HAPPINESS_INTERVAL, now)
        val energy = decay(prefs.getInt("enerji", 66), prefs.getLong("enerji_tick", now), ENERGY_INTERVAL, now)
        prefs.edit()
            .putInt("tokluk", hunger.first).putLong("tokluk_tick", hunger.second)
            .putInt("mutluluk", happiness.first).putLong("mutluluk_tick", happiness.second)
            .putInt("enerji", energy.first).putLong("enerji_tick", energy.second)
            .apply()
        return PetStats(hunger.first, happiness.first, energy.first)
    }

    fun save(context: Context, stats: PetStats) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val editor = prefs.edit()
            .putInt("tokluk", stats.hunger.coerceIn(0, 100))
            .putInt("mutluluk", stats.happiness.coerceIn(0, 100))
            .putInt("enerji", stats.energy.coerceIn(0, 100))
        if (!prefs.contains("tokluk_tick")) editor.putLong("tokluk_tick", now)
        if (!prefs.contains("mutluluk_tick")) editor.putLong("mutluluk_tick", now)
        if (!prefs.contains("enerji_tick")) editor.putLong("enerji_tick", now)
        editor.apply()
    }

    private fun decay(value: Int, lastTick: Long, interval: Long, now: Long): Pair<Int, Long> {
        if (lastTick <= 0L || lastTick > now) return value.coerceIn(0, 100) to now
        val steps = ((now - lastTick) / interval).coerceAtMost(10_000L)
        if (steps <= 0L) return value.coerceIn(0, 100) to lastTick
        return (value - steps.toInt()).coerceIn(0, 100) to (lastTick + steps * interval)
    }
}
