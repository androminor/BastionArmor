package com.example.bastionarmor.domain.model

import com.example.bastionarmor.utility.UpgradeOption
import com.example.bastionarmor.utility.UpgradeType

data class Tower(
    val id: String,
    val position: Position,
    val type: TowerType,
    val level: Int = 1,
    val damage: Int,
    val maxDamage: Int,
    val range: Float,
    val fireRate: Long, // milliseconds between shots
    val cost: Int,
    val lastShotTime: Long = 0L,
    val health: Int,
    val maxHealth: Int = 100
) {

    fun canShoot(currentTime: Long): Boolean {
        return currentTime - lastShotTime >= fireRate && isOperational()
    }

    fun isOperational(): Boolean {
        return health > 0 && damage > 0
    }

    fun isInRange(enemyPosition: Position): Boolean {
        val distance = position.distancessTo(enemyPosition)
        return distance <= range
    }

    // Repair tower to full health and damage
    fun repair(): Pair<Tower, Int> {
        val repairCost = costOfRepair()
        val repairedTower = copy(
            health = maxHealth,
            damage = maxDamage
        )
        return Pair(repairedTower, repairCost)
    }

    private fun costOfRepair(): Int {
        val healthPercentage = health.toFloat() / maxHealth.toFloat()
        val damagePercentage = damage.toFloat() / maxDamage.toFloat()
        val averageCondition = (healthPercentage + damagePercentage) / 2f
        return ((1f - averageCondition) * cost * 0.3f).toInt().coerceAtLeast(5)
    }

    /**
     * Reduces the tower's health when it takes damage from an enemy.
     * @param damageAmount The amount of damage inflicted by the enemy.
     * @return A new Tower instance with reduced health.
     */
    fun takeDamageOnTower(damageAmount: Int): Tower {
        val newHealth = (this.health - damageAmount).coerceAtLeast(0)
        val newDamage = if (newHealth <= 0) 0 else (this.damage * 0.95f).toInt() // Reduce damage when damaged
        return this.copy(
            health = newHealth,
            damage = newDamage.coerceAtLeast(0)
        )
    }

    fun upgrade(): Tower {
        return copy(
            level = level + 1,
            damage = damage + (damage * 0.5f).toInt(),
            maxDamage = maxDamage + (maxDamage * 0.5f).toInt(),
            range = range * 1.2f,
            fireRate = (fireRate * 0.9f).toLong(), // Faster firing
            cost = cost * 2,
            health = maxHealth,
            maxHealth = (maxHealth * 1.1f).toInt()
        )
    }

    fun upgradeToType(newType: TowerType): Tower {
        val typeSwapCost = calculateTypeSwapCost(newType)
        return copy(
            type = newType,
            level = 1,
            damage = newType.baseDamage,
            maxDamage = newType.baseDamage,
            range = newType.baseRange,
            fireRate = newType.baseFireRate,
            cost = typeSwapCost,
            health = maxHealth,
            lastShotTime = 0L
        )
    }

    private fun calculateTypeSwapCost(newType: TowerType): Int {
        val currentTypeCost = type.baseCost * level
        val newTypeCost = newType.baseCost
        val upgradeFee = (cost * 0.5f).toInt()
        return (newTypeCost - currentTypeCost).coerceAtLeast(0) + upgradeFee
    }

    fun getUpgradeOptions(): List<UpgradeOption> {
        val options = mutableListOf<UpgradeOption>()

        // Level up option
        if (level < 5) {
            options.add(
                UpgradeOption(
                    UpgradeType.LEVEL_UP,
                    cost = cost,
                    description = "Upgrade to Level ${level + 1}\n+50% Damage, +20% Range, +10% Speed"
                )
            )
        }

        // Type swap options
        TowerType.values().filter { it != type }.forEach { towerType ->
            options.add(
                UpgradeOption(
                    UpgradeType.TYPE_SWAP,
                    targetType = towerType,
                    cost = calculateTypeSwapCost(towerType),
                    description = "Convert to ${towerType.displayName}\n${towerType.baseDamage} DMG, ${towerType.baseRange.toInt()} Range"
                )
            )
        }

        // Repair option
        if (health < maxHealth || damage < maxDamage) {
            options.add(
                UpgradeOption(
                    UpgradeType.REPAIR,
                    cost = costOfRepair(),
                    description = "Repair Tower\nRestore health and damage to maximum"
                )
            )
        }

        return options
    }
}