package ir.syrent.velocityreport.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

val miniMessage = MiniMessage.miniMessage()

fun String.component(): Component {
    return miniMessage.deserialize(this)
}

fun String.component(vararg tags: TagResolver): Component {
    return miniMessage.deserialize(this, *tags)
}