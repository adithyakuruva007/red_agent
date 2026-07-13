package com.inspiredandroid.red.tools

import com.inspiredandroid.red.contacts.ContactsReader
import com.inspiredandroid.red.data.ContactRecord
import com.inspiredandroid.red.network.tools.ParameterSchema
import com.inspiredandroid.red.network.tools.Tool
import com.inspiredandroid.red.network.tools.ToolInfo
import com.inspiredandroid.red.network.tools.ToolSchema
import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.tool_search_contacts_description
import red.composeapp.generated.resources.tool_search_contacts_name

object ContactsTools {

    private fun summary(record: ContactRecord): Map<String, Any?> = mapOf(
        "id" to record.id,
        "name" to record.displayName,
        "phone_numbers" to record.phoneNumbers,
        "emails" to record.emails,
    )

    fun searchContactsTool(reader: ContactsReader) = object : Tool {
        override val schema = ToolSchema(
            name = "search_contacts",
            description = "Search your phone contacts by name. Returns the matching contacts with their phone numbers and emails.",
            parameters = mapOf(
                "query" to ParameterSchema(
                    type = "string",
                    description = "Name search query to search contacts. Leave empty to list contacts.",
                    required = false,
                ),
            ),
        )

        override suspend fun execute(args: Map<String, Any>): Any {
            if (!reader.isSupported()) {
                return mapOf("success" to false, "error" to "Contacts reading is not available on this build")
            }
            if (!reader.hasPermission()) {
                return mapOf(
                    "success" to false,
                    "error" to "Contacts permission not granted. Ask the user to enable Contacts access in Settings → Agent.",
                )
            }
            val query = args["query"]?.toString().orEmpty().trim()
            val matches = reader.search(query, 50)
            return mapOf(
                "success" to true,
                "count" to matches.size,
                "contacts" to matches.map(::summary),
            )
        }
    }

    val searchContactsToolInfo = ToolInfo(
        id = "search_contacts",
        name = "Search Contacts",
        description = "Search phone contacts by name",
        nameRes = Res.string.tool_search_contacts_name,
        descriptionRes = Res.string.tool_search_contacts_description,
    )

    val contactsToolDefinitions = listOf(
        searchContactsToolInfo,
    )

    fun getContactsTools(reader: ContactsReader): List<Tool> = listOf(
        searchContactsTool(reader),
    )
}
