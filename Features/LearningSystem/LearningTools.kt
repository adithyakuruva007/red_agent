package com.inspiredandroid.red.tools

import com.inspiredandroid.red.data.AppSettings
import com.inspiredandroid.red.data.MemoryCategory
import com.inspiredandroid.red.data.MemoryStore
import com.inspiredandroid.red.network.tools.ParameterSchema
import com.inspiredandroid.red.network.tools.Tool
import com.inspiredandroid.red.network.tools.ToolSchema

object LearningTools {

    /**
     * Storing learnings, error resolutions, or preferences.
     */
    fun memoryLearnTool(memoryStore: MemoryStore) = object : Tool {
        override val schema = ToolSchema(
            name = "memory_learn",
            description = "Store a structured learning with a category. Use LEARNING for things that worked, ERROR for error resolutions, PREFERENCE for user corrections/preferences.",
            parameters = mapOf(
                "key" to ParameterSchema(type = "string", description = "Descriptive key for the learning", required = true),
                "content" to ParameterSchema(type = "string", description = "What was learned", required = true),
                "category" to ParameterSchema(type = "string", description = "Category: LEARNING, ERROR, or PREFERENCE", required = true),
                "source" to ParameterSchema(type = "string", description = "How this was learned: user_correction, observation, or error_resolution", required = false),
            ),
        )

        override suspend fun execute(args: Map<String, Any>): Any {
            val key = args["key"]?.toString() ?: return mapOf("success" to false, "error" to "Missing key")
            val content = args["content"]?.toString() ?: return mapOf("success" to false, "error" to "Missing content")
            val categoryStr = args["category"]?.toString() ?: return mapOf("success" to false, "error" to "Missing category")
            val source = args["source"]?.toString()

            val category = try {
                MemoryCategory.valueOf(categoryStr.uppercase())
            } catch (e: Exception) {
                return mapOf("success" to false, "error" to "Invalid category: $categoryStr. Use LEARNING, ERROR, or PREFERENCE")
            }

            if (category == MemoryCategory.GENERAL) {
                return mapOf("success" to false, "error" to "Use memory_store for GENERAL memories. memory_learn is for LEARNING, ERROR, or PREFERENCE")
            }

            val entry = memoryStore.store(key, content, category, source)
            return mapOf(
                "success" to true,
                "key" to entry.key,
                "category" to entry.category.name,
                "message" to "Learning stored successfully.",
            )
        }
    }

    /**
     * Increments the hit count of a memory when it produces a good outcome.
     */
    fun memoryReinforceTool(memoryStore: MemoryStore) = object : Tool {
        override val schema = ToolSchema(
            name = "memory_reinforce",
            description = "Reinforce a stored memory by incrementing its hit count. Use this when a stored learning or preference produced a good outcome.",
            parameters = mapOf(
                "key" to ParameterSchema(type = "string", description = "The key of the memory to reinforce", required = true),
            ),
        )

        override suspend fun execute(args: Map<String, Any>): Any {
            val key = args["key"]?.toString() ?: return mapOf("success" to false, "error" to "Missing key")
            val entry = memoryStore.reinforceMemory(key) ?: return mapOf("success" to false, "error" to "Memory not found: $key")
            return mapOf(
                "success" to true,
                "key" to entry.key,
                "hit_count" to entry.hitCount,
                "message" to "Memory reinforced successfully.",
            )
        }
    }

    /**
     * Promotes a well-established memory to the system prompt and deletes it from memories.
     */
    fun promoteLearningTool(memoryStore: MemoryStore, appSettings: AppSettings) = object : Tool {
        override val schema = ToolSchema(
            name = "promote_learning",
            description = "Promote a well-established memory into the soul/system prompt. Use this for patterns that have been reinforced multiple times and should become permanent behavior.",
            parameters = mapOf(
                "memory_key" to ParameterSchema(type = "string", description = "The key of the memory to promote", required = true),
                "soul_addition" to ParameterSchema(type = "string", description = "The text to append to the soul/system prompt", required = true),
            ),
        )

        override suspend fun execute(args: Map<String, Any>): Any {
            val memoryKey = args["memory_key"]?.toString()
                ?: return mapOf("success" to false, "error" to "Missing memory_key")
            val soulAddition = args["soul_addition"]?.toString()
                ?: return mapOf("success" to false, "error" to "Missing soul_addition")

            val memories = memoryStore.getAllMemories()
            val memory = memories.find { it.key == memoryKey }
                ?: return mapOf("success" to false, "error" to "Memory not found: $memoryKey")

            val currentSoul = appSettings.getSoulText()
            val newSoul = if (currentSoul.isEmpty()) {
                soulAddition
            } else {
                "$currentSoul\n\n$soulAddition"
            }
            appSettings.setSoulText(newSoul)
            memoryStore.forget(memoryKey)

            return mapOf(
                "success" to true,
                "promoted_key" to memoryKey,
                "hit_count" to memory.hitCount,
                "message" to "Memory promoted to soul. Original memory removed.",
            )
        }
    }
}
