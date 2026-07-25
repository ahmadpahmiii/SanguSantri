package com.sangusantri.app.data.local.database

import androidx.room.TypeConverter
import com.sangusantri.app.domain.model.AmaliyahVersionStatus
import com.sangusantri.app.domain.model.ApprovalStatus
import com.sangusantri.app.domain.model.OwnerType
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.domain.model.Visibility

/** Stores the shared domain enums as their declared name so schema and JSON stay human-readable. */
class Converters {
    @TypeConverter
    fun fromStepType(value: StepType): String = value.name

    @TypeConverter
    fun toStepType(value: String): StepType = StepType.valueOf(value)

    @TypeConverter
    fun fromAmaliyahVersionStatus(value: AmaliyahVersionStatus): String = value.name

    @TypeConverter
    fun toAmaliyahVersionStatus(value: String): AmaliyahVersionStatus = AmaliyahVersionStatus.valueOf(value)

    @TypeConverter
    fun fromApprovalStatus(value: ApprovalStatus): String = value.name

    @TypeConverter
    fun toApprovalStatus(value: String): ApprovalStatus = ApprovalStatus.valueOf(value)

    @TypeConverter
    fun fromOwnerType(value: OwnerType): String = value.name

    @TypeConverter
    fun toOwnerType(value: String): OwnerType = OwnerType.valueOf(value)

    @TypeConverter
    fun fromVisibility(value: Visibility): String = value.name

    @TypeConverter
    fun toVisibility(value: String): Visibility = Visibility.valueOf(value)
}
