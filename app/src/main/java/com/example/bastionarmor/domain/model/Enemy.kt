package com.example.bastionarmor.domain.model

data class Enemy(
    val id: Int,
    val type: EnemyType,
    val position: Position,
    val health: Int,
    val maxHealth: Int,
    val speed: Float,
    val reward: Int,
    val damage: Int, 
    val firingRate: Long,
    val lastAttackTime: Long = 0L,
    val pathIndex: Int = 0,
    val isAlive: Boolean = true
) {
    fun takeDamage(damage: Int): Enemy {
        val newHealth = (health - damage).coerceAtLeast(0)
        return copy(
            health = newHealth,
            isAlive = newHealth > 0
        )
    }

    fun moveAlongPath(path: List<Position>, deltaTime: Float): Enemy {
        if (pathIndex >= path.size - 1) return this 

        val currentTarget = path[pathIndex + 1]
        val distance = position.distanceTo(currentTarget)

        if (distance < 2f) { // Closer threshold
            return copy(position = currentTarget, pathIndex = pathIndex + 1)
        }

        // Avoid division by zero
        val safeDistance = if (distance < 0.001f) 0.001f else distance

        val normalizedDirection = Position(
            (currentTarget.x - position.x) / safeDistance,
            (currentTarget.y - position.y) / safeDistance
        )

        return copy(
            position = Position(
                position.x + normalizedDirection.x * speed * deltaTime,
                position.y + normalizedDirection.y * speed * deltaTime
            )
        )
    }

    fun canAttack(currentTime: Long): Boolean {
        return currentTime - lastAttackTime >= firingRate && isAlive
    }
}
