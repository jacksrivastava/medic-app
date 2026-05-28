package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalRecordDao {
    @Query("SELECT * FROM medical_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<MedicalRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MedicalRecord): Long

    @Delete
    suspend fun deleteRecord(record: MedicalRecord)

    @Query("DELETE FROM medical_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)
}

@Dao
interface DoctorAppointmentDao {
    @Query("SELECT * FROM doctor_appointments ORDER BY date ASC, time ASC")
    fun getAllAppointments(): Flow<List<DoctorAppointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: DoctorAppointment): Long

    @Delete
    suspend fun deleteAppointment(appointment: DoctorAppointment)

    @Query("UPDATE doctor_appointments SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Int, completed: Boolean)
}

@Dao
interface MedicationReminderDao {
    @Query("SELECT * FROM medication_reminders ORDER BY medicationName ASC")
    fun getAllReminders(): Flow<List<MedicationReminder>>

    @Query("SELECT * FROM medication_reminders WHERE isActive = 1 ORDER BY nextReminderTime ASC")
    fun getActiveReminders(): Flow<List<MedicationReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MedicationReminder): Long

    @Delete
    suspend fun deleteReminder(reminder: MedicationReminder)

    @Query("UPDATE medication_reminders SET isActive = :active WHERE id = :id")
    suspend fun setActiveState(id: Int, active: Boolean)

    @Query("UPDATE medication_reminders SET nextReminderTime = :nextTime WHERE id = :id")
    suspend fun updateNextReminderTime(id: Int, nextTime: Long)
}

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLog): Long

    @Query("UPDATE notification_logs SET isRead = true WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM notification_logs")
    suspend fun clearAll()
}
