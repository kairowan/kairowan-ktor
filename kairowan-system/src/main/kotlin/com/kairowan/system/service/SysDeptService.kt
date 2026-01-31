package com.kairowan.system.service

import com.kairowan.common.utils.TreeUtils
import com.kairowan.system.domain.*
import com.kairowan.core.page.KPageRequest
import com.kairowan.core.page.KTableData
import com.kairowan.core.extensions.toMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.entity.update

/**
 * 部门服务
 * @author Kairowan
 * @date 2026-01-18
 */
class SysDeptService(private val database: Database) {

    /**
     * 查询部门列表
     */
    suspend fun listDepts(): List<Map<String, Any?>> = withContext(Dispatchers.IO) {
        database.sequenceOf(SysDepts)
            .filter { it.status eq "0" }
            .toList()
            .sortedBy { it.orderNum }
            .map { it.toMap() }
    }

    /**
     * 查询部门树 (用于前端下拉选择)
     */
    suspend fun selectDeptTree(): List<DeptTreeVo> = withContext(Dispatchers.IO) {
        val depts = database.sequenceOf(SysDepts)
            .filter { it.status eq "0" }
            .toList()
            .sortedBy { it.orderNum }

        buildDeptTree(depts, 0L)
    }

    private fun buildDeptTree(depts: List<SysDept>, parentId: Long): List<DeptTreeVo> {
        return depts.filter { it.parentId == parentId }
            .map { dept ->
                DeptTreeVo(
                    id = dept.deptId,
                    label = dept.deptName,
                    children = buildDeptTree(depts, dept.deptId)
                )
            }
    }

    /**
     * 根据ID查询部门
     */
    suspend fun getById(deptId: Long): Map<String, Any?>? = withContext(Dispatchers.IO) {
        database.sequenceOf(SysDepts).find { it.deptId eq deptId }?.toMap()
    }

    /**
     * 新增部门
     */
    suspend fun save(dept: SysDept): Int = withContext(Dispatchers.IO) {
        val parentDept = database.sequenceOf(SysDepts).find { it.deptId eq dept.parentId }
        if (parentDept != null) {
            dept.ancestors = "${parentDept.ancestors},${parentDept.deptId}"
        } else {
            dept.ancestors = "0"
        }
        database.sequenceOf(SysDepts).add(dept)
    }

    /**
     * 修改部门
     */
    suspend fun update(dept: SysDept): Int = withContext(Dispatchers.IO) {
        database.sequenceOf(SysDepts).update(dept)
    }

    /**
     * 删除部门
     */
    suspend fun deleteById(deptId: Long): Int = withContext(Dispatchers.IO) {
        database.delete(SysDepts) { it.deptId eq deptId }
    }

    /**
     * 获取部门下所有子部门ID
     */
    suspend fun getChildDeptIds(deptId: Long): Set<Long> = withContext(Dispatchers.IO) {
        val allDepts = database.sequenceOf(SysDepts).toList()
        TreeUtils.getAllChildIds(
            allDepts,
            deptId,
            { it.deptId },
            { it.parentId }
        )
    }
}

/**
 * 部门树 VO
 */
data class DeptTreeVo(
    val id: Long,
    val label: String,
    val children: List<DeptTreeVo> = emptyList()
)

/**
 * 岗位服务
 * @author Kairowan
 * @date 2026-01-18
 */
class SysPostService(private val database: Database) {

    /**
     * 查询岗位列表
     */
    suspend fun list(page: KPageRequest? = null): KTableData = withContext(Dispatchers.IO) {
        val safePage = page?.normalized()
        val query = database.from(SysPosts)
            .select()
            .orderBy(SysPosts.postSort.asc())

        if (safePage != null) {
            val offset = safePage.getOffset()
            query.limit(offset, safePage.pageSize)

            val total = database.from(SysPosts).select(count()).map { it.getInt(1) }.first().toLong()
            val list = query.map { SysPosts.createEntity(it) }.map { it.toMap() }

            KTableData.build(list, total)
        } else {
            val list = query.map { SysPosts.createEntity(it) }.map { it.toMap() }
            KTableData.build(list)
        }
    }

    /**
     * 查询所有岗位 (用于下拉选择)
     */
    suspend fun selectPostAll(): List<Map<String, Any?>> = withContext(Dispatchers.IO) {
        database.sequenceOf(SysPosts)
            .filter { it.status eq "0" }
            .toList()
            .sortedBy { it.postSort }
            .map { it.toMap() }
    }

    /**
     * 根据ID查询岗位
     */
    suspend fun getById(postId: Long): Map<String, Any?>? = withContext(Dispatchers.IO) {
        database.sequenceOf(SysPosts).find { it.postId eq postId }?.toMap()
    }

    /**
     * 新增岗位
     */
    suspend fun save(post: SysPost): Int = withContext(Dispatchers.IO) {
        database.sequenceOf(SysPosts).add(post)
    }

    /**
     * 修改岗位
     */
    suspend fun update(post: SysPost): Int = withContext(Dispatchers.IO) {
        database.sequenceOf(SysPosts).update(post)
    }

    /**
     * 删除岗位
     */
    suspend fun deleteById(postId: Long): Int = withContext(Dispatchers.IO) {
        database.delete(SysPosts) { it.postId eq postId }
    }
}
