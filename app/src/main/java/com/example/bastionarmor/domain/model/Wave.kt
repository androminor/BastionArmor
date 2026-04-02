package com.example.bastionarmor.domain.model

data class Wave(
    val waveNumber:Int,
    val enemies:List<EnemySpawn>,
    val isCompleted:Boolean
)


data class EnemySpawn(
    val enemyType:EnemyType,
    val count: Int,
    val spawnDelay: Long
)
