package com.niktech.explain.data

data class MethodInfo(
    val methodText: String,
    val relatedMethodSources: String,
    val fieldInfo: String?,
    val classAnnotations: String?,
    val methodName: String
)