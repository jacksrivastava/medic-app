package com.example.data

import com.example.utils.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MedRepository(private val db: AppDatabase) {
    private val medicalRecordDao = db.medicalRecordDao()
    private val doctorAppointmentDao = db.doctorAppointmentDao()
    private val medicationReminderDao = db.medicationReminderDao()
    private val notificationLogDao = db.notificationLogDao()

    val allRecords: Flow<List<MedicalRecord>> = medicalRecordDao.getAllRecords().map { list ->
        list.map { record ->
            record.copy(
                title = CryptoManager.decryptString(record.title),
                patientNotes = CryptoManager.decryptString(record.patientNotes),
                rawExtractedContent = CryptoManager.decryptString(record.rawExtractedContent),
                attachedFileName = CryptoManager.decryptString(record.attachedFileName)
            )
        }
    }
    
    val allAppointments: Flow<List<DoctorAppointment>> = doctorAppointmentDao.getAllAppointments()
    val allReminders: Flow<List<MedicationReminder>> = medicationReminderDao.getAllReminders()
    val activeReminders: Flow<List<MedicationReminder>> = medicationReminderDao.getActiveReminders()
    val allLogs: Flow<List<NotificationLog>> = notificationLogDao.getAllLogs()

    suspend fun insertRecord(record: MedicalRecord): Long {
        val encryptedRecord = record.copy(
            title = CryptoManager.encryptString(record.title),
            patientNotes = CryptoManager.encryptString(record.patientNotes),
            rawExtractedContent = CryptoManager.encryptString(record.rawExtractedContent),
            attachedFileName = CryptoManager.encryptString(record.attachedFileName)
        )
        return medicalRecordDao.insertRecord(encryptedRecord)
    }
    
    suspend fun deleteRecord(record: MedicalRecord) = medicalRecordDao.deleteRecord(record)
    suspend fun deleteRecordById(id: Int) = medicalRecordDao.deleteRecordById(id)


    suspend fun insertAppointment(appointment: DoctorAppointment) = doctorAppointmentDao.insertAppointment(appointment)
    suspend fun deleteAppointment(appointment: DoctorAppointment) = doctorAppointmentDao.deleteAppointment(appointment)
    suspend fun setAppointmentCompleted(id: Int, completed: Boolean) = doctorAppointmentDao.setCompleted(id, completed)

    suspend fun insertReminder(reminder: MedicationReminder) = medicationReminderDao.insertReminder(reminder)
    suspend fun deleteReminder(reminder: MedicationReminder) = medicationReminderDao.deleteReminder(reminder)
    suspend fun setReminderActive(id: Int, active: Boolean) = medicationReminderDao.setActiveState(id, active)
    suspend fun updateNextReminderTime(id: Int, nextTime: Long) = medicationReminderDao.updateNextReminderTime(id, nextTime)

    suspend fun insertLog(log: NotificationLog) = notificationLogDao.insertLog(log)
    suspend fun markLogAsRead(id: Int) = notificationLogDao.markAsRead(id)
    suspend fun clearLogs() = notificationLogDao.clearAll()
}
