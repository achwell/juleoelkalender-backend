package no.juleoelkalender.utils

import no.juleoelkalender.exception.ExcelGeneraionException
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.ZonedDateTime
import java.util.stream.IntStream

@Component
class ExcelGenerator {
    fun prepareStyles(wb: Workbook): Map<CustomCellStyle, CellStyle> {
        val boldArial = createBoldArialFont(wb)
        val leftAlignedStyle = createLeftAlignedStyle(wb)
        val rightAlignedStyle = createRightAlignedStyle(wb)
        val greyCenteredBoldArialWithBorderStyle = createGreyCenteredBoldArialWithBorderStyle(
                wb,
                boldArial
        )
        return mapOf(
                CustomCellStyle.RIGHT_ALIGNED to rightAlignedStyle,
                CustomCellStyle.LEFT_ALIGNED to leftAlignedStyle,
                CustomCellStyle.GREY_CENTERED_BOLD_ARIAL_WITH_BORDER to greyCenteredBoldArialWithBorderStyle
        )
    }

    fun generateReport(sheetname: String, headers: Array<String>, rowData: List<Array<Any>>): ByteArray {
        try {
            XSSFWorkbook().use { workbook ->
                val styles = prepareStyles(workbook)
                val sheet = workbook.createSheet(sheetname)
                createHeaderRow(sheet, styles, *headers)
                IntStream.range(0, rowData.size).forEach { i: Int -> createRow(sheet, styles, i + 1, *rowData[i]) }
                IntStream.range(0, headers.size).forEach { column: Int -> sheet.autoSizeColumn(column) }
                val out = ByteArrayOutputStream()
                workbook.write(out)
                out.close()
                return out.toByteArray()
            }
        } catch (e: IOException) {
            throw ExcelGeneraionException(e)
        }
    }

    fun createHeaderRow(
            sheet: XSSFSheet, styles: Map<CustomCellStyle, CellStyle>,
            vararg headers: String
    ): XSSFRow {
        val row = sheet.createRow(0)
        var colNumber = 0
        for (header in headers) {
            createHeaderCell(row, colNumber++, header, styles)
        }
        return row
    }

    fun createRow(
            sheet: XSSFSheet, styles: Map<CustomCellStyle, CellStyle>, rowNum: Int,
            vararg values: Any?
    ): XSSFRow {
        val row = sheet.createRow(rowNum)
        var colNr = 0
        for (value in values) {
            createContentCell(row, colNr++, value, styles)
        }
        return row
    }

    fun createHeaderCell(
            row: XSSFRow, columnIndex: Int, content: String?,
            styles: Map<CustomCellStyle, CellStyle>
    ): XSSFCell {
        return row.createCell(columnIndex).apply {
            setCellValue(content)
            setCellStyle(styles[CustomCellStyle.GREY_CENTERED_BOLD_ARIAL_WITH_BORDER])
        }
    }

    fun createContentCell(
            row: XSSFRow, columnIndex: Int, content: Any?,
            styles: Map<CustomCellStyle, CellStyle>
    ): XSSFCell? {
        when (content) {
            is String -> {
                return createStringContentCell(row, columnIndex, content, styles)
            }

            is Int -> {
                return createIntegerContentCell(row, columnIndex, content, styles)
            }

            is Double -> {
                return createDoubleContentCell(row, columnIndex, content, styles)
            }

            is ZonedDateTime -> {
                return createZonedDateTimeContentCell(row, columnIndex, content, styles)
            }
        }
        return null
    }

    private fun createStringContentCell(
            row: XSSFRow, columnIndex: Int, content: String?,
            styles: Map<CustomCellStyle, CellStyle>
    ): XSSFCell = row.createCell(columnIndex).apply {
        setCellValue(content)
        setCellStyle(styles[CustomCellStyle.LEFT_ALIGNED])
    }

    private fun createIntegerContentCell(
            row: XSSFRow, columnIndex: Int, content: Int,
            styles: Map<CustomCellStyle, CellStyle>
    ): XSSFCell = row.createCell(columnIndex).apply {
        setCellValue(content.toDouble())
        setCellStyle(styles[CustomCellStyle.RIGHT_ALIGNED])
    }

    private fun createDoubleContentCell(
            row: XSSFRow, columnIndex: Int, content: Double,
            styles: Map<CustomCellStyle, CellStyle>
    ): XSSFCell = row.createCell(columnIndex).apply {
        setCellValue(content)
        setCellStyle(styles[CustomCellStyle.RIGHT_ALIGNED])
    }

    private fun createZonedDateTimeContentCell(
            row: XSSFRow, columnIndex: Int, content: ZonedDateTime?,
            styles: Map<CustomCellStyle, CellStyle>
    ): XSSFCell = row.createCell(columnIndex).apply {
        setCellValue(content?.toLocalDateTime())
        setCellStyle(styles[CustomCellStyle.LEFT_ALIGNED])
    }

    private fun createLeftAlignedStyle(wb: Workbook): CellStyle = wb.createCellStyle().apply { alignment = HorizontalAlignment.LEFT }

    private fun createRightAlignedStyle(wb: Workbook): CellStyle = wb.createCellStyle().apply { alignment = HorizontalAlignment.RIGHT }

    private fun createBoldArialFont(wb: Workbook): Font = wb.createFont().apply {
        fontName = "Arial"
        bold = true
    }

    private fun createGreyCenteredBoldArialWithBorderStyle(wb: Workbook, font: Font): CellStyle = createBorderedStyle(wb).apply {
        alignment = HorizontalAlignment.CENTER
        fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
        fillPattern = FillPatternType.SOLID_FOREGROUND
        setFont(font)
    }

    private fun createBorderedStyle(wb: Workbook): CellStyle = wb.createCellStyle().apply {
        borderRight = BorderStyle.THIN
        borderLeft = BorderStyle.THIN
        borderTop = BorderStyle.THIN
        borderBottom = BorderStyle.THIN
        rightBorderColor = IndexedColors.BLACK.getIndex()
        leftBorderColor = IndexedColors.BLACK.getIndex()
        bottomBorderColor = IndexedColors.BLACK.getIndex()
        topBorderColor = IndexedColors.BLACK.getIndex()
    }
}
