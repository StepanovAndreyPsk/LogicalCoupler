import coupling.LogicalCoupler
import fetcher.RepositoryFetcher

class RepositoryTraverser(private val repositoryOwner: String, private val repositoryName: String, private val token: String) {
    suspend fun traverse(): List<LogicalCoupler.ContributorPair> {
        val fetcher = RepositoryFetcher(repositoryOwner, repositoryName, token)
        val commitEntriesList = fetcher.listCommits()
        val commitList = commitEntriesList.map { commitEntry -> fetcher.getCommitFullInfo(commitEntry.sha) }
        val contributors = fetcher.getContributors()
        val logicalCoupler = LogicalCoupler(contributors)
        logicalCoupler.processCommits(commitList)
        fetcher.close()
        return logicalCoupler.getContributorPairsWithHighestScore(3)
    }
}