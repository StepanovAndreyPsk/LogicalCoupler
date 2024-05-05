package coupling

import fetcher.models.CommitFullInfo
import fetcher.models.Contributor
import fetcher.models.File
import kotlin.math.min

class LogicalCoupler(contributors: List<Contributor>) {
    private val couplingScore: MutableMap<Contributor, MutableMap<Contributor, Int>> = mutableMapOf()
    // coupling score [Bob][Alice] -- how many commits Alice made in the same file as Bob

    private val fileToContributionsMap: MutableMap<File, MutableMap<Contributor, Int>> = mutableMapOf()

    init {
        for (contributor in contributors) {
            couplingScore[contributor] = mutableMapOf()
            for (other in contributors) {
                couplingScore[contributor]?.put(other, 0)
            }
        }
    }

    fun processCommits(commits: List<CommitFullInfo>) {
        for (commit in commits) {
            val author = commit.author
            val files = commit.files
            for (file in files) {
                if (fileToContributionsMap.containsKey(file)) {
                    if (fileToContributionsMap[file]?.containsKey(author) == true) {
                        fileToContributionsMap[file]?.get(author)?.inc()
                    }
                    else {
                        fileToContributionsMap[file]?.put(author, 1)
                    }
                }
                else {
                    fileToContributionsMap[file] = mutableMapOf(author to 1)
                }
            }
        }
    }

    private fun calculateCouplingScore() {
        for (fileEntry in fileToContributionsMap.entries) {
            for (contributionEntry in fileEntry.value) {
                for (other in fileEntry.value) {
                    if (fileEntry == other)
                        continue
                    val contributorA = contributionEntry.key
                    val contributorB = other.key
                    if (contributorA == contributorB)
                        continue
                    val score = min(contributionEntry.value, other.value)
                    val oldValue: Int? = couplingScore[contributorA]?.get(contributorB)
                    assert(oldValue != null)
                    oldValue?.plus(score)?.let {newVal ->
                        couplingScore[contributorA]?.put(contributorB, newVal)
                        couplingScore[contributorB]?.put(contributorA, newVal)
                    }
                }
            }
        }
    }

    fun getContributorPairsWithHighestScore(count: Int): List<ContributorPair> {
        calculateCouplingScore()
        val contributorPairSet = mutableSetOf<ContributorPair>()
        val couplingScoreList = couplingScore.entries.toList()
        for (i in 0..<couplingScoreList.size) {
            val contributorA = couplingScoreList[i].key
            val innerMapList = couplingScoreList[i].value.entries.toList()
            for (j in i+1..<innerMapList.size) {
                val contributorB = innerMapList[j].key
                val score = innerMapList[j].value
                val contributorPair = ContributorPair(contributorA, contributorB, score)
                contributorPairSet.add(contributorPair)
            }
        }

        val result = contributorPairSet.toMutableList()
        result.sortDescending()
        require(count <= result.size) { "requested more pairs than present" }
        return result.subList(0, count)
    }

    data class ContributorPair(
        val firstContributor: Contributor,
        val secondContributor: Contributor,
        val score: Int
    ) : Comparable<ContributorPair> {
        override fun compareTo(other: ContributorPair): Int = this.score.compareTo(other.score)
    }
}