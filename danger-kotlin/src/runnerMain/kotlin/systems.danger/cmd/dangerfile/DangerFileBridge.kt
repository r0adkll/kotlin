package systems.danger.cmd.dangerfile

interface DangerFileBridge {
  fun execute(dangerKotlinJarOverride: String?, inputJson: String, outputJson: String)
}
