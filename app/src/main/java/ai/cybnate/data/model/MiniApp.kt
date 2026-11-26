package ai.cybnate.data.model

data class MiniApp(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String = "⚡",
    val entryHtmlPath: String, // local path or asset
    val version: String = "1.0.0",
    val author: String = "Community",
    val permissions: List<String> = emptyList()
)
