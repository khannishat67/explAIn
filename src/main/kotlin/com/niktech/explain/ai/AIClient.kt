package com.niktech.explain.ai

import com.niktech.explain.config.AISettings
import io.github.oshai.kotlinlogging.KotlinLogging
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


object AIClient {
    private val logger = KotlinLogging.logger {  }

    private val apiKey: String
        get() = AISettings.getInstance().state.apiKey
    fun explainCode(methodCode: String, relatedMethodSources: String, fieldInfo: String?, classAnnotations: String?): String {

        val prompt = """
You are analyzing a Java method and its related code.

Main method:
${methodCode}

Related methods:
$relatedMethodSources

Class fields:
$fieldInfo

Class annotations:
$classAnnotations

Explain the main method clearly, describing how the related methods are involved.
""".trimIndent()
        val body = JSONObject()
        body.put("model", "gpt-4")
        body.put("messages", JSONArray().put(
            JSONObject().put("role", "user").put("content", prompt)
        ))

        val conn = URL("https://api.openai.com/v1/chat/completions").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        conn.outputStream.use { it.write(body.toString().toByteArray()) }

        val response = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(response)
        return json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")

    }
}