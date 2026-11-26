package ai.cybnate.ui.miniapps

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ai.cybnate.data.model.MiniApp
import ai.cybnate.data.terminal.ShellExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MiniAppWebViewer(
    app: MiniApp,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shellExecutor = ShellExecutor()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.name, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()

                    // Secure Mini-App Bridge
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun executeCommand(command: String, callbackJs: String) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = shellExecutor.execute(command)
                                post {
                                    val safeOutput = result.output.replace("'", "\\'")
                                        .replace("\n", "\\n")
                                    evaluateJavascript("$callbackJs('$safeOutput', ${result.exitCode});", null)
                                }
                            }
                        }

                        @JavascriptInterface
                        fun getAppName(): String = app.name
                    }, "CybNateBridge")

                    loadDataWithBaseURL(
                        null,
                        """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <style>
                                body { font-family: sans-serif; background: #0D1117; color: #F3F4F6; padding: 20px; }
                                .card { background: #161B22; border-radius: 12.dp; padding: 16px; margin-top: 10px; }
                                button { background: #3B82F6; color: white; border: none; padding: 10px 16px; border-radius: 8px; font-weight: bold; }
                                pre { background: #010409; color: #00FF66; padding: 12px; border-radius: 8px; overflow-x: auto; }
                            </style>
                        </head>
                        <body>
                            <h2>${app.iconEmoji} ${app.name}</h2>
                            <p>${app.description}</p>
                            <div class="card">
                                <h3>Device Diagnostics</h3>
                                <button onclick="runDiag()">Run Quick Diagnostic</button>
                                <pre id="output">Waiting for user action...</pre>
                            </div>
                            <script>
                                function runDiag() {
                                    document.getElementById('output').innerText = 'Running diagnostics...';
                                    if (window.CybNateBridge) {
                                        window.CybNateBridge.executeCommand('uname -a && uptime && df -h /data', 'handleResult');
                                    } else {
                                        document.getElementById('output').innerText = 'Bridge not available in browser mode.';
                                    }
                                }
                                function handleResult(output, exitCode) {
                                    document.getElementById('output').innerText = output;
                                }
                            </script>
                        </body>
                        </html>
                        """.trimIndent(),
                        "text/html",
                        "utf-8",
                        null
                    )
                }
            }
        )
    }
}
