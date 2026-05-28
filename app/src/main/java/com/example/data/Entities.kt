package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_records")
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "Prescription", "Lab Result", "Immunization", "Clinical Notes", "Other"
    val date: String,
    val patientNotes: String,
    val rawExtractedContent: String = "",
    val attachedFileName: String = "",
    val attachmentPath: String = "",
    val profileName: String = "Sarah"
)

@Entity(tableName = "doctor_appointments")
data class DoctorAppointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorName: String,
    val specialty: String,
    val date: String, // e.g., "2026-06-15"
    val time: String, // e.g., "14:30"
    val location: String,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val profileName: String = "Sarah"
)

@Entity(tableName = "medication_reminders")
data class MedicationReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationName: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val specificTimes: String, // Comma-separated list like "08:00,20:00"
    val nextReminderTime: Long = 0L, // Unix timestamp in milliseconds for sorting & triggering
    val patientNotes: String = "",
    val isActive: Boolean = true,
    val profileName: String = "Sarah"
)

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
