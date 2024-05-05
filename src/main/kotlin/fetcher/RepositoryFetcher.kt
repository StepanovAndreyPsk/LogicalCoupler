package fetcher

import fetcher.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal class RepositoryFetcher(private val repositoryOwner: String, private val repositoryName: String, val token: String) {
    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        coerceInputValues = true
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }

    suspend fun listBranches(): List<Branch> =
        client.get("$api/repos/$repositoryOwner/$repositoryName/branches") {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github+json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()

    suspend fun listFilesFromBranch(branchName: String): List<TreeEntry> =
        client.get("$api/repos/$repositoryOwner/$repositoryName/git/trees/$branchName") {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github+json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            parameters {
                append("recursive", "1")
            }
        }.body()

    suspend fun getContributors(): List<Contributor> =
        client.get("$api/repos/$repositoryOwner/$repositoryName/contributors") {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github+json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()

    suspend fun listCommits(): List<CommitEntry> =
        client.get("$api/repos/$repositoryOwner/$repositoryName/commits") {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github+json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()

    suspend fun getCommitFullInfo(commitSha: String): CommitFullInfo =
        client.get("$api/repos/$repositoryOwner/$repositoryName/commits/$commitSha") {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github+json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()

    fun close() = client.close()

    companion object {
        const val api = "https://api.github.com"
    }
}