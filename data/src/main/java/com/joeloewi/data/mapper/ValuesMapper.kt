package com.joeloewi.data.mapper

import com.joeloewi.domain.entity.Values

class ValuesMapper {
    fun toDomain(values: com.joeloewi.data.Values): Values = Values(
        jumpState = values.jumpCount
    )
}