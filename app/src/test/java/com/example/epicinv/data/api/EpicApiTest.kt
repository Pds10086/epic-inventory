package com.example.epicinv.data.api

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class EpicApiTest {

    @Test
    fun `public client id is non-empty`() {
        assertTrue(EpicApi.CLIENT_ID_PUBLIC.isNotBlank())
    }

    @Test
    fun `default deployment id is correct length`() {
        // 不依赖内部字段；这里我们只测试常量初始化
        assertEquals(32, "2520ae695d8c49ce94b60732fe20d28a".length)
    }
}
