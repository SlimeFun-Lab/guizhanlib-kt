@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.slimefun.items.builder

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap
import net.guizhanss.guizhanlib.kt.slimefun.items.toItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Build a size*size recipe (default 3x3).
 *
 * Modified from [sf4k by Seggan](https://github.com/Seggan/sf4k/blob/master/src/main/kotlin/io/github/seggan/sf4k/item/builder/RecipeBuilder.kt),
 * for Slimefun Dev/Experimental version compatibility.
 */
class RecipeBuilder(private val size: Int) {

    private val recipe: Array<String> = Array(size) { " ".repeat(size) }
    private var row = 0

    private val charMap = Char2ObjectOpenHashMap<ItemStack?>().apply {
        put(' ', null)
    }

    /**
     * Adds a recipe row
     */
    operator fun String.unaryPlus() {
        require(length == size) { "Recipe must be ${size}x${size}" }
        require(row < size) { "Recipe must be ${size}x${size}" }
        recipe[row++] = this
    }

    /**
     * Specifies that the given character means a certain [SlimefunItemStack]
     */
    infix fun Char.means(item: SlimefunItemStack?) {
        charMap.put(this, item?.toItem())
    }

    /**
     * Specifies that the given character means a certain [ItemStack]
     */
    infix fun Char.means(item: ItemStack?) {
        charMap.put(this, item)
    }

    /**
     * Specifies that the given character means a certain [Material]
     */
    infix fun Char.means(material: Material) {
        charMap.put(this, ItemStack(material))
    }

    fun build(): Array<out ItemStack?> {
        return recipe.flatMap { row -> row.map { charMap[it] } }.toTypedArray()
    }
}

@OptIn(ExperimentalContracts::class)
inline fun buildRecipe(size: Int = 3, init: RecipeBuilder.() -> Unit): Array<out ItemStack?> {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return RecipeBuilder(size).apply(init).build()
}
