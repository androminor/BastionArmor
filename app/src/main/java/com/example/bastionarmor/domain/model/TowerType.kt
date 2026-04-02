package com.example.bastionarmor.domain.model

enum class TowerType(
    val displayName: String,
    val baseDamage: Int,
    val baseRange: Float,
    val baseFireRate: Long,
    val baseCost: Int
) {
    // Made SUPER powerful for testing - should kill Wave 1 enemies (100 HP) in 1-2 shots
    GUNNER("Gunner", 120, 120f, 500L, 50),        // 120 damage = instant kill
    SENTRY("Sentry", 150, 100f, 600L, 75),        // 150 damage = overkill
    TRIPLE_SHOOTER("Triple Shooter", 100, 140f, 400L, 100), // 100 damage = almost instant kill
    THROWING_AXE("Throwing Axe", 200, 80f, 800L, 150)      // 200 damage = massive overkill
}