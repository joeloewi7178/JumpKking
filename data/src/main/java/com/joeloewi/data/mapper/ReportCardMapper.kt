package com.joeloewi.data.mapper

import com.joeloewi.data.entity.ReportCardEntity
import com.joeloewi.data.mapper.base.Mapper
import com.joeloewi.domain.entity.ReportCard

class ReportCardMapper: Mapper<ReportCard, ReportCardEntity> {
    override fun toData(domainEntity: ReportCard): ReportCardEntity = ReportCardEntity(
        androidId = domainEntity.androidId,
        jumpCount = domainEntity.jumpCount,
        timestamp = domainEntity.timestamp
    )

    override fun toDomain(dataEntity: ReportCardEntity): ReportCard = ReportCard(
        androidId = dataEntity.androidId,
        jumpCount = dataEntity.jumpCount,
        timestamp = dataEntity.timestamp
    )
}