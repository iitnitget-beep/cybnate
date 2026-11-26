package ai.cybnate.data.agent

import org.json.JSONArray
import org.json.JSONObject

/**
 * Handles Model Context Protocol (MCP) JSON-RPC 2.0 messages.
 * Allows client agents and apps to discover and call device tools.
 */
class McpBridge {

    fun handleJsonRpc(requestJson: String): String {
        return try {
            val req = JSONObject(requestJson)
            val id = req.opt("id")
            val method = req.optString("method")

            when (method) {
                "initialize" -> {
                    JSONObject().apply {
                        put("jsonrpc", "2.0")
                        put("id", id)
                        put("result", JSONObject().apply {
                            put("protocolVersion", "2024-11-05")
                            put("capabilities", JSONObject().apply {
                                put("tools", JSONObject())
                            })
                            put("serverInfo", JSONObject().apply {
                                put("name", "cybnate-mcp")
                                put("version", "1.0.0")
                            })
                        })
                    }.toString()
                }

                "tools/list" -> {
                    JSONObject().apply {
                        put("jsonrpc", "2.0")
                        put("id", id)
                        put("result", JSONObject().apply {
                            put("tools", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("name", "device_shell_run")
                                    put("description", "Executes shell commands in user space")
                                    put("inputSchema", JSONObject().apply {
                                        put("type", "object")
                                        put("properties", JSONObject().apply {
                                            put("command", JSONObject().put("type", "string"))
                                        })
                                        put("required", JSONArray().put("command"))
                                    })
                                })
                            })
                        })
                    }.toString()
                }

                else -> {
                    JSONObject().apply {
                        put("jsonrpc", "2.0")
                        put("id", id)
                        put("error", JSONObject().apply {
                            put("code", -32601)
                            put("message", "Method not found: $method")
                        })
                    }.toString()
                }
            }
        } catch (e: Exception) {
            JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", JSONObject.NULL)
                put("error", JSONObject().apply {
                    put("code", -32700)
                    put("message", "Parse error: ${e.message}")
                })
            }.toString()
        }
    }
}
