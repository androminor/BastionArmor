package com.example.bastionarmor.utility

import com.example.bastionarmor.domain.model.TowerType

data class UpgradeOption(
    val type: UpgradeType,
    val targetType: TowerType? = null,
    val cost: Int,
    val description: String
)

enum class UpgradeType{
    LEVEL_UP,
    TYPE_SWAP,
    REPAIR
}
