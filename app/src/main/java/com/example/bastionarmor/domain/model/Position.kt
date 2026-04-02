package com.example.bastionarmor.domain.model

import kotlin.math.pow
import kotlin.math.sqrt

data class Position(
    val x: Float, val y: Float
) {
    fun distanceTo(other: Position): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    // Alias for consistency with existing calls
    fun distancessTo(other: Position): Float = distanceTo(other)

    fun distancesToUsingPow(other: Position): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx.pow(2) + dy.pow(2))
    }
}

fun Position.distance(other: Position): Float {
    val dx = other.x - this.x
    val dy = other.y - this.y
    return sqrt(dx * dx + dy * dy)
}
