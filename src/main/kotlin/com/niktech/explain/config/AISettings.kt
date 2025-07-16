package com.niktech.explain.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "AISettings", storages = [Storage("AISettings.xml")])
class AISettings() : PersistentStateComponent<AISettings.State> {
    data class State(var apiKey: String = "")

    private var state = State()

    override fun getState(): State = state
    override fun loadState(state: State) { this.state = state }

    companion object {
        fun getInstance(): AISettings =
            com.intellij.openapi.application.ApplicationManager.getApplication().getService(AISettings::class.java)
    }
}