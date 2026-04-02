package com.example.bastionarmor.domain.model

data class GameBoard(
    val width: Int = 800,
    val height: Int = 600,
    val gridSize: Int = 40,
    val path: List<Position>
) {
    fun isValidTowerPosition(position: Position): Boolean {
        println("Checking tower position: (${position.x}, ${position.y})")

        // Check bounds
        if (position.x < 40 || position.x > width - 40 || position.y < 40 || position.y > height - 40) {
            println("Position out of bounds")
            return false
        }

        // NEW: Check if CLOSE ENOUGH to path (not too far)
        val minDistanceFromPath = 35f  // Must be at least 35 pixels from path
        val maxDistanceFromPath = 90f  // But not more than 90 pixels from path

        val closestPathDistance = path.minOfOrNull { pathPos ->
            position.distancessTo(pathPos)
        } ?: Float.MAX_VALUE

        println("Closest distance to path: $closestPathDistance")

        if (closestPathDistance < minDistanceFromPath) {
            println("Too close to path (min: $minDistanceFromPath)")
            return false
        }

        if (closestPathDistance > maxDistanceFromPath) {
            println("Too far from path (max: $maxDistanceFromPath)")
            return false
        }

        println("Position is valid! Distance to path: $closestPathDistance")
        return true
    }

    fun snapToGrid(position: Position): Position {
        val snappedX = ((position.x / gridSize).toInt() * gridSize + gridSize / 2f)
        val snappedY = ((position.y / gridSize).toInt() * gridSize + gridSize / 2f)
        return Position(snappedX, snappedY)
    }

    // Get all valid tower positions for visual feedback
    fun getValidTowerZones(): List<Position> {
        val zones = mutableListOf<Position>()

        // Generate positions around the path
        for (pathPoint in path) {
            // Add positions around each path point
            val offsets = listOf(
                //Position for top-left, top, top-right, left, right, bottom-left, bottom, bottom-right).
                Position(-60f, -60f),
                Position(0f, -70f),
                Position(60f, -60f),
                Position(-70f, 0f),
                Position(70f, 0f),
                Position(-60f, 60f),
                Position(0f, 70f),
                Position(60f, 60f)
            )

            offsets.forEach { offset ->
                val candidatePos = Position(
                    pathPoint.x + offset.x,
                    pathPoint.y + offset.y
                )
                if (isValidTowerPosition(candidatePos)) {
                    zones.add(candidatePos)
                }
            }
        }

        return zones.distinctBy { "${it.x.toInt()}_${it.y.toInt()}" }
    }
}