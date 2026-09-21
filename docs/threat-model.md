# VoiceGuard Threat Model & Attack Taxonomy
## Smart India Hackathon 2026 — Problem Statement 26104
### AICTE Cyber Security Cell

---

## 1. Threat Landscape & Motivation

Voice cloning attacks targeting Indian citizens have surged due to the proliferation of few-shot zero-shot neural vocoders (e.g. Tortoise, XTTS, HiFi-GAN, VITS). Scammers extract 3 to 10 seconds of speech from social media or brief inquiry calls to generate convincing voice clones of family members or executives.

---

## 2. STRIDE Threat Model

| Threat Category | Attack Vector | Adversary Objective | VoiceGuard Mitigation |
| :--- | :--- | :--- | :--- |
| **Spoofing Identity** | Few-shot voice cloning of trusted contact (e.g., Mom or CEO) | Impersonate trusted individual to bypass suspicion | Speaker consistency embedding verification ($S$) against enrolled baseline |
| **Tampering** | Codec downsampling, audio compression to mask synthetic artifacts | Disguise vocoder phase distortion | Telephony-degradation-augmented ML models trained on 8kHz GSM conditions |
| **Repudiation** | Caller disconnects after harvesting OTP or money | Evade detection and investigation | Automatic 7-stage Attack Reconstruction & one-tap export to Cybercrime (1930) / Chakshu |
| **Information Disclosure** | Social engineering coercion targeting OTP/PIN/banking info | Credential theft | Real-time Indic Multilingual NLU flagging OTP, PIN, UPI, and Digital Arrest phrases |
| **Denial of Service** | High-volume robocall campaigns from VOIP trunks | Overwhelm victim | Telecom `CallScreeningService` filtering numbers with high fraud reputation |
| **Elevation of Privilege** | Digital Arrest / CBI impersonation claiming arrest warrants | Coerce victim through fake legal authority | Automatic legal authority disclaimer ("Law enforcement never conducts online arrest/extortion") |

---

## 3. Attack Vector Taxonomy

```
                          AI VOICE ATTACK TAXONOMY
                                      |
         +----------------------------+----------------------------+
         |                                                         |
         v                                                         v
   SYNTHETIC GENERATION                                    DELIVERY TRANSPORT
   - Text-to-Speech (TTS) Vocoding                         - Cellular Telephony (VoLTE)
   - Real-Time Voice Conversion (VC)                       - VoIP / SIP Gateways
   - Replay of Prior Recorded Audio                        - Messaging Audio Notes
         |                                                         |
         +----------------------------+----------------------------+
                                      |
                                      v
                           SOCIAL-ENGINEERING TACTIC
                           - Family Emergency ("Hospital bills")
                           - Digital Arrest ("Narcotics courier found")
                           - Bank KYC Block ("Account suspended today")
                           - Corporate Wire ("Confidential CEO order")
```

---

## 4. Honest Technical Limitations & OS Security Boundaries

1. **Cellular Audio Sandboxing**:
   Android OS strictly protects the bidirectional remote audio stream on standard carrier calls for third-party applications.
   *VoiceGuard is architected to operate transparently*:
   - On carrier calls, pre-call screening is executed via `CallScreeningService` and accessibility/overlay warnings engage.
   - For VoIP/Enterprise calls and controlled simulations, full sliding-window audio feature extraction is performed.
   - If carrier hardware does not yield audio access, VoiceGuard displays:
     > *"Voice stream unavailable for this call transport. Other protection signals (reputation, context, verification) remain active."*
   This guarantees technical honesty before SIH Grand Finale judges.
