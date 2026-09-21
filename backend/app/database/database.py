"""
VoiceGuard Backend - SQLite Audit & Demo Threat Database
Stores incident records, demo threat intelligence records, and verification events.
"""

import sqlite3
import os
import json
from datetime import datetime
from typing import Optional, Dict, Any, List

DB_PATH = os.path.join(os.path.dirname(__file__), "voiceguard.db")

def init_db():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # Incidents Table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS incidents (
        id TEXT PRIMARY KEY,
        timestamp TEXT NOT NULL,
        call_id TEXT NOT NULL,
        caller_number TEXT NOT NULL,
        caller_name TEXT,
        risk_score INTEGER NOT NULL,
        threat_type TEXT NOT NULL,
        language TEXT NOT NULL,
        transcript_summary TEXT,
        forensic_evidence TEXT,
        recommended_action TEXT
    )
    """)

    # Demo Threat Intelligence Numbers
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS threat_intel (
        phone_number TEXT PRIMARY KEY,
        name TEXT,
        category TEXT,
        reputation_score REAL,
        reports_count INTEGER,
        historical_incidents INTEGER
    )
    """)

    # Populate Realistic Synthetic Demo Threat Intel
    demo_records = [
        ("+919876543210", "Suspected Bank Impersonator", "Financial Fraud", 0.92, 142, 18),
        ("+919123456789", "Fake Mumbai Police / Digital Arrest", "Authority Extortion", 0.95, 230, 29),
        ("+918888877777", "Delivery KYC Scam", "Credential Theft", 0.88, 94, 11),
        ("+919820012345", "Unknown Caller", "Unverified", 0.60, 4, 1),
        ("+919422001122", "Rahul Sharma (Friend)", "Personal", 0.05, 0, 0),
        ("+919822114455", "Mom (Genuine Number)", "Family", 0.02, 0, 0)
    ]

    cursor.executemany("""
    INSERT OR REPLACE INTO threat_intel (phone_number, name, category, reputation_score, reports_count, historical_incidents)
    VALUES (?, ?, ?, ?, ?, ?)
    """, demo_records)

    conn.commit()
    conn.close()

def get_threat_intel(phone_number: str) -> Optional[Dict[str, Any]]:
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute("SELECT phone_number, name, category, reputation_score, reports_count, historical_incidents FROM threat_intel WHERE phone_number = ?", (phone_number,))
    row = cursor.fetchone()
    conn.close()
    if row:
        return {
            "phone_number": row[0],
            "name": row[1],
            "category": row[2],
            "reputation_score": row[3],
            "reports_count": row[4],
            "historical_incidents": row[5],
            "is_synthetic_demo_data": True
        }
    return None

def save_incident(incident: Dict[str, Any]):
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute("""
    INSERT OR REPLACE INTO incidents (id, timestamp, call_id, caller_number, caller_name, risk_score, threat_type, language, transcript_summary, forensic_evidence, recommended_action)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, (
        incident["id"],
        incident.get("timestamp", datetime.utcnow().isoformat()),
        incident["call_id"],
        incident["caller_number"],
        incident.get("caller_name"),
        incident["risk_score"],
        incident["threat_type"],
        incident.get("language", "en"),
        incident.get("transcript_summary", ""),
        json.dumps(incident.get("forensic_evidence", [])),
        incident.get("recommended_action", "")
    ))
    conn.commit()
    conn.close()

def list_incidents() -> List[Dict[str, Any]]:
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute("SELECT id, timestamp, call_id, caller_number, caller_name, risk_score, threat_type, language, transcript_summary, forensic_evidence, recommended_action FROM incidents ORDER BY timestamp DESC")
    rows = cursor.fetchall()
    conn.close()

    results = []
    for r in rows:
        results.append({
            "incident_id": r[0],
            "timestamp": r[1],
            "call_id": r[2],
            "caller_number": r[3],
            "caller_name": r[4],
            "risk_score": r[5],
            "threat_type": r[6],
            "language": r[7],
            "transcript_summary": r[8],
            "forensic_evidence": json.loads(r[9]) if r[9] else [],
            "recommended_action": r[10],
            "reporting_destinations": ["Cybercrime Portal (1930)", "Chakshu (Sanchar Saathi)"]
        })
    return results

init_db()
