package com.kairowan.ktor.common.utils

/**
 * 树形结构工具类
 * @author Kairowan
 * @date 2026-01-18
 */
object TreeUtils {

    /**
     * 通用树形结构构建
     * @param list 原始列表
     * @param rootId 根节点ID
     * @param getId 获取节点ID的函数
     * @param getParentId 获取父节点ID的函数
     * @param setChildren 设置子节点的函数
     */
    fun <T, ID> buildTree(
        list: List<T>,
        rootId: ID,
        getId: (T) -> ID,
        getParentId: (T) -> ID,
        setChildren: (T, List<T>) -> Unit
    ): List<T> {
        val nodeMap = list.groupBy { getParentId(it) }
        
        fun findChildren(parentId: ID): List<T> {
            val children = nodeMap[parentId] ?: emptyList()
            children.forEach { node ->
                val grandChildren = findChildren(getId(node))
                setChildren(node, grandChildren)
            }
            return children
        }
        
        return findChildren(rootId)
    }

    /**
     * 简化版: 构建部门树
     */
    fun <T> buildDeptTree(
        depts: List<T>,
        getIdFn: (T) -> Long,
        getParentIdFn: (T) -> Long,
        setChildrenFn: (T, List<T>) -> Unit
    ): List<T> {
        return buildTree(depts, 0L, getIdFn, getParentIdFn, setChildrenFn)
    }
    
    /**
     * 获取所有子节点ID (包括自身)
     */
    fun <T, ID> getAllChildIds(
        list: List<T>,
        parentId: ID,
        getId: (T) -> ID,
        getParentId: (T) -> ID
    ): Set<ID> {
        val result = mutableSetOf<ID>()
        result.add(parentId)
        
        val nodeMap = list.groupBy { getParentId(it) }
        
        fun collectChildren(pId: ID) {
            val children = nodeMap[pId] ?: emptyList()
            children.forEach { node ->
                val nodeId = getId(node)
                result.add(nodeId)
                collectChildren(nodeId)
            }
        }
        
        collectChildren(parentId)
        return result
    }
}
