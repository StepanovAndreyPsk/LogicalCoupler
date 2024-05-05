package fetcher

import fetcher.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal class RepositoryFetcher(private val repositoryOwner: String, private val repositoryName: String) {
    private var personalToken: String? = null

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
            expectSuccess = true
        }
    }

    suspend fun listBranches(): List<Branch> =
        client.get("$api/repos/$repositoryOwner/$repositoryName/branches") {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github+json")
                if (!personalToken.isNullOrEmpty()) {
                    append(HttpHeaders.Authorization, "Bearer $personalToken")
                }
            }
        }.body()

    suspend fun listFilesFromBranch(branchName: String): List<TreeEntry> =
        client.get("$api/repos/$repositoryOwner/$repositoryName/git/trees/$branchName") {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github+json")
                if (!personalToken.isNullOrEmpty()) {
                    append(HttpHeaders.Authorization, "Bearer $personalToken")
                }
            }
            parameters {
                append("recursive", "1")
            }
        }.body()

    suspend fun getContributors(): List<Contributor> {
         try {
            return client.get("$api/repos/$repositoryOwner/$repositoryName/contributors") {
                headers {
                    append(HttpHeaders.Accept, "application/vnd.github+json")
                    if (!personalToken.isNullOrEmpty()) {
                        append(HttpHeaders.Authorization, "Bearer $personalToken")
                    }
                }
            }.body()
        } catch (e: ClientRequestException) {
            throw handleRequestException(e)
        }
    }

    suspend fun listCommits(): List<CommitEntry> {
        try {
            return client.get("$api/repos/$repositoryOwner/$repositoryName/commits") {
                headers {
                    append(HttpHeaders.Accept, "application/vnd.github+json")
                    if (!personalToken.isNullOrEmpty()) {
                        append(HttpHeaders.Authorization, "Bearer $personalToken")
                    }
                }
            }.body()
        } catch (e: ClientRequestException) {
            throw handleRequestException(e)
        }
    }

    suspend fun getCommitFullInfo(commitSha: String): CommitFullInfo {
        try {
            return client.get("$api/repos/$repositoryOwner/$repositoryName/commits/$commitSha") {
                headers {
                    append(HttpHeaders.Accept, "application/vnd.github+json")
                    if (!personalToken.isNullOrEmpty()) {
                        append(HttpHeaders.Authorization, "Bearer $personalToken")
                    }
                }
            }.body()
        } catch(e: ClientRequestException) {
            throw handleRequestException(e)
        }
    }

    fun close() = client.close()
    fun addToken(token: String) {
        personalToken = token
    }

    private fun handleRequestException(e: ResponseException): RuntimeException {
        return when(e.response.status) {
            HttpStatusCode.Unauthorized -> RuntimeException("Bad credentials. Please check if provided token is valid and you have access to the repository")
            HttpStatusCode.NotFound -> RuntimeException("Repository not found. Please check that you specified an existing repository name and a valid owner name")
            else -> RuntimeException(e.message)
        }
    }

    companion object {
        const val api = "https://api.github.com"
    }
}