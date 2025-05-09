package net.guizhanss.guizhanlib.kt.slimefun.items.builder

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import net.guizhanss.guizhanlib.kt.common.utils.RequiredProperty
import org.bukkit.inventory.ItemStack
import java.util.logging.Level
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.full.valueParameters

/**
 * DSL class to construct a [SlimefunItem].
 *
 * Modified from [sf4k by Seggan](https://github.com/Seggan/sf4k/blob/master/src/main/kotlin/io/github/seggan/sf4k/item/builder/ItemBuilder.kt).
 */
class SlimefunItemBuilder(private val registry: ItemRegistry) {

    var id: String by RequiredProperty(setter = { it.uppercase() })
    var material: MaterialType by RequiredProperty()
    var name: String by RequiredProperty()
    var amount: Int by RequiredProperty(1)

    var itemGroup: ItemGroup by RequiredProperty()
    var recipeType: RecipeType by RequiredProperty()
    var recipe: Array<out ItemStack?> by RequiredProperty(arrayOfNulls<ItemStack>(9))

    var editItem: (SlimefunItemStack) -> SlimefunItemStack = { it }

    private val lore = mutableListOf<String>()

    operator fun String.unaryPlus() {
        lore += this
    }

    fun <T : SlimefunItem> build(clazz: KClass<T>, vararg otherArgs: Any?): SlimefunItemStack {
        // SlimefunItemStack
        val sfis = SlimefunItemStack(
            "${registry.prefix}$id",
            material.convert(),
            name,
            *lore.toTypedArray()
        )
        sfis.amount = amount

        // SlimefunItem
        val args = arrayOf(itemGroup, editItem(sfis), recipeType, recipe, *otherArgs)
        // sf4k's reflection cant find the constructor, we use our own implementation
        val constructor = clazz.constructors.firstOrNull { constructor ->
            constructor.valueParameters.size == args.size && constructor.valueParameters.zip(args).all { (param, arg) ->
                val classifier = param.type.classifier as? KClass<*> ?: return@all false
                if (arg == null) param.type.isMarkedNullable
                else classifier.isInstance(arg)
            }
        } ?: error("No constructor found for ${clazz.simpleName} with arguments: ${args.joinToString()}")

        val item = try {
            constructor.call(*args)
        } catch (e: Exception) {
            registry.addon.javaPlugin.logger.log(Level.SEVERE, e) { "Failed to create SlimefunItem" }
            throw e
        }
        item.register(registry.addon)
        return sfis
    }
}

inline fun <reified I : SlimefunItem> ItemRegistry.buildSlimefunItem(
    vararg otherArgs: Any?,
    crossinline builder: SlimefunItemBuilder.() -> Unit
) = PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, SlimefunItemStack>> { _, property ->
    val itemBuilder = SlimefunItemBuilder(this)
    itemBuilder.id = property.name.uppercase()
    itemBuilder.apply(builder)
    val item = itemBuilder.build(I::class, *otherArgs)
    ReadOnlyProperty { _, _ -> item }
}
