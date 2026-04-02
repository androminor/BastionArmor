package com.example.bastionarmor.domain.model

enum class TowerType(
    val displayName: String,
    val baseDamage: Int,
    val baseRange: Float,
    val baseFireRate: Long,
    val baseCost: Int
) {
    // Nerfed damage so health decrease is visible (Basic Enemy has 100 HP)
    GUNNER("Gunner", 20, 150f, 400L, 50),        // 20 damage = 5 shots to kill
    SENTRY("Sentry", 35, 120f, 600L, 75),        // 35 damage = 3 shots to kill
    TRIPLE_SHOOTER("Triple Shooter", 15, 180f, 300L, 100), // 15 damage = fast but weak
    THROWING_AXE("Throwing Axe", 50, 100f, 800L, 150)      // 50 damage = 2 shots to kill
}
