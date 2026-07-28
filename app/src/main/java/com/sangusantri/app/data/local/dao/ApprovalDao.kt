package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.ApprovalEntity

@Dao
interface ApprovalDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ApprovalEntity)

    @Query("SELECT * FROM approvals WHERE id = :id")
    suspend fun getById(id: String): ApprovalEntity?

    @Query("DELETE FROM approvals WHERE id = :id")
    suspend fun deleteById(id: String)
}
