package ua.com.radiokot.photoprism

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ua.com.radiokot.photoprism.di.ioModules
import ua.com.radiokot.photoprism.features.ext.memories.data.model.Memory
import ua.com.radiokot.photoprism.features.ext.memories.data.model.MemoryDbEntity
import ua.com.radiokot.photoprism.features.gallery.logic.MediaPreviewUrlFactory
import java.util.Date
import kotlin.test.assertIs

class MemoryDbEntityTest : KoinComponent {
    companion object {
        @BeforeClass
        @JvmStatic
        fun initKoin() {
            startKoin {
                modules(
                    ioModules
                            +
                            module {
                                singleOf<MediaPreviewUrlFactory>(::DummyMediaPreviewUrlFactory)
                            }
                )
            }
        }
    }

    @Test
    fun createFromMemorySuccessfully() {
        val memory = Memory(
            typeData = Memory.TypeData.ThisDayInThePast(
                year = 2021,
            ),
            searchQuery = "uid:1|2|3",
            createdAt = Date(1706952860000),
            isSeen = false,
            thumbnailHash = "hash",
        )

        val entity = MemoryDbEntity(memory)

        Assert.assertEquals(memory.searchQuery, entity.searchQuery)
        Assert.assertEquals(memory.createdAt.time, entity.createdAtMs)
        Assert.assertEquals(memory.isSeen, entity.isSeen)
        Assert.assertEquals(memory.thumbnailHash, entity.thumbnailHash)

        val typeData = assertIs<MemoryDbEntity.TypeData.ThisDayInThePast>(entity.typeData)
        Assert.assertEquals((memory.typeData as Memory.TypeData.ThisDayInThePast).year, typeData.year)
    }

    @Test
    fun mapToMemorySuccessfully() {
        val entity = MemoryDbEntity(
            searchQuery = "uid:1|2|3",
            createdAtMs = 1706952860000,
            isSeen = false,
            thumbnailHash = "hash",
            typeData = MemoryDbEntity.TypeData.ThisDayInThePast(
                year = 2021,
            )
        )

        val memory = entity.toMemory()

        Assert.assertEquals(entity.searchQuery, memory.searchQuery)
        Assert.assertEquals(entity.createdAtMs, memory.createdAt.time)
        Assert.assertEquals(entity.isSeen, memory.isSeen)
        Assert.assertEquals(entity.thumbnailHash, memory.thumbnailHash)

        val thisDayInThePast = assertIs<Memory.TypeData.ThisDayInThePast>(memory.typeData)
        Assert.assertEquals(
            (entity.typeData as MemoryDbEntity.TypeData.ThisDayInThePast).year,
            thisDayInThePast.year
        )
    }

    @Test
    fun convertTypeDataToJsonSuccessfully() {
        val typeData = MemoryDbEntity.TypeData.ThisDayInThePast(
            year = 2021,
        )

        val json = MemoryDbEntity.Converters().typeDataToJson(typeData)

        Assert.assertEquals("{\"_t\":\"1\",\"year\":2021}", json)
    }

    @Test
    fun convertTypeDataToJsonSuccessfully_IfNull() {
        val json = MemoryDbEntity.Converters().typeDataToJson(null)
        Assert.assertNull(json)
    }

    @Test
    fun convertJsonToTypeDataSuccessfully() {
        val json = "{\"_t\":\"1\",\"year\":2021}"

        val typeData = MemoryDbEntity.Converters().typeDataFromJson(json)

        assertIs<MemoryDbEntity.TypeData.ThisDayInThePast>(typeData)
        Assert.assertEquals(2021, typeData.year)
    }

    @Test
    fun convertJsonToTypeDataSuccessfully_IfNull() {
        val typeData = MemoryDbEntity.Converters().typeDataFromJson(null)
        Assert.assertNull(typeData)
    }
}
