package me.devcexx.basiclockette

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor

val String.component get() = Component.text(this)

val TextComponent.info get() = this.color(NamedTextColor.AQUA)
val TextComponent.fine get() = this.color(NamedTextColor.GREEN)
val TextComponent.warning get() = this.color(NamedTextColor.YELLOW)
val TextComponent.error get() = this.color(NamedTextColor.RED)

val String.chatColorInfo get() = component.info
val String.chatColorFine get() = component.fine
val String.chatColorWarning get() = component.warning
val String.chatColorError get() = component.error
