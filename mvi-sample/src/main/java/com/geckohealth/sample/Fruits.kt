package io.prefanatic.sample

import java.io.Serializable
import java.util.*

/**
 * Created by cgoldberg02 on 3/22/17.
 */
data class Fruit(
        val uuid: UUID = UUID.randomUUID(),
        var name: String = "Unknown"
) : Serializable

object Fruits {
    val fruits = ArrayList<Fruit>()

    fun add(name: String, init: Fruit.() -> Unit = {}) {
        val fruit = Fruit(name = name)
        fruit.init()

        fruits.add(fruit)
    }

    fun add(init: Fruit.() -> Unit) {
        val fruit = Fruit()
        fruit.init()

        fruits.add(fruit)
    }

    init {
        add {
            name = "Apple"
        }

        add {
            name = "Banana"
        }

        add {
            name = "Grape"
        }

        add("Pear")
        add("Orange")
        add("Pineapple")
        add("Dragon Fruit")
    }
}



