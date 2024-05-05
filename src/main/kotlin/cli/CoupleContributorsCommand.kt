package cli

import RepositoryTraverser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking
import cli.styles.*

class CoupleContributorsCommand: CliktCommand(
    name = "contributor-pairs",
    help = "Calculates the pairs of developers who most frequently contribute to the same files/modules in a GitHub repository")
{
    private val repositoryOwner: String by argument(help = "Repository owner")
    private val repositoryName: String by argument(help = "Repository name")
    private val token: String by argument(help = "Github personal token")
    private val count: Int by option(help="Number of contributor pairs to output (default 3)").int().default(3)
    override fun run() {
        val repositoryTraverser = RepositoryTraverser(repositoryOwner, repositoryName, token)
        echo(progress("Calculating contributor pairs for repository: `$repositoryOwner/$repositoryName`"))
        runBlocking {
            val result = repositoryTraverser.traverse()
            for (record in result) {
                val firstContributor = record.firstContributor.login
                val firstContributorUrl = record.firstContributor.url
                val secondContributor = record.secondContributor.login
                val secondContributorUrl = record.secondContributor.url
                echo("${reportProperty("First contributor:")} ${hyperlink(firstContributorUrl, firstContributor)}, ${reportProperty("Second Contributor:")} ${hyperlink(secondContributorUrl, secondContributor)}, ${reportProperty("Coupling score:")} ${record.score}")
            }
        }
    }
}