package com.r0adkll.danger.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import git4idea.GitBranch
import git4idea.remote.hosting.GitHostingUrlUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import org.jetbrains.plugins.github.api.GHRepositoryCoordinates
import org.jetbrains.plugins.github.api.GithubApiRequestExecutor
import org.jetbrains.plugins.github.api.GithubApiRequests
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.api.data.GithubIssueState
import org.jetbrains.plugins.github.authentication.GHAccountsUtil
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount
import org.jetbrains.plugins.github.util.GHCompatibilityUtil
import org.jetbrains.plugins.github.util.GithubUrlUtil

fun Project.gitHubService(): GithubService = service()

@Service(Service.Level.PROJECT)
class GithubService(private val project: Project) : CiProvider {

  private val pullRequestUrls = mutableMapOf<GitBranch, String>()

  override suspend fun isAvailable(): Boolean =
    withContext(Dispatchers.IO) { GHAccountsUtil.getSingleOrDefaultAccount(project) != null }

  override suspend fun findCurrentPullRequestUrl(): String? {
    val ghAccount = getMatchingGithubAccount()
    if (ghAccount == null) {
      thisLogger().warn("No Github account found! Can't load PR information to Danger against.")

      GHAccountsUtil.accounts.forEach { acct ->
        thisLogger().info("Github Account: ${acct.name} --> ${acct.server.toUrl()}")
      }

      return null
    }

    val repo = project.gitService().currentGitRepository() ?: return null
    val trackedBranch = repo.currentBranch?.findTrackedBranch(repo) ?: return null
    val remoteUrl = trackedBranch.remote.firstUrl ?: return null

    // Check if we have already cached the url for this branch
    val existing = pullRequestUrls[trackedBranch]
    if (existing != null) {
      return existing
    }

    val host = getHostFromUrl(remoteUrl)
    val ghServerPath = GithubServerPath(host)
    val ghRepoPath = GithubUrlUtil.getUserAndRepositoryFromRemoteUrl(remoteUrl)
    val repoCoordinates =
      ghRepoPath?.let { GHRepositoryCoordinates(ghServerPath, it) } ?: return null

    thisLogger()
      .info(
        """
          Repo Path Info:
            remoteUrl = $remoteUrl,
            serverPath = $ghServerPath,
            serverPath.toUrl = ${ghServerPath.toUrl()},
            serverPath.toApiUrl = ${ghServerPath.toApiUrl()},
            repoPath = $ghRepoPath,
            repoCoordinates = $repoCoordinates
            headRef = ${trackedBranch.nameForRemoteOperations},
        """
          .trimIndent()
      )

    val request =
      GithubApiRequests.Repos.PullRequests.find(
        repository = repoCoordinates,
        state = GithubIssueState.open,
        headRef = "${repoCoordinates.repositoryPath.owner}:${trackedBranch.nameForRemoteOperations}",
      )

    val token = GHCompatibilityUtil.getOrRequestToken(ghAccount, project)
    if (token == null) {
      thisLogger().warn("GH account, ${ghAccount.name}, is not logged in. Can't fetch current PR")
      return null
    }

    val apiExecutor = GithubApiRequestExecutor.Factory.getInstance().create(ghServerPath, token)

    thisLogger()
      .info(
        "Searching pull request @ ${request.url}, for ${trackedBranch.nameForRemoteOperations}"
      )

    return withContext(Dispatchers.IO) {
      try {
        val response = apiExecutor.execute(request)
        val pullRequestId = response.items.firstOrNull()?.number
        if (pullRequestId != null) {
          // attempt to construct the PR url from the found number, the Github plugin keeps the API
          // for pulling
          // PR details internal, so this looks like as far as we are going to get
          val prUrl = "${ghServerPath.toUrl()}/$ghRepoPath/pull/$pullRequestId"
          pullRequestUrls[trackedBranch] = prUrl
          return@withContext prUrl
        } else {
          this@GithubService.thisLogger()
            .warn("Unable to find matching pull request for HEAD ${trackedBranch.fullName}")
        }
      } catch (e: IOException) {
        this@GithubService.thisLogger().error("Unable to fetch PullRequest", e)
      }
      null
    }
  }

  override suspend fun runEnvironment(): Map<String, String> =
    withContext(Dispatchers.IO) {
      val ghAccount = getMatchingGithubAccount() ?: return@withContext emptyMap()
      val token =
        GHCompatibilityUtil.getOrRequestToken(ghAccount, project) ?: return@withContext emptyMap()

      mapOf(
        "DANGER_GITHUB_API_TOKEN" to token,
        "DANGER_GITHUB_HOST" to ghAccount.server.toUrl(),
        "DANGER_GITHUB_API_BASE_URL" to ghAccount.server.toApiUrl(),
        "CI_PROVIDER" to "Github"
      )
    }

  /** Find the matching [GithubAccount] for the current tracked branch */
  private suspend fun getMatchingGithubAccount(): GithubAccount? {
    val repo = project.gitService().currentGitRepository() ?: return null
    val remoteUrl = repo.remotes.firstOrNull()?.firstUrl ?: return null

    val host = getHostFromUrl(remoteUrl)
    val ghServerPath = GithubServerPath(host)

    return GHAccountsUtil.accounts.find { acct -> acct.server.host == ghServerPath.host }
  }

  /** E.g.: https://github.com/suffix/ -> github.com github.com:8080/ -> github.com */
  private fun getHostFromUrl(url: String): String {
    val path: String = GitHostingUrlUtil.removeProtocolPrefix(url).replace(':', '/')
    val index = path.indexOf('/')
    return if (index == -1) {
      path
    } else {
      path.substring(0, index)
    }
  }
}
