package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking
import cli.styles.*
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.types.boolean
import coupling.LogicalCoupler
import coupling.LogicalCoupler.ContributorPair
import fetcher.RepositoryFetcher
import io.ktor.client.plugins.*

class CoupleContributorsCommand: CliktCommand(
    name = "contributor-pairs",
    help = "Calculates the pairs of developers who most frequently contribute to the same files/modules in a GitHub repository")
{
    private val repositoryOwner: String by argument(help = "Repository owner")
    private val repositoryName: String by argument(help = "Repository name")
    private val token: String? by argument(help = "Github personal token").optional()
    private val count: Int by option(help="Number of contributor pairs to output (default 3)").int().default(3)
    private val details: Boolean by option(help="Output details while calculating coupling score").boolean().default(false)
    override fun run() {
        echo(progress("Calculating contributor pairs for repository: `$repositoryOwner/$repositoryName`"))
        runBlocking {
            val fetcher = RepositoryFetcher(repositoryOwner, repositoryName)
            if (token != null) {
                fetcher.addToken(token.toString())
            }
            try {
                val commitEntriesList = fetcher.listCommits()
                val commitList = commitEntriesList.map { commitEntry -> fetcher.getCommitFullInfo(commitEntry.sha) }
                val contributors = fetcher.getContributors()
                val logicalCoupler = LogicalCoupler(contributors)
                logicalCoupler.processCommits(commitList)
                if (details) {
                    printFileToContributorsMap(logicalCoupler)
                }
                fetcher.close()
                printReport(logicalCoupler.getContributorPairsWithHighestScore(count))
            } catch (e: RuntimeException) {
                echo(warning(e.message ?: "Exception without any message"))
            }
        }
    }

    private fun printFileToContributorsMap(logicalCoupler: LogicalCoupler) {
        echo(progress("For each file printing the number of commits made by each contributor"))
        val map = logicalCoupler.getFileToContributorsMap()
        for (entry in map.entries) {
            echo(reportProperty("`${entry.key.filename}`:"))
            for (contribution in entry.value.entries) {
                val contributorLogin = contribution.key.login
                val commitNumber = contribution.value
                echo("* $contributorLogin: $commitNumber commits made")
            }
            echo("\n")
        }
    }

    private fun printReport(result: List<ContributorPair>) {
        for (record in result) {
            val firstContributor = record.firstContributor.login
            val firstContributorUrl = record.firstContributor.url
            val secondContributor = record.secondContributor.login
            val secondContributorUrl = record.secondContributor.url
            echo("${reportProperty("First contributor:")} ${hyperlink(firstContributorUrl, firstContributor)}, ${reportProperty("Second Contributor:")} ${hyperlink(secondContributorUrl, secondContributor)}, ${reportProperty("Coupling score:")} ${record.score}")
        }
    }
}