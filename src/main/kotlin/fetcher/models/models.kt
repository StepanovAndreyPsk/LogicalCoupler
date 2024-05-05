package fetcher.models

import kotlinx.serialization.Serializable

@Serializable
data class Contributor(
    val login: String,
//    val id: Int,
//    val node_id: String,
//    val avatar_url: String,
//    val gravatar_id: String,
    val url: String,
//    val html_url: String,
//    val followers_url: String,
//    val following_url: String,
//    val gists_url: String,
//    val starred_url: String,
//    val subscriptions_url: String,
//    val organizations_url: String,
//    val repos_url: String,
//    val events_url: String,
//    val received_events_url: String,
//    val type: String,
//    val site_admin: Boolean,
//    val contributions: Int
)

@Serializable
data class Tree(
    val sha: String,
//    val url: String
)

@Serializable
data class Person(
    val name: String,
    val email: String,
    val date: String
)

@Serializable
data class Commit(
    val url: String,
    val author: Person,
    val committer: Person,
    val message: String,
    val tree: Tree,
    val comment_count: Int,
    val verification: Verification
)

@Serializable
data class Verification(
    val verified: Boolean,
    val reason: String,
    val signature: String?,
    val payload: String?
)

@Serializable
data class CommitFullInfo(
    val url: String,
    val sha: String,
    val node_id: String,
    val html_url: String,
    val comments_url: String,
    val commit: Commit,
    val author: Contributor,
    val files: List<File>
)

@Serializable
data class CommitEntry(
    val sha: String
)

@Serializable
data class Stats(
    val additions: Int,
    val deletions: Int,
    val total: Int
)

@Serializable
data class Branch(
    val name: String,
    val commit: CommitRef,
    val protected: Boolean
)

@Serializable
data class CommitRef(
    val sha: String,
    val url: String
)

@Serializable
data class ParentRef(
    val url: String,
    val sha: String
)

@Serializable
data class TreeEntry(
    val path: String,
    val mode: String,
    val type: String
)

@Serializable
data class File(
    val filename: String,
    // other properties
)