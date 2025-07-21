package com.niktech.explain.ai

import com.niktech.explain.config.AISettings
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.core.http.StreamResponse
import com.openai.models.ChatModel
import com.openai.models.chat.completions.ChatCompletionChunk
import com.openai.models.chat.completions.ChatCompletionCreateParams


object AIClient {

    private val apiKey: String
        get() = AISettings.getInstance().state.apiKey
    fun explainCode(methodCode: String, relatedMethodSources: String, fieldInfo: String?, classAnnotations: String?): StreamResponse<ChatCompletionChunk> {

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
        val client = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build()
        val chatCompletionsParams = ChatCompletionCreateParams.builder()
            .model(ChatModel.GPT_4O)
            .addUserMessage(prompt)
            .build()
        return client.chat().completions().createStreaming(chatCompletionsParams)


    }
}