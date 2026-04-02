package com.example.bastionarmor.domain.model

data class Enemy(
    val id: Int,
    val type: EnemyType,
    val position: Position,
    val health: Int,
    val maxHealth: Int,
    val speed: Float,
    val reward: Int,
    val damage: Int, //damage enemy caused to towers
    val firingRate: Long,//Time between attacks (ms)
    val lastAttackTime: Long = 0L,
    val pathIndex: Int = 0,
    val isAlive: Boolean = true

) {
    /*
has a very important purpose: **It defines how an `Enemy` reacts when it is attacked, usually by a `Tower`.**

### The Role of the `Enemy.takeDamage()` Function

Think of it as cause and effect in your game world:

1.  **The Cause (An Attack):** A `Tower` shoots a projectile, or a player uses a special ability.
2.  **The Effect (Receiving Damage):** The `Enemy` that was hit needs to lose health.

This function is the "effect" part. It will be called from **outside** the `Enemy` class, most likely from a `Tower` or a central `GameViewModel`.

### How It Will Be Used (Example)

Right now, your `Enemy` knows how to attack a `Tower`, but your `Tower` doesn't know how to attack an `Enemy`. You will eventually write a function in your `Tower` class that looks something like this:

**Inside your `Tower.kt` file (this is a future step for you):**

*/
    fun takeDamage(damage: Int): Enemy {
        val newHealth = (health - damage).coerceAtLeast(0)
        val shouldDie = newHealth <= 0

        println("🎯 Enemy $id: $health HP -> $newHealth HP (damage: $damage)")
        if (shouldDie) {
            println("💀 Enemy $id SHOULD DIE!")
        }

        return copy(
            health = newHealth,
            isAlive = !shouldDie  // This is the critical fix!
        )
    }

    fun moveAlongPath(path: List<Position>, deltaTime: Float): Enemy {
        if (pathIndex >= path.size - 1) return this // Already at the end of the path

        val currentTarget = path[pathIndex + 1]

        // TYPO FIX & NULL HANDLING: Changed 'distancessTo' to 'distanceTo'
        // and added the Elvis operator for null safety.
        val distance = position.distanceTo(currentTarget) ?: return this

        // Check if the enemy is close enough to the next waypoint
        if (distance < 5f) {
            // Snap to the waypoint and update the path index for the next target
            return copy(position = currentTarget, pathIndex = pathIndex + 1)
        }

        // If not at the waypoint, calculate the movement direction
        val normalizedDirection = Position(
            (currentTarget.x - position.x) / distance,
            (currentTarget.y - position.y) / distance
        )

        // Return a new Enemy instance with the updated position.
        // Your code was creating a new Position object twice, simplified it.
        return copy(
            position = Position(
                position.x + normalizedDirection.x * speed * deltaTime,
                position.y + normalizedDirection.y * speed * deltaTime
            )
        )
    }

    fun canAttack(curentTime: Long): Boolean {
        return curentTime - lastAttackTime >= firingRate && isAlive
    }

    fun attackTower(tower: Tower, currentTime: Long): Pair<Enemy, Tower> {
        if (!canAttack(currentTime)) return Pair(this, tower)
        // This line should now work if the Tower class is correct.
        val damagedTower = tower.takeDamageOnTower(damage)
        return Pair(copy(lastAttackTime = currentTime), damagedTower)
    }
}
