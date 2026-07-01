package com.ownstd.project.pincard

import androidx.compose.ui.graphics.ImageBitmap
import com.ownstd.project.pincard.internal.data.model.Clothe
import com.ownstd.project.pincard.internal.domain.repository.WardrobeRepository
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ---------------------------------------------------------------------------
// Fake Repository — имитирует серверную фильтрацию по occasion
// ---------------------------------------------------------------------------
private class FakeWardrobeRepository : WardrobeRepository {

    // Полный набор тестовых данных (то, что "хранится на сервере")
    private val database = listOf(
        Clothe(id = 1, name = "Белая рубашка",   imageUrl = "img1", storeUrl = "", occasion = "office"),
        Clothe(id = 2, name = "Деловой пиджак",   imageUrl = "img2", storeUrl = "", occasion = "office"),
        Clothe(id = 3, name = "Кроссовки Nike",   imageUrl = "img3", storeUrl = "", occasion = "sport"),
        Clothe(id = 4, name = "Спортивные штаны", imageUrl = "img4", storeUrl = "", occasion = "sport"),
        Clothe(id = 5, name = "Джинсы Levi's",    imageUrl = "img5", storeUrl = "", occasion = "casual"),
        Clothe(id = 6, name = "Красное платье",   imageUrl = "img6", storeUrl = "", occasion = "date"),
        Clothe(id = 7, name = "Старый свитер",    imageUrl = "img7", storeUrl = "", occasion = null),
        Clothe(id = 8, name = "Футболка без тега", imageUrl = "img8", storeUrl = "", occasion = null),
    )

    // Лог всех вызовов — чтобы в тесте было видно какие запросы летели
    val callLog = mutableListOf<String>()

    override suspend fun getClothes(occasion: String?): List<Clothe> {
        val result = if (occasion.isNullOrBlank()) {
            database
        } else {
            database.filter { it.occasion == occasion }
        }
        callLog.add("getClothes(occasion=$occasion) → ${result.size} items: ${result.map { it.name }}")
        return result
    }

    override suspend fun loadClothe(bitmap: ImageBitmap, occasion: String?) {
        callLog.add("loadClothe(occasion=$occasion)")
    }

    override suspend fun uploadFromUrl(pageUrl: String): Clothe {
        callLog.add("uploadFromUrl($pageUrl)")
        return Clothe.empty()
    }

    override suspend fun deleteClothe(clotheId: Int) {
        callLog.add("deleteClothe($clotheId)")
    }
}

// ---------------------------------------------------------------------------
// Тест
// ---------------------------------------------------------------------------
class WardrobeOccasionFilterTest {

    private lateinit var repo: FakeWardrobeRepository
    private lateinit var useCase: WardrobeUseCase

    @Before
    fun setUp() {
        repo = FakeWardrobeRepository()
        useCase = WardrobeUseCase(repo)
        println("\n" + "=".repeat(60))
        println("  WARDROBE OCCASION FILTER TEST")
        println("=".repeat(60))
    }

    // -----------------------------------------------------------------------
    // 1. Без фильтра → все вещи
    // -----------------------------------------------------------------------
    @Test
    fun `getClothes without filter returns all items`() = runTest {
        println("\n[TEST 1] Без фильтра — ожидаем все вещи")

        val result = useCase.getClothes(occasion = null)

        println("  Запрос:   getClothes(occasion=null)")
        println("  Получено: ${result.size} вещей")
        result.forEachIndexed { i, c ->
            println("    ${i + 1}. '${c.name}' [occasion=${c.occasion ?: "—"}]")
        }

        assertEquals("Должны вернуться все 8 вещей", 8, result.size)
        println("  ✅ PASSED")
    }

    // -----------------------------------------------------------------------
    // 2. Фильтр office → только офисные вещи
    // -----------------------------------------------------------------------
    @Test
    fun `getClothes with office filter returns only office items`() = runTest {
        println("\n[TEST 2] Фильтр 'office' — ожидаем только офисные вещи")

        val result = useCase.getClothes(occasion = "office")

        println("  Запрос:   getClothes(occasion='office')")
        println("  Получено: ${result.size} вещей")
        result.forEachIndexed { i, c ->
            println("    ${i + 1}. '${c.name}' [occasion=${c.occasion}]")
        }

        assertEquals("Должно быть 2 офисных вещи", 2, result.size)
        assertTrue("Все должны быть office", result.all { it.occasion == "office" })
        println("  ✅ PASSED")
    }

    // -----------------------------------------------------------------------
    // 3. Фильтр sport → только спортивные
    // -----------------------------------------------------------------------
    @Test
    fun `getClothes with sport filter returns only sport items`() = runTest {
        println("\n[TEST 3] Фильтр 'sport' — ожидаем только спортивные вещи")

        val result = useCase.getClothes(occasion = "sport")

        println("  Запрос:   getClothes(occasion='sport')")
        println("  Получено: ${result.size} вещей")
        result.forEachIndexed { i, c ->
            println("    ${i + 1}. '${c.name}' [occasion=${c.occasion}]")
        }

        assertEquals("Должно быть 2 спортивных вещи", 2, result.size)
        assertTrue("Все должны быть sport", result.all { it.occasion == "sport" })
        println("  ✅ PASSED")
    }

    // -----------------------------------------------------------------------
    // 4. Фильтр с нет результатами (travel) → пустой список
    // -----------------------------------------------------------------------
    @Test
    fun `getClothes with travel filter returns empty list`() = runTest {
        println("\n[TEST 4] Фильтр 'travel' — в базе нет таких вещей")

        val result = useCase.getClothes(occasion = "travel")

        println("  Запрос:   getClothes(occasion='travel')")
        println("  Получено: ${result.size} вещей (ожидаем 0)")

        assertTrue("Список должен быть пустым", result.isEmpty())
        println("  ✅ PASSED")
    }

    // -----------------------------------------------------------------------
    // 5. Вещи без occasion НЕ попадают в результат при активном фильтре
    // -----------------------------------------------------------------------
    @Test
    fun `items without occasion are excluded when filter is active`() = runTest {
        println("\n[TEST 5] Вещи без тега не должны попадать в отфильтрованный результат")

        val resultOffice = useCase.getClothes(occasion = "office")
        val resultCasual = useCase.getClothes(occasion = "casual")

        println("  office-результат: ${resultOffice.map { it.name }}")
        println("  casual-результат: ${resultCasual.map { it.name }}")

        val untagged = resultOffice.filter { it.occasion == null }
        assertTrue("В office-результате не должно быть вещей без тега", untagged.isEmpty())

        val untaggedCasual = resultCasual.filter { it.occasion == null }
        assertTrue("В casual-результате не должно быть вещей без тега", untaggedCasual.isEmpty())

        println("  ✅ PASSED")
    }

    // -----------------------------------------------------------------------
    // 6. Сброс фильтра: null после активного → снова все вещи
    // -----------------------------------------------------------------------
    @Test
    fun `resetting filter to null returns all items including untagged`() = runTest {
        println("\n[TEST 6] Сброс фильтра: office → null — должны вернуться все вещи включая без тега")

        val officeResult = useCase.getClothes(occasion = "office")
        println("  Шаг 1 getClothes('office') → ${officeResult.size} вещей")

        val allResult = useCase.getClothes(occasion = null)
        println("  Шаг 2 getClothes(null) → ${allResult.size} вещей")

        val untagged = allResult.filter { it.occasion == null }
        println("  Вещи без тега в полном списке: ${untagged.map { it.name }}")

        assertEquals("После сброса должны вернуться все 8", 8, allResult.size)
        assertEquals("Включая 2 вещи без тега", 2, untagged.size)
        println("  ✅ PASSED")
    }

    // -----------------------------------------------------------------------
    // Финальный вывод лога вызовов репозитория
    // -----------------------------------------------------------------------
    @org.junit.After
    fun printCallLog() {
        println("\n--- Call log (FakeWardrobeRepository) ---")
        repo.callLog.forEachIndexed { i, entry -> println("  ${i + 1}. $entry") }
        println("=".repeat(60) + "\n")
    }
}
