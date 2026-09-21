# VoiceGuard — 5-Slide SIH Grand Finale Pitch Deck Outline
## Smart India Hackathon 2026 — Problem Statement 26104
### AICTE Cyber Security Cell

---

### SLIDE 1: The Crisis — The AI Voice Cloning Epidemic in India
- **The Hook**: ₹1,750+ Crores lost in India to digital arrest, family emergency voice clones, and KYC extortion in 2024–2025.
- **The Core Flaw in Existing Defenses**: Truecaller and spam filters rely on static phone numbers. Attackers use 5-second audio samples from Instagram/WhatsApp to clone trusted family voices, calling from brand-new disposable numbers.
- **The Challenge (AICTE 26104)**: Real-time, on-device detection of AI voice cloning and impersonation attacks across Indian multilingual environments without invading citizen privacy.

---

### SLIDE 2: Our Innovation — Detect the Attack, Not Just the Phone Number
- **Product Definition**: VoiceGuard is an AI-powered real-time voice integrity and impersonation defense layer.
- **The Core Differentiator**: It does not ask *"Is this number spam?"* It answers:
  > *"Is this caller really who they claim to be, is this conversation becoming dangerous, and how do we protect the user before money is lost?"*
- **Defense-In-Depth Fusion**:
  $$\text{Voice Forensics} + \text{Caller Intel} + \text{Speaker Consistency} + \text{Indic Semantic Risk} + \text{Historical Context}$$

---

### SLIDE 3: Deep Technical Architecture
- **Spectro-Temporal Forensics**: 2D SpectroCNNAttention + AASIST-Light graph attention models detecting neural vocoder boundary discontinuities above 3.2 kHz.
- **Speaker Consistency Engine**: 192-dimensional d-vector embeddings to verify trusted family voiceprints (Mom/Dad) via cosine distance.
- **Indic Multilingual NLU**: Real-time semantic analysis in Hindi, Marathi, and English detecting OTP, PIN, UPI, and Digital Arrest coercion tactics.
- **Calibrated Risk Fusion**: Real-time 0-100 risk score with Exponential Moving Average (EMA) temporal smoothing.

---

### SLIDE 4: User Experience & Explainable AI (XAI)
- **Cinematic Dynamic Risk Ring**: Live color transitions (Green $24 \to$ Yellow $47 \to$ Orange $72 \to$ Crimson $91$).
- **Explainability**: Visual spectrogram heatmap highlighting vocoder anomalies and robotic pitch rigidity (F0 std = 6.2 Hz).
- **Proactive Protection**: Actionable safe verification (out-of-band callback) instead of blunt blocking.
- **7-Stage Attack Reconstruction**: Post-call forensics breaking down the social-engineering sequence for user education and legal evidence.

---

### SLIDE 5: Privacy, National Scale & Regulatory Integration
- **Zero Raw Audio Retention**: Ephemeral in-RAM processing; raw waveforms are never recorded or stored. Fully compliant with India's DPDP Act 2023.
- **National Security Ecosystem**: One-tap reporting to **Chakshu (Sanchar Saathi)** and the National Cyber Crime Reporting Portal (**1930 Helpline**).
- **Dual Deployment**:
  1. Consumer Android Mobile App (Offline-First Edge Inference).
  2. B2B Enterprise / Telco API for IMS call screening and bank transfer protection.
