# VoiceGuard — 5-Minute SIH Grand Finale Live Demo Script
## Problem Statement 26104 (AICTE Cyber Security Cell)
### "AI-Powered Real-Time Detection and Prevention of Voice Cloning Impersonation Attacks"

---

## Pre-Flight Checklist
- [x] Android APK installed or running in emulator / test device.
- [x] Demo mode active (Offline-first: no internet required).
- [x] Initial seeded records loaded (Mom voice profile enrolled, threat intelligence active).

---

## The 5-Minute Winning Pitch Walkthrough

### 00:00 - 00:45: The Problem & Vision
> *"Respected Jury, conventional Truecaller-style apps only check if a phone number is flagged as spam. But today, criminals do not need to use flagged numbers. With modern AI zero-shot vocoders, an attacker can clone your mother's, father's, or CEO's voice with just 5 seconds of audio, call from a fresh disposable SIM, and demand emergency money.
>
> Our solution is **VoiceGuard**. Our core principle is:
> **Detect the impersonation attack, not just the phone number.**"*

---

### 00:45 - 01:15: Dashboard & Hero Status
1. Open the VoiceGuard app.
2. Point out:
   - **"VOICEGUARD ACTIVE"** hero card with animated shield pulse.
   - **Voice Security Score: 82 / 100** based on enrolled family profiles and active screening.
   - Today's stats: 12 calls analyzed, 1 high-risk incident stopped.
   - Transparent zero audio retention guarantee.

---

### 01:15 - 02:30: Live Attack Simulation (WOW Moments #1, #2, #3, #5, #6)
1. Tap the big crimson button: **"SIMULATE INCOMING ATTACK CALL"**.
2. Explain the scenario:
   - *"The incoming call displays 'Mom' because the caller claims to be the user's mother, but it is calling from an unknown disposable number (+91 91234 56789)."*
3. The call connects and analysis begins:
   - **T = 2s**: Initial score **24** (GREEN). Audio arrives.
     - Marathi transcript appears: *"हॅलो, बाळा... माझा फोन चोरीला गेला आहे, मी दुसऱ्या नंबरवरून फोन करत आहे..."*
   - **T = 5s**: Score jumps to **47** (YELLOW).
     - Pitch rigidity detected in vocal envelope.
     - Transcript: *"मी हॉस्पिटलच्या जवळ एका अडचणीत अडकले आहे..."*
   - **T = 8s**: Score escalates to **72** (ORANGE - SUSPICIOUS).
     - Transcript: *"लगेच या नंबरवर गुगल पे ने वीस हजार रुपये पाठवा, अत्यंत तातडीची गरज आहे!"*
     - Warning: Speaker voice cosine similarity is only 34% compared to Mom's enrolled baseline!
   - **T = 12s**: Score surges to **91** (CRIMSON - CRITICAL IMPERSONATION RISK).
     - Transcript: *"कोणालाही सांगू नको, त्वरित पैसे पाठव नाहीतर डॉक्टर उपचार थांबवतील!"*
4. Highlight to judges:
   - *"Notice how the Dynamic Risk Ring seamlessly evolved from 24 to 91 in real-time as acoustic vocoder artifacts fused with Marathi urgency analysis!"*
   - The screen flashes: **DO NOT TRANSFER MONEY. Voice cloning confirmed.**

---

### 02:30 - 03:15: Explainable AI & Safe Verification (WOW #4, #7)
1. Tap **"Why Flagged?"**:
   - The Explainable AI modal appears.
   - Show the **Acoustic Spectrogram**: Highlight the red band showing abnormal vocoder phase discontinuities above 3.2 kHz.
   - Point to the **Forensic Checklist**:
     - ✓ High-frequency vocoder phase distortion
     - ✓ Pitch standard deviation rigid at 6.2 Hz (robotic synthesis)
     - ✓ Speaker similarity anomaly (34%)
     - ✓ Social-engineering urgency trigger
2. Dismiss the modal and tap **"Verify Caller"**:
   - Show the safe verification recommendation: Instead of blindly blocking, VoiceGuard triggers an independent callback to Mom's verified enrolled number stored in the device phonebook.

---

### 03:15 - 04:00: Attack Reconstruction & Incident Report (WOW #8)
1. Tap **"Hang Up & Reconstruct Attack"**:
   - The **Attack Reconstruction** screen opens.
2. Walk the jury through the 7 psychological stages:
   - *Stage 1: Identity Claim*
   - *Stage 2: Trust Manipulation*
   - *Stage 3: Urgency Coercion*
   - *Stage 4: Isolation Tactic ("Don't tell anyone")*
   - *Stage 5: Financial Demand (₹20,000 via UPI)*
   - *Stage 6: AI Voice Evidence (Vocoder distortion)*
   - *Stage 7: VoiceGuard Intervention (Transfer blocked)*
3. Tap **"Incident Report"**:
   - Show generated report with Incident ID (`VG-INC-XXXXX`).
   - Demonstrate **Export JSON** and **Export CSV**.
   - Show 1-tap submission to the national **Chakshu (Sanchar Saathi)** and **1930 Cybercrime Helpline**.

---

### 04:00 - 04:45: Attack Lab & Privacy Center (WOW #9, #10)
1. Open the **Attack Lab**:
   - Show the 8 attack scenarios: Bank Officer KYC, Digital Arrest / Police Coercion, CEO Wire Fraud, Investment Scam, etc.
   - Demonstrate that judges can test any attack vector on demand.
2. Open the **Privacy Center**:
   - Prove that raw audio retention is strictly toggled OFF.
   - Show that processing happens on-device in RAM and demonstrate the **One-Tap Data Wipe**.

---

### 04:45 - 05:00: Conclusion & Impact
> *"VoiceGuard is not a conceptual mockup. It is a complete, functioning security system ready for deployment across Indian citizens, banking systems, and telecom operators. Thank you!"*
