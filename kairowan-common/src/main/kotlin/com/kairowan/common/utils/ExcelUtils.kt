package com.kairowan.common.utils

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.lang.reflect.Field
import java.util.Date

/**
 * Excel 工具类
 * Wraps Apache POI for generic list export
 *
 * @author Kairowan
 * @date 2026-01-17
 */
object ExcelUtils {

    /**
     * 导出列表到 Excel (返回 ByteArray)
     */
    fun <T> exportObjects(list: List<T>, clazz: Class<T>): ByteArray {
        val workbook: Workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Sheet1")
        
        // 1. 解析注解
        val fields = clazz.declaredFields.filter { it.isAnnotationPresent(Excel::class.java) }
        if (fields.isEmpty()) {
            throw IllegalArgumentException("No @Excel annotations found in ${clazz.simpleName}")
        }
        
        // 2. 创建表头
        val headerRow = sheet.createRow(0)
        fields.forEachIndexed { index, field ->
            val excelAnno = field.getAnnotation(Excel::class.java)
            val cell = headerRow.createCell(index)
            cell.setCellValue(if (excelAnno.name.isNotEmpty()) excelAnno.name else field.name)
        }

        // 3. 填充数据
        list.forEachIndexed { rowIndex, item ->
            val row = sheet.createRow(rowIndex + 1)
            fields.forEachIndexed { colIndex, field ->
                field.isAccessible = true
                val value = field.get(item)
                val cell = row.createCell(colIndex)
                setCellValue(cell, value, field.getAnnotation(Excel::class.java))
            }
        }
        
        // 4. 输出流
        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()
        return outputStream.toByteArray()
    }

    private fun setCellValue(cell: Cell, value: Any?, anno: Excel) {
        if (value == null) {
            cell.setCellValue("")
            return
        }
        
        when (value) {
            is String -> cell.setCellValue(value)
            is Number -> cell.setCellValue(value.toDouble())
            is Boolean -> cell.setCellValue(value)
            is Date -> {
                // Simple date formatting (Production would use DateUtil with pattern)
                cell.setCellValue(value.toString())
            }
            else -> cell.setCellValue(value.toString())
        }
    }
}
