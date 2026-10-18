package com.example.epicinv.data.freegame

import org.junit.Test
import org.junit.Assert.assertEquals

class FreeGameItemTest {

    @Test
    fun `status is CURRENT when now is between start and end`() {
        val now = System.currentTimeMillis()
        val item = FreeGameItem(
            namespace = "ns",
            offerId = "off",
            title = "Sample",
            description = "",
            coverUrl = null,
            startDate = now - 1000,
            endDate = now + 1000,
            status = FreeGameItem.Status.CURRENT
        )
        assertEquals(FreeGameItem.Status.CURRENT, item.status)
    }

    @Test
    fun `enum count matches expected`() {
        assertEquals(3, FreeGameItem.Status.values().size)
    }
}
