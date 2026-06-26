package com.contratop.featureclicker

data class Upgrade(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val baseCost: Long,
    val costMultiplier: Double = 1.15,
    val level: Int = 0,
    val commitsPerClickAdded: Long = 0,
    val commitsPerSecondAdded: Long = 0
) {
    val currentCost: Long
        get() = (baseCost * Math.pow(costMultiplier, level.toDouble())).toLong()
}
