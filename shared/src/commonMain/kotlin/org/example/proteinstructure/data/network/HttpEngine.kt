package org.example.proteinstructure.data.network

import io.ktor.client.engine.HttpClientEngine

expect fun createHttpClientEngine(): HttpClientEngine
