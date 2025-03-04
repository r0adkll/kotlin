package systems.danger.kotlin.models.danger

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import systems.danger.kotlin.models.bitbucket.BitBucketCloud
import systems.danger.kotlin.models.bitbucket.BitBucketServer
import systems.danger.kotlin.models.git.Git
import systems.danger.kotlin.models.github.GitHub
import systems.danger.kotlin.models.gitlab.GitLab

@Serializable internal data class DSL(val danger: DangerDSLModel)

@Serializable
data class DangerDSLModel(
  @SerialName("github") private val _github: GitHub? = null,
  @SerialName("bitbucket_server") private val _bitBucketServer: BitBucketServer? = null,
  @SerialName("bitbucket_cloud") private val _bitBucketCloud: BitBucketCloud? = null,
  @SerialName("gitlab") private val _gitlab: GitLab? = null,
  override val git: Git,
): DangerDSL {

  override val github: GitHub
    get() = _github!!

  override val bitBucketServer: BitBucketServer
    get() = _bitBucketServer!!

  override val bitBucketCloud: BitBucketCloud
    get() = _bitBucketCloud!!

  override val gitlab: GitLab
    get() = _gitlab!!

  override val onGitHub
    get() = _github != null

  override val onBitBucketServer
    get() = _bitBucketServer != null

  override val onBitBucketCloud
    get() = _bitBucketCloud != null

  override val onGitLab
    get() = _gitlab != null

  override val utils: Utils
    get() = Utils()
}

interface DangerDSL {
  val git: Git
  val github: GitHub
  val bitBucketServer: BitBucketServer
  val bitBucketCloud: BitBucketCloud
  val gitlab: GitLab
  val onGitHub: Boolean
  val onBitBucketServer: Boolean
  val onBitBucketCloud: Boolean
  val onGitLab: Boolean
  val utils: Utils
}
