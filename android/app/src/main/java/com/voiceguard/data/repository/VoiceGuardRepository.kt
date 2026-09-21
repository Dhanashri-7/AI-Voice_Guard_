package com.voiceguard.data.repository

import com.voiceguard.data.local.dao.VoiceGuardDao
import com.voiceguard.data.local.entity.CallSessionEntity
import com.voiceguard.data.local.entity.CallerEntity
import com.voiceguard.data.local.entity.IncidentEntity
import com.voiceguard.data.local.entity.ProtectedContactEntity
import kotlinx.coroutines.flow.Flow

class VoiceGuardRepository(private val dao: VoiceGuardDao) {

    val allCallSessions: Flow<List<CallSessionEntity>> = dao.getAllCallSessions()
    val highRiskSessions: Flow<List<CallSessionEntity>> = dao.getHighRiskSessions()
    val allIncidents: Flow<List<IncidentEntity>> = dao.getAllIncidents()
    val allProtectedContacts: Flow<List<ProtectedContactEntity>> = dao.getAllProtectedContacts()

    suspend fun saveCallSession(session: CallSessionEntity) {
        dao.insertCallSession(session)
    }

    suspend fun saveIncident(incident: IncidentEntity) {
        dao.insertIncident(incident)
    }

    suspend fun getIncidentById(id: String): IncidentEntity? {
        return dao.getIncidentById(id)
    }

    suspend fun sanitizeSavedContactIncidents() {
        dao.sanitizeSavedContactIncidents()
    }

    suspend fun saveProtectedContact(contact: ProtectedContactEntity) {
        dao.insertProtectedContact(contact)
    }

    suspend fun deleteProtectedContact(contact: ProtectedContactEntity) {
        dao.deleteProtectedContact(contact)
    }

    suspend fun getProtectedContactByPhone(phone: String): ProtectedContactEntity? {
        return dao.getProtectedContactByPhone(phone)
    }

    suspend fun getCaller(phone: String): CallerEntity? {
        return dao.getCaller(phone)
    }

    suspend fun purgeAllUserData() {
        dao.clearAllCallSessions()
        dao.clearAllIncidents()
        dao.clearVoiceAnalyses()
        dao.clearRiskEvents()
    }
}
