import cli.CoupleContributorsCommand
import cli.LogicCouplerCli
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal

fun main(args: Array<String>) =
        LogicCouplerCli().context {
                terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true)
        }.subcommands(CoupleContributorsCommand()).main(args)
