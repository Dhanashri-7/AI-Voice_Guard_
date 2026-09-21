# VoiceGuard Technical Architecture
## Smart India Hackathon 2026 — Problem Statement 26104
### Organization: All India Council for Technical Education (AICTE) — Cyber Security Cell

---

## 1. Architectural Philosophy: "Detect the Impersonation Attack, Not Just the Number"

Traditional caller identification applications are built around static telephone number reputation lists. However, modern AI voice cloning attacks bypass number-based filtering through spoofed Caller IDs, disposable burner SIM cards, or VoIP gateways.

**VoiceGuard** is designed with a defense-in-depth security model combining:
1. **Acoustic Voice Forensics**: Identifies neural vocoder phase anomalies, high-frequency spectral artifacts, and pitch rigidity.
2. **Speaker Consistency Verification**: Matches active caller embeddings against enrolled voice profiles of trusted family members.
3. **Indic Multilingual Semantic Risk Engine**: Analyzes conversation transcripts in Hindi, Marathi, and English for extortion and digital arrest triggers.
4. **Caller Threat Intelligence**: Computes pre-call caller reputation scores from local and national fraud registries.
5. **Calibrated 5-Factor Risk Fusion**: Uses a mathematically tuned formula with Exponential Moving Average (EMA) temporal smoothing.

```
+---------------------------------------------------------------------------------------------------+
|                                      VOICEGUARD PIPELINE OVERVIEW                                 |
+---------------------------------------------------------------------------------------------------+
|                                                                                                   |
|  [ Inbound Call ] ---> [ Android CallScreeningService ] ---> Pre-Call Threat Intelligence Lookup  |
|                                     |                                                             |
|                                     v                                                             |
|                           [ Call Connected ]                                                      |
|                                     |                                                             |
|            +------------------------+-------------------------+                                  |
|            |                                                  |                                  |
|            v                                                  v                                  |
|   [ Acoustic Analysis ]                              [ Dialogue Analysis ]                        |
|   - 1.5s Sliding Window                              - Indic ASR (Hi/Mr/En)                      |
|   - 80-bin Log-Mel Spectrogram                       - Social-Engineering Classifier             |
|   - SpectroCNNAttention + AASIST                     - Credential/UPI/Arrest Extraction          |
|   - F0 Pitch Dynamics (YIN/Autocorr)                 - Urgency & Pressure Tactic Flags           |
|            |                                                  |                                  |
|            +------------------------+-------------------------+                                  |
|                                     |                                                             |
|                                     v                                                             |
|                       [ 5-Factor Risk Fusion Engine ]                                             |
|              Risk = 0.35 V + 0.20 C + 0.15 S + 0.20 M + 0.10 H                                    |
|                         (EMA Temporal Smoothing: a=0.40)                                          |
|                                     |                                                             |
|                                     v                                                             |
|    +--------------------------------+--------------------------------+                            |
|    |                                |                                |                            |
|    v                                v                                v                            |
| [ 0-29: SAFE ]             [ 30-79: CAUTION/SUSP ]       [ 80-100: CRITICAL ATTACK ]              |
| Standard Call Display      Vigilance Banners             Audio Warning Overlay                    |
| Minimal Intervention       Step-Up Verification Advice   Independent Callback Protocol            |
|                                                          Post-Call 7-Stage Reconstruction         |
|                                                          One-Tap Chakshu / 1930 Report            |
+---------------------------------------------------------------------------------------------------+
```

---

## 2. Component Specifications

### 2.1 Android Client (`android/app`)
- **Language**: Kotlin 2.0.21
- **UI Toolkit**: Jetpack Compose with Material 3 (Dark-First Cybersecurity Palette)
- **Architecture**: Clean Architecture + MVVM + Repository Pattern
- **Telecom Integration**:
  - `VoiceGuardCallScreeningService`: Intercepts calls via Android Telecom framework, screening numbers prior to ringing.
  - `TelecomRoleHelper`: Manages `RoleManager.ROLE_CALL_SCREENING`.
  - Realistic Transport Handling: Distinguishes native telephony where remote audio is protected by the OS from VoIP and controlled simulation streams.
- **Local Persistence**:
  - Encrypted `Room` database with 10 entities (`CallSession`, `Incident`, `ProtectedContact`, `Caller`, etc.).
  - Zero raw audio retention: Waveforms are processed strictly in ephemeral RAM buffers and destroyed after feature extraction.

### 2.2 Machine Learning Engine (`ml-engine/`)
- **SpectroCNNAttention**:
  - 4 convolutional blocks with Squeeze-and-Excitation (SE) channel attention.
  - Temporal multi-head self-attention pooling across time frames.
  - Target: Detects synthetic vocoder artifacts in frequency bands > 3.2 kHz.
- **AASIST-Light**:
  - Graph-attention architecture analyzing spectral-temporal node correlations to detect phase vocoder discontinuities.
- **SpeakerVerifier**:
  - 192-dimensional d-vector embedding extractor for cosine similarity comparison against enrolled trusted contacts.
- **Telephony Degradation Augmentation**:
  - G.711 / GSM bandpass filtering (300Hz - 3400Hz), 8kHz decimation/resampling, additive background babble, and burst packet loss simulation.
- **Zero Speaker Leakage**:
  - Training, validation, and test splits partition disjoint speaker IDs to prevent data contamination.

### 2.3 Python Backend & Streaming Service (`backend/`)
- **FastAPI** microservice exposing REST and streaming WebSocket endpoints (`/ws/live-analysis`).
- **Indic Multilingual NLU**:
  - Regex and keyword semantics tuned for Indian fraud tactics: OTP/PIN harvesting, fake Mumbai police / Digital Arrest threats, bank manager KYC expiration, and family medical emergencies.
- **Enterprise & Banking Integration API**:
  - `/api/v1/telecom/screen`: Real-time call screening decision for Telco IMS cores.
  - `/api/v1/banking/verify-transaction-call`: Real-time coercion verification for banks confirming high-value RTGS/NEFT transfers.

---

## 3. Mathematical Formulation of Risk Fusion

The composite risk score $R \in [0, 100]$ is evaluated for every 1.5-second chunk:

$$R_{\text{raw}} = \frac{w_v V + w_c C + w_s S + w_m M + w_h H}{w_v + w_c + w_s + w_m + w_h} \times 100$$

Where:
- $V \in [0, 1]$: Synthetic Voice / Vocoder Artifact Probability ($w_v = 0.35$)
- $C \in [0, 1]$: Caller Reputation Risk ($w_c = 0.20$)
- $S \in [0, 1]$: Speaker Embedding Mismatch Risk ($w_s = 0.15$, reallocated if contact is unenrolled)
- $M \in [0, 1]$: Conversational Social Engineering Risk ($w_m = 0.20$)
- $H \in [0, 1]$: Historical Incident Risk ($w_h = 0.10$)

**Cross-Signal Amplification**:
- If $V > 0.65$ and $M > 0.50$, $R_{\text{raw}} \leftarrow \min(100, R_{\text{raw}} \times 1.25)$ (Synthetic voice combined with financial coercion).
- If claimed identity matches a trusted contact but $S > 0.50$, $R_{\text{raw}} \leftarrow \min(100, R_{\text{raw}} \times 1.30)$ (Impersonation of known family member).

**Temporal Smoothing**:
To prevent score flickering across conversational pauses, Exponential Moving Average (EMA) is applied:

$$R_t = \alpha R_{\text{raw}} + (1 - \alpha) R_{t-1} \quad (\alpha = 0.40)$$
