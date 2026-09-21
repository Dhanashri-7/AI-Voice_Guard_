# VoiceGuard Privacy & Data Sovereignty Architecture
## Compliance with India's Digital Personal Data Protection (DPDP) Act 2023
### Smart India Hackathon 2026 — Problem Statement 26104

---

## 1. Privacy-By-Design Core Principles

VoiceGuard treats user voice privacy as an inviolable right. Security applications that secretly record or transmit private phone conversations violate user trust and create severe surveillance vulnerabilities.

### Key Privacy Pillars:
1. **Zero Raw Audio Retention**:
   - Audio chunks (1.5 seconds) reside solely in volatile RAM buffers.
   - Once spectral features (e.g. Mel spectrogram, F0 pitch, energy) are computed, the raw PCM audio buffer is immediately zeroed and freed.
   - Raw voice waveforms are **NEVER written to disk** or transmitted to cloud servers by default.
2. **On-Device Edge Inference**:
   - Deepfake voice detection is executed locally using quantized mobile models (`SpectroCNNAttention` via ONNX Runtime Mobile / TFLite).
   - Analysis functions 100% offline without requiring an active internet connection.
3. **Anonymized & Hashed Caller Metadata**:
   - Phone numbers stored in local logs can be hashed or masked (`+91 91234 XXXXX`) according to user preference.
4. **User Data Sovereignty & One-Tap Wipe**:
   - Users maintain absolute control. The Privacy Center provides a single button to purge all local Room databases, enrolled voice embeddings, and incident logs.

---

## 2. Data Flow Comparison

| Data Type | Traditional Invasive Apps | VoiceGuard Privacy Architecture |
| :--- | :--- | :--- |
| **Raw Call Audio** | Uploaded to remote servers for speech recognition | **Discarded immediately** after in-RAM feature extraction |
| **Speaker Voiceprint** | Centralized biometrics database | **Stored locally** in encrypted Room database using Android Keystore |
| **Conversation Transcript** | Full dialogue stored permanently | **Processed ephemerally**; only summarized fraud markers retained |
| **Contact Book** | Synced to global directory | **Never harvested**; only user-selected protected contacts are enrolled |

---

## 3. Regulatory Alignment (DPDP Act 2023 & CERT-In Guidelines)

- **Purpose Limitation**: Audio features are processed exclusively for voice cloning and impersonation risk detection during the active call window.
- **Data Minimization**: Only 80 Mel bins and scalar prosody metrics (F0 std, ZCR, pause duration) are preserved in forensic logs.
- **Right to Erasure**: Implemented via `VoiceGuardRepository.purgeAllUserData()`, enabling instant deletion of all device records.
