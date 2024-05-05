package cli.styles

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles


val warning = TextColors.red
val progress = TextColors.blue + TextStyles.bold
val reportProperty = TextColors.yellow
val success = TextColors.green

fun hyperlink(destination: String, name: String): String = TextStyles.hyperlink(destination)(name)