@file:Suppress("unused", "deprecation")

package net.guizhanss.guizhanlib.kt.minecraft.extensions

import net.guizhanss.guizhanlib.kt.common.extensions.valueOfOrNull
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import java.util.Locale
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.toMap
import kotlin.text.uppercase

/**
 * Loads a map from the [ConfigurationSection].
 */
inline fun <reified K, reified V> ConfigurationSection?.loadMap(
    keyParser: (String) -> K? = { key -> key as? K },
    valueParser: (Any?) -> V? = { value -> value as? V },
    valuePredicate: (V) -> Boolean = { true }
): Map<K, V> {
    if (this == null) return emptyMap()

    val result = mutableMapOf<K, V>()
    getKeys(false).forEach { keyStr ->
        val key = keyParser(keyStr) ?: return@forEach
        val value = valueParser(this[keyStr]) ?: return@forEach
        if (!valuePredicate(value)) return@forEach
        result[key] = value
    }
    return result.toMap()
}

/**
 * Loads a map from the [ConfigurationSection] with enum keys.
 */
inline fun <reified K : Enum<K>, reified V> ConfigurationSection?.loadEnumKeyMap(
    valueParser: (Any?) -> V? = { value -> value as? V },
    valuePredicate: (V) -> Boolean = { true }
): Map<K, V> = loadMap(
    keyParser = { keyStr -> valueOfOrNull<K>(keyStr.uppercase(Locale.getDefault())) },
    valueParser,
    valuePredicate
)

fun ConfigurationSection?.loadIntMap(valuePredicate: (Int) -> Boolean = { true }) =
    loadMap<String, Int>(valuePredicate = valuePredicate)

fun ConfigurationSection?.loadDoubleMap(valuePredicate: (Double) -> Boolean = { true }) =
    loadMap<String, Double>(valuePredicate = valuePredicate)

fun ConfigurationSection?.loadBooleanMap() = loadMap<String, Boolean>()

fun ConfigurationSection?.loadStringMap(valuePredicate: (String) -> Boolean = { true }) =
    loadMap<String, String>(valueParser = { it.toString() }, valuePredicate = valuePredicate)

fun ConfigurationSection?.loadSectionMap() = loadMap<String, ConfigurationSection>()

fun ConfigurationSection?.loadEnchantmentKeyMap() =
    loadMap<Enchantment, Int>({ key -> Enchantment.getByName(key) }, valuePredicate = { it >= 1 })

