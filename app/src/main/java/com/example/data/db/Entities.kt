package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Channel
import com.example.data.model.FilterRule
import com.example.data.model.FilterType
import com.example.data.model.Provider
import com.example.data.model.ProviderType

@Entity(
    tableName = "channels",
    indices = [
        Index("providerId"),
        Index("group"),
        Index("isFavorite"),
        Index("isCustomHidden")
    ]
)
data class ChannelEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val channelNumber: Int,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val group: String = "General",
    val tvgId: String = "",
    val tvgName: String = "",
    val customName: String? = null,
    val customNumber: Int? = null,
    val customGroup: String? = null,
    val isFavorite: Boolean = false,
    val isCustomHidden: Boolean = false,
    val isParentalLocked: Boolean = false,
    val alternativeUrlsCsv: String = "",
    val language: String = "",
    val country: String = "",
    val isPlayable: Boolean = true,
    val sortOrder: Int = 0
) {
    fun toChannel(): Channel {
        val altUrls = if (alternativeUrlsCsv.isNotBlank()) {
            alternativeUrlsCsv.split(";").filter { it.isNotBlank() }
        } else {
            emptyList()
        }

        return Channel(
            id = id,
            providerId = providerId,
            channelNumber = channelNumber,
            name = name,
            streamUrl = streamUrl,
            logoUrl = logoUrl,
            group = group,
            tvgId = tvgId,
            tvgName = tvgName,
            customName = customName,
            customNumber = customNumber,
            customGroup = customGroup,
            isFavorite = isFavorite,
            isCustomHidden = isCustomHidden,
            isParentalLocked = isParentalLocked,
            alternativeUrls = altUrls,
            language = language,
            country = country,
            isPlayable = isPlayable,
            sortOrder = sortOrder
        )
    }

    companion object {
        fun fromChannel(channel: Channel): ChannelEntity {
            return ChannelEntity(
                id = channel.id,
                providerId = channel.providerId,
                channelNumber = channel.channelNumber,
                name = channel.name,
                streamUrl = channel.streamUrl,
                logoUrl = channel.logoUrl,
                group = channel.group,
                tvgId = channel.tvgId,
                tvgName = channel.tvgName,
                customName = channel.customName,
                customNumber = channel.customNumber,
                customGroup = channel.customGroup,
                isFavorite = channel.isFavorite,
                isCustomHidden = channel.isCustomHidden,
                isParentalLocked = channel.isParentalLocked,
                alternativeUrlsCsv = channel.alternativeUrls.joinToString(";"),
                language = channel.language,
                country = channel.country,
                isPlayable = channel.isPlayable,
                sortOrder = channel.sortOrder
            )
        }
    }
}

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // ProviderType name
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val epgUrl: String = "",
    val macAddress: String = "",
    val isActive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),
    val channelCount: Int = 0
) {
    fun toProvider(): Provider {
        val provType = try {
            ProviderType.valueOf(type)
        } catch (_: Exception) {
            ProviderType.M3U_URL
        }
        return Provider(
            id = id,
            name = name,
            type = provType,
            url = url,
            username = username,
            password = password,
            epgUrl = epgUrl,
            macAddress = macAddress,
            isActive = isActive,
            lastUpdated = lastUpdated,
            channelCount = channelCount
        )
    }

    companion object {
        fun fromProvider(p: Provider): ProviderEntity {
            return ProviderEntity(
                id = p.id,
                name = p.name,
                type = p.type.name,
                url = p.url,
                username = p.username,
                password = p.password,
                epgUrl = p.epgUrl,
                macAddress = p.macAddress,
                isActive = p.isActive,
                lastUpdated = p.lastUpdated,
                channelCount = p.channelCount
            )
        }
    }
}

@Entity(tableName = "custom_groups")
data class CustomGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val orderIndex: Int = 0
)

@Entity(tableName = "filter_rules")
data class FilterRuleEntity(
    @PrimaryKey val id: String,
    val type: String,
    val pattern: String,
    val isExclude: Boolean = true,
    val isEnabled: Boolean = true
) {
    fun toFilterRule(): FilterRule {
        val fType = try { FilterType.valueOf(type) } catch (_: Exception) { FilterType.CATEGORY }
        return FilterRule(
            id = id,
            type = fType,
            pattern = pattern,
            isExclude = isExclude,
            isEnabled = isEnabled
        )
    }

    companion object {
        fun fromFilterRule(rule: FilterRule): FilterRuleEntity {
            return FilterRuleEntity(
                id = rule.id,
                type = rule.type.name,
                pattern = rule.pattern,
                isExclude = rule.isExclude,
                isEnabled = rule.isEnabled
            )
        }
    }
}
