package com.example.bastionarmor.domain.model

enum class EnemyType(
    val displayedName: String,
    val displayedHealth: Int,
    val baseSpeed: Float,
    val baseReward: Int,
    val baseDamage: Int,
    val baseFiringRate: Long


) {
    BASIC("Basic Enemy",100,50f,10,5,2000L),
    FAST("Fast Enemy",60,80f,15,3,1500L),
    TANK("Tank Enemy",200,30f,25,10,2500L),
    BOSS("Boss Enemy",500,40f,100,200,3000L)


}