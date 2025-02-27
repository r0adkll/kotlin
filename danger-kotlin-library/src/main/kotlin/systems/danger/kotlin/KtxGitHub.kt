package systems.danger.kotlin

import com.spotify.github.v3.clients.GitHubClient
import systems.danger.kotlin.models.github.GitHub
import systems.danger.kotlin.tools.github.GithubClientFactory

// extensions over [GitHub] object

/**
 * Get a hydrated instance of [GitHubClient] to use
 */
val GitHub.api: GitHubClient by lazy {
  GithubClientFactory.instance.create()
}
