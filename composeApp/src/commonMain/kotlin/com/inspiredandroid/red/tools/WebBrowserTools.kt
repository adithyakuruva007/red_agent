package com.inspiredandroid.red.tools

import com.inspiredandroid.red.network.tools.ParameterSchema
import com.inspiredandroid.red.network.tools.Tool
import com.inspiredandroid.red.network.tools.ToolInfo
import com.inspiredandroid.red.network.tools.ToolSchema
import com.inspiredandroid.red.saveTempScreenshot

object WebBrowserTools {
    val browserController = BrowserController()

    val browserNavigateTool = object : Tool {
        override val schema = ToolSchema(
            name = "browser_navigate",
            description = "Navigate the browser session to a specified URL and extract the page elements.",
            parameters = mapOf(
                "url" to ParameterSchema("string", "The absolute URL to navigate to (e.g. 'https://www.google.com')", true)
            )
        )

        override suspend fun execute(args: Map<String, Any>): Any {
            val url = args["url"]?.toString() ?: return mapOf("success" to false, "error" to "url parameter is required")
            return browserController.navigate(url)
        }
    }

    val browserExtractTool = object : Tool {
        override val schema = ToolSchema(
            name = "browser_extract",
            description = "Extract the current page's metadata and interactive DOM elements (buttons, links, inputs) with temporary IDs.",
            parameters = emptyMap()
        )

        override suspend fun execute(args: Map<String, Any>): Any {
            return browserController.getPageStructure()
        }
    }

    val browserInteractTool = object : Tool {
        override val schema = ToolSchema(
            name = "browser_interact",
            description = "Interact with elements on the current webpage (click, type, or scroll).",
            parameters = mapOf(
                "action" to ParameterSchema(
                    "string",
                    "The action to execute: 'click' (to tap an element), 'type' (to enter text into an input), 'scroll_down' (to scroll down the page), or 'scroll_up' (to scroll up)",
                    true
                ),
                "elementId" to ParameterSchema(
                    "integer",
                    "The temporary integer ID of the element to interact with (required for 'click' and 'type' actions)",
                    false
                ),
                "text" to ParameterSchema(
                    "string",
                    "The text content to input (required only for 'type' action)",
                    false
                )
            )
        )

        override suspend fun execute(args: Map<String, Any>): Any {
            val action = args["action"]?.toString() ?: return mapOf("success" to false, "error" to "action parameter is required")
            return when (action.lowercase()) {
                "click" -> {
                    val elementId = (args["elementId"] as? Number)?.toInt() ?: return mapOf("success" to false, "error" to "elementId is required for click action")
                    browserController.click(elementId)
                }
                "type" -> {
                    val elementId = (args["elementId"] as? Number)?.toInt() ?: return mapOf("success" to false, "error" to "elementId is required for type action")
                    val text = args["text"]?.toString() ?: return mapOf("success" to false, "error" to "text parameter is required for type action")
                    browserController.typeText(elementId, text)
                }
                "scroll_down" -> {
                    browserController.scroll(down = true)
                }
                "scroll_up" -> {
                    browserController.scroll(down = false)
                }
                else -> {
                    mapOf("success" to false, "error" to "Unknown action: $action. Supported actions are: click, type, scroll_down, scroll_up")
                }
            }
        }
    }

    val browserScreenshotTool = object : Tool {
        override val schema = ToolSchema(
            name = "browser_screenshot",
            description = "Capture a PNG screenshot of the current browser viewport and save it as a local file, returning the absolute file path.",
            parameters = emptyMap()
        )

        override suspend fun execute(args: Map<String, Any>): Any {
            val bytes = browserController.getScreenshot() ?: return mapOf("success" to false, "error" to "Failed to capture screenshot")
            val path = saveTempScreenshot(bytes) ?: return mapOf("success" to false, "error" to "Failed to save screenshot file to disk")
            return mapOf("success" to true, "path" to path, "message" to "Screenshot captured successfully")
        }
    }

    val browserToolDefinitions = listOf(
        ToolInfo(browserNavigateTool.schema.name, "Navigate Website", "Open a website and inspect its interactive elements", null),
        ToolInfo(browserExtractTool.schema.name, "Extract Page", "Read page title, URL, and interactive elements", null),
        ToolInfo(browserInteractTool.schema.name, "Interact with Webpage", "Click buttons, enter text, or scroll the page", null),
        ToolInfo(browserScreenshotTool.schema.name, "Capture Screenshot", "View the current page visually", null)
    )
}
