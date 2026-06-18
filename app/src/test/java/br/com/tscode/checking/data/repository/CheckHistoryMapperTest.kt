package br.com.tscode.checking.data.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.api.CheckApi
import br.com.tscode.checking.data.dto.CheckAction as DtoCheckAction
import br.com.tscode.checking.data.dto.InformeType as DtoInformeType
import br.com.tscode.checking.data.dto.WebCheckHistoryItemDto
import br.com.tscode.checking.data.dto.WebCheckHistoryListResponseDto
import br.com.tscode.checking.data.remote.sse.CheckEventStream
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.InformeType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** EP2/P2.1 — CheckRepositoryImpl.getHistory DTO→domain mapping (action, local null passthrough,
 *  parsed time, informe). */
class CheckHistoryMapperTest {

    private val checkApi = mockk<CheckApi>()
    private val repo = CheckRepositoryImpl(
        checkApi,
        mockk<Clock>(relaxed = true),
        mockk<CheckEventStream>(relaxed = true),
    )

    @Test
    fun getHistory_mapsDtoToDomain_withLocationParsedTimeAndInforme() = runTest {
        coEvery { checkApi.getHistory("U3RD") } returns WebCheckHistoryListResponseDto(
            items = listOf(
                WebCheckHistoryItemDto(
                    action = DtoCheckAction.CHECKIN,
                    projeto = "P80",
                    local = "Área X",
                    time = "2026-06-15T01:00:00Z",
                    informe = DtoInformeType.NORMAL,
                ),
                WebCheckHistoryItemDto(
                    action = DtoCheckAction.CHECKOUT,
                    projeto = "P80",
                    local = null,
                    time = "2026-06-15T03:00:00Z",
                    informe = DtoInformeType.RETROATIVO,
                ),
            ),
        )

        val result = repo.getHistory("U3RD")
        assertTrue(result is AppResult.Success)
        val items = (result as AppResult.Success).data
        assertEquals(2, items.size)

        assertEquals(CheckAction.CHECKIN, items[0].action)
        assertEquals("P80", items[0].projeto)
        assertEquals("Área X", items[0].local)
        assertEquals(Instant.parse("2026-06-15T01:00:00Z"), items[0].time)
        assertEquals(InformeType.NORMAL, items[0].informe)

        assertEquals(CheckAction.CHECKOUT, items[1].action)
        assertNull(items[1].local) // null location passes through
        assertEquals(InformeType.RETROATIVO, items[1].informe)
    }

    @Test
    fun getHistory_emptyList_mapsToEmptyDomainList() = runTest {
        coEvery { checkApi.getHistory("U390") } returns WebCheckHistoryListResponseDto(items = emptyList())
        val result = repo.getHistory("U390")
        assertTrue(result is AppResult.Success)
        assertTrue((result as AppResult.Success).data.isEmpty())
    }
}
