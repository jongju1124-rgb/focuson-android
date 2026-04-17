package com.focuson.app.data.repo

import com.focuson.app.data.db.BlockRuleDao
import com.focuson.app.data.db.entity.BlockRuleEntity
import com.focuson.app.domain.model.PresetMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BlockRuleRepository(private val dao: BlockRuleDao) {

    fun observeMode(modeId: String): Flow<List<BlockRuleEntity>> = dao.observeByMode(modeId)

    fun observeModeApps(modeId: String): Flow<Set<String>> =
        observeMode(modeId).map { list -> list.filter { it.kind == KIND_APP && it.enabled }.map { it.value }.toSet() }

    fun observeModeSites(modeId: String): Flow<List<String>> =
        observeMode(modeId).map { list -> list.filter { it.kind == KIND_SITE && it.enabled }.map { it.value } }

    suspend fun findMode(modeId: String): List<BlockRuleEntity> = dao.findByMode(modeId)

    suspend fun seedIfEmpty(mode: PresetMode) {
        val existing = dao.findByMode(mode.id)
        if (existing.isEmpty()) {
            val apps = mode.defaultAppPackages.map { BlockRuleEntity(modeId = mode.id, kind = KIND_APP, value = it) }
            val sites = mode.defaultSitePatterns.map { BlockRuleEntity(modeId = mode.id, kind = KIND_SITE, value = it) }
            dao.upsertAll(apps + sites)
        }
    }

    suspend fun replaceApps(modeId: String, packages: Collection<String>) {
        val rules = packages.map { BlockRuleEntity(modeId = modeId, kind = KIND_APP, value = it) }
        dao.replaceByModeAndKind(modeId, KIND_APP, rules)
    }

    suspend fun replaceSites(modeId: String, patterns: Collection<String>) {
        val rules = patterns.map { BlockRuleEntity(modeId = modeId, kind = KIND_SITE, value = it) }
        dao.replaceByModeAndKind(modeId, KIND_SITE, rules)
    }

    companion object {
        const val KIND_APP = "app"
        const val KIND_SITE = "site"
    }
}
