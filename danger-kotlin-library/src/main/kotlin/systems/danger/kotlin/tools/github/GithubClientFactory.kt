package systems.danger.kotlin.tools.github

import com.spotify.github.v3.clients.GitHubClient
import java.net.URI

interface GithubClientFactory {

  fun create(): GitHubClient

  companion object {
    /** Override this to provide your own instantiation of the [GitHubClient] */
    var instance: GithubClientFactory = DefaultGithubClientFactory
  }
}

object DefaultGithubClientFactory : GithubClientFactory {

  private const val DEFAULT_GITHUB_URL = "https://api.github.com"

  override fun create(): GitHubClient {
    System.getenv()

    return GitHubClient.create(getBaseUrl(), getToken())
  }

  private fun getToken(): String {
    return System.getenv("DANGER_GITHUB_API_TOKEN")
      ?: System.getenv("GITHUB_TOKEN")
      ?: error(
        "Unable to find GitHub API token. Please set the envvar 'DANGER_GITHUB_API_TOKEN' or 'GITHUB_TOKEN'"
      )
  }

  private fun getBaseUrl(): URI {
    return URI.create(System.getenv("DANGER_GITHUB_API_BASE_URL") ?: DEFAULT_GITHUB_URL)
  }
}
