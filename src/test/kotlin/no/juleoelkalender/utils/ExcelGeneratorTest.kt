package no.juleoelkalender.utils

import no.juleoelkalender.exception.ExcelGeneraionException
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.ZonedDateTime

internal class ExcelGeneratorTest {
    @BeforeEach
    fun setUp() {
    }

    @Test
    fun testPrepareStyles() {
        val excelGenerator = ExcelGenerator()
        try {
            XSSFWorkbook().use { workbook ->
                val styleMap: Map<CustomCellStyle, CellStyle> = excelGenerator.prepareStyles(workbook)
                assertAll(
                        { assertNotNull(styleMap) },
                        { assertEquals(3, styleMap.size) },
                        { assertTrue(styleMap.containsKey(CustomCellStyle.LEFT_ALIGNED)) },
                        { assertTrue(styleMap.containsKey(CustomCellStyle.RIGHT_ALIGNED)) },
                        {
                            assertTrue(
                                    styleMap.containsKey(CustomCellStyle.GREY_CENTERED_BOLD_ARIAL_WITH_BORDER)
                            )
                        }
                )
            }
        } catch (e: IOException) {
            throw ExcelGeneraionException(e)
        }
    }

    @Test
    fun testCreateHeaderRow() {
        val excelGenerator = ExcelGenerator()
        try {
            XSSFWorkbook().use { workbook ->
                val sheet = workbook.createSheet("Sheet 1")
                excelGenerator.createHeaderRow(sheet, excelGenerator.prepareStyles(workbook), "A", "B", "C")
                assertEquals("STRING", sheet.getRow(0).getCell(0).cellType.name)
            }
        } catch (e: IOException) {
            throw ExcelGeneraionException(e)
        }
    }

    @Test
    fun testGenerateReport() {
        val excelGenerator = ExcelGenerator()
        val headers = arrayOf("A", "B", "C", "D")
        val rowData = listOf(
                arrayOf<Any>(1, "B1", 1.0, ZonedDateTime.now()),
                arrayOf<Any>(2, "B2", 2.0, ZonedDateTime.now())
        )
        val bytes = excelGenerator.generateReport("Sheet 1", headers, rowData)
        assertNotNull(bytes)
    }
}
