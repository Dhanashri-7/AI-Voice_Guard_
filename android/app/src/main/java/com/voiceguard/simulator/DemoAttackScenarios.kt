package com.voiceguard.simulator

import com.voiceguard.domain.model.*

object DemoAttackScenarios {

    val scenarios: List<AttackScenario> = listOf(
        // SCENARIO 0A: Live Teammate Call - Safe Baseline (Hii Hello Normal Call)
        AttackScenario(
            id = "scenario_teammate_normal",
            title = "Teammate Live Call: Normal Speech (Safe)",
            subtitle = "Live incoming call from teammate with natural conversational voice harmonics",
            incomingNumber = "+91 98221 55443", // Teammate's live phone number
            callerName = "Teammate (Live Call)",
            claimedIdentity = "Teammate",
            callerReputation = 0.05f,
            language = "mr", // Marathi & English
            attackType = "Genuine Human Conversation",
            expectedThreatLevel = RiskTier.SAFE,
            dialogueSteps = listOf(
                CallEventStep(
                    timeOffsetSeconds = 2,
                    speaker = "Teammate",
                    text = "हॅलो धनश्री, कशी आहेस? आजच्या हॅकाथॉन प्रेझेंटेशनची तयारी झाली का?",
                    targetRiskScore = 12,
                    voiceAuthenticity = 0.98f,
                    detectedIntent = "CASUAL_GREETING",
                    urgency = "LOW",
                    alertMessage = null
                ),
                CallEventStep(
                    timeOffsetSeconds = 5,
                    speaker = "Teammate",
                    text = "मी सर्व स्लाइड्स आणि प्रोटोटाईप चेक केले आहेत, सर्व व्यवस्थित काम करत आहे.",
                    targetRiskScore = 14,
                    voiceAuthenticity = 0.96f,
                    detectedIntent = "NORMAL_DISCUSSION",
                    urgency = "LOW",
                    alertMessage = null
                ),
                CallEventStep(
                    timeOffsetSeconds = 9,
                    speaker = "Teammate",
                    text = "छान! ज्युरींसमोर भेटूया. ऑल द बेस्ट!",
                    targetRiskScore = 12,
                    voiceAuthenticity = 0.98f,
                    detectedIntent = "FAREWELL",
                    urgency = "LOW",
                    alertMessage = null
                )
            ),
            reconstructionStages = listOf(
                AttackStage(1, "Natural Pitch Variance", "Dynamic F0 modulation (std dev 26.4 Hz) indicates human vocal cords.", "Human F0 Harmonic OK"),
                AttackStage(2, "No Vocoder Phase Cutoff", "Natural high-frequency audio decay without neural vocoder truncation.", "Spectrogram: 100% Organic"),
                AttackStage(3, "Zero Coercion", "No OTP requests, financial demands, or panic induction detected.", "Semantic Score: Clean"),
                AttackStage(4, "System Status", "Risk Score: 12/100 (Safe). Call classified as authentic human.", "SAFE CALL CONFIRMED")
            )
        ),

        // SCENARIO 0B: Live Teammate Call - AI Voice Clone & Bank KYC Scam (Attack Test)
        AttackScenario(
            id = "scenario_teammate_ai_clone",
            title = "Teammate Live Call: AI Voice Clone (Attack)",
            subtitle = "Live incoming call from teammate simulating AI cloned voice with Bank KYC fraud",
            incomingNumber = "+91 98221 55443", // Teammate's live phone number
            callerName = "SBI Bank Manager (AI Clone)",
            claimedIdentity = "State Bank of India",
            callerReputation = 0.92f,
            language = "hi", // Hindi / Marathi
            attackType = "AI Voice Cloning + Bank KYC Extortion",
            expectedThreatLevel = RiskTier.CRITICAL_IMPERSONATION,
            dialogueSteps = listOf(
                CallEventStep(
                    timeOffsetSeconds = 2,
                    speaker = "SBI Manager (AI Clone)",
                    text = "नमस्कार, मैं भारतीय स्टेट बैंक मुख्य शाखा से सीनियर साइबर सिक्योरिटी मैनेजर बोल रहा हूँ।",
                    targetRiskScore = 32,
                    voiceAuthenticity = 0.65f,
                    detectedIntent = "BANK_AUTHORITY_CLAIM",
                    urgency = "LOW",
                    alertMessage = null
                ),
                CallEventStep(
                    timeOffsetSeconds = 5,
                    speaker = "SBI Manager (AI Clone)",
                    text = "आपके बैंक खाते और एटीएम डेबिट कार्ड पर संदेहास्पद फ्रॉड ट्रांजैक्शन पाया गया है, कार्ड तुरंत ब्लॉक कर दिया गया है!",
                    targetRiskScore = 68,
                    voiceAuthenticity = 0.32f,
                    detectedIntent = "PANIC_INDUCTION",
                    urgency = "HIGH",
                    alertMessage = "Warning: Abnormal pitch rigidity & neural vocoder synthesis anomaly detected."
                ),
                CallEventStep(
                    timeOffsetSeconds = 8,
                    speaker = "SBI Manager (AI Clone)",
                    text = "खाता तुरंत अनब्लॉक करने के लिए आपके मोबाइल पर भेजा गया 6-अंकों का गुप्त OTP बताइए, या ₹20,000 रिफंडेबल सेक्योरिटी ट्रांसफर कीजिए!",
                    targetRiskScore = 94,
                    voiceAuthenticity = 0.08f,
                    detectedIntent = "CREDENTIAL_THEFT_OTP",
                    urgency = "CRITICAL",
                    alertMessage = "CRITICAL ALERT: Voice Clone Confirmed — Do Not Transfer Money or Share OTP!"
                ),
                CallEventStep(
                    timeOffsetSeconds = 12,
                    speaker = "SBI Manager (AI Clone)",
                    text = "अगर आपने तुरंत OTP नहीं दिया तो आपका खाता स्थायी रूप से सील कर दिया जाएगा और कानूनी कार्रवाई होगी!",
                    targetRiskScore = 98,
                    voiceAuthenticity = 0.04f,
                    detectedIntent = "EXTORTION_COERCION",
                    urgency = "CRITICAL",
                    alertMessage = "CRITICAL ALERT: Voice Clone Confirmed — Do Not Transfer Money or Share OTP!"
                )
            ),
            reconstructionStages = listOf(
                AttackStage(1, "Vocoder Phase Artifact", "High-frequency phase discontinuity >3.2 kHz matching neural autoregressive TTS.", "Synthesis Prob: 96%"),
                AttackStage(2, "Rigid Vocal Envelope", "F0 pitch standard deviation of only 4.8 Hz indicates artificial neural prosody.", "Pitch Rigidity Detected"),
                AttackStage(3, "Financial Credential Demand", "Demands immediate 6-digit OTP and bank funds transfer.", "Critical Keyword: OTP/Card"),
                AttackStage(4, "Urgency Coercion", "Threatens immediate account closure and legal arrest.", "Coercion Flag: CRITICAL"),
                AttackStage(5, "Active Defense Triggered", "Risk score: 94/100. Strong haptic pulses fired, Reverse Voice-CAPTCHA deployed.", "Transfer Blocked"),
                AttackStage(6, "Consortium Ledger", "Cryptographic SHA-256 Merkle root committed to AICTE, CERT-In, DoT, NPCI ledger.", "Section 65B Anchored"),
                AttackStage(7, "Golden Hour Action", "One-tap dispatch prepared for Sanchar Saathi & MHA 1930 account freeze.", "Golden Hour Ready")
            )
        ),

        // SCENARIO 1: Family Member Voice Clone (Mom) - Primary SIH WOW Demo
        AttackScenario(
            id = "scenario_family_clone",
            title = "Family Member Voice Clone (Mom)",
            subtitle = "Cloned voice of trusted family member requesting emergency transfer",
            incomingNumber = "+91 91234 56789", // Unknown number claiming to be Mom
            callerName = "Mom",
            claimedIdentity = "Mom",
            callerReputation = 0.40f,
            language = "mr", // Marathi
            attackType = "AI Voice Cloning + Emergency Fraud",
            expectedThreatLevel = RiskTier.CRITICAL_IMPERSONATION,
            dialogueSteps = listOf(
                CallEventStep(
                    timeOffsetSeconds = 2,
                    speaker = "Mom (Claimed)",
                    text = "हॅलो, बाळा... माझा फोन चोरीला गेला आहे, मी दुसऱ्या नंबरवरून फोन करत आहे...",
                    targetRiskScore = 24,
                    voiceAuthenticity = 0.76f,
                    detectedIntent = "IDENTITY_CLAIM",
                    urgency = "LOW",
                    alertMessage = null
                ),
                CallEventStep(
                    timeOffsetSeconds = 5,
                    speaker = "Mom (Claimed)",
                    text = "मी हॉस्पिटलच्या जवळ एका अडचणीत अडकले आहे...",
                    targetRiskScore = 47,
                    voiceAuthenticity = 0.52f,
                    detectedIntent = "TRUST_MANIPULATION",
                    urgency = "MEDIUM",
                    alertMessage = "Caution: Pitch rigidity detected in vocal envelope."
                ),
                CallEventStep(
                    timeOffsetSeconds = 8,
                    speaker = "Mom (Claimed)",
                    text = "लगेच या नंबरवर गुगल पे ने वीस हजार रुपये पाठवा, अत्यंत तातडीची गरज आहे!",
                    targetRiskScore = 86,
                    voiceAuthenticity = 0.18f,
                    detectedIntent = "FINANCIAL_FRAUD",
                    urgency = "HIGH",
                    alertMessage = "CRITICAL ALERT: Voice Clone Confirmed — Do Not Transfer Money or Share OTP!"
                ),
                CallEventStep(
                    timeOffsetSeconds = 12,
                    speaker = "Mom (Claimed)",
                    text = "कोणालाही सांगू नको, त्वरित पैसे पाठव नाहीतर डॉक्टर उपचार थांबवतील!",
                    targetRiskScore = 93,
                    voiceAuthenticity = 0.06f,
                    detectedIntent = "CRITICAL_COERCION",
                    urgency = "CRITICAL",
                    alertMessage = "CRITICAL ALERT: Voice Clone Confirmed — Do Not Transfer Money or Share OTP!"
                )
            ),
            reconstructionStages = listOf(
                AttackStage(1, "Caller Identity Claim", "Caller claims to be 'Mom' from an unknown disposable SIM card.", "Unknown Number +91 91234 56789"),
                AttackStage(2, "Trust Manipulation", "Fabricated medical emergency to exploit emotional vulnerability.", "Psychological urgency trigger"),
                AttackStage(3, "Urgency Coercion", "Demands immediate action ('त्वरित वीस हजार पाठवा') to bypass cognitive checks.", "Urgency Flag: HIGH"),
                AttackStage(4, "Isolation Tactic", "Instructs user not to inform other family members.", "'कोणालाही सांगू नको'"),
                AttackStage(5, "Financial Demand", "Demands instant UPI transfer of ₹20,000 to an unverified VPA.", "Financial Request: YES"),
                AttackStage(6, "AI Voice Evidence", "Vocoder phase discontinuity above 3.2kHz; speaker cosine similarity to Mom's profile is only 0.34.", "Voice Authenticity: 8%"),
                AttackStage(7, "VoiceGuard Intervention", "Real-time risk score reached 91/100. Screen transitioned to Critical Warning, blocking fraudulent transfer.", "Recommended Action: Independent callback to saved number")
            )
        ),

        // SCENARIO 2: Bank Officer KYC Impersonation (SBI Manager)
        AttackScenario(
            id = "scenario_bank_kyc",
            title = "Bank Officer KYC Impersonation",
            subtitle = "Impersonation of SBI Branch Manager threatening account suspension",
            incomingNumber = "+91 98765 43210",
            callerName = "SBI Bank Manager",
            claimedIdentity = "State Bank of India",
            callerReputation = 0.85f,
            language = "hi", // Hindi
            attackType = "Credential Theft + Banking Extortion",
            expectedThreatLevel = RiskTier.CRITICAL_IMPERSONATION,
            dialogueSteps = listOf(
                CallEventStep(
                    timeOffsetSeconds = 2,
                    speaker = "SBI Officer",
                    text = "नमस्कार, मैं भारतीय स्टेट बैंक के मुख्य शाखा से सीनियर मैनेजर बोल रहा हूँ।",
                    targetRiskScore = 32,
                    voiceAuthenticity = 0.65f,
                    detectedIntent = "AUTHORITY_CLAIM",
                    urgency = "LOW",
                    alertMessage = null
                ),
                CallEventStep(
                    timeOffsetSeconds = 6,
                    speaker = "SBI Officer",
                    text = "आपके बैंक खाते का केवाईसी अपडेट नहीं हुआ है, आज शाम तक आपका खाता और डेबिट कार्ड ब्लॉक हो जाएगा।",
                    targetRiskScore = 58,
                    voiceAuthenticity = 0.44f,
                    detectedIntent = "KYC_BANK_SCAM",
                    urgency = "HIGH",
                    alertMessage = "Warning: Known bank impersonation phraseology detected."
                ),
                CallEventStep(
                    timeOffsetSeconds = 10,
                    speaker = "SBI Officer",
                    text = "खाता चालू रखने के लिए आपके फोन पर 6 अंकों का ओटीपी आया है, तुरंत बताइए!",
                    targetRiskScore = 88,
                    voiceAuthenticity = 0.12f,
                    detectedIntent = "CREDENTIAL_THEFT",
                    urgency = "CRITICAL",
                    alertMessage = "CRITICAL ALERT: Voice Clone Confirmed — Do Not Transfer Money or Share OTP!"
                )
            ),
            reconstructionStages = listOf(
                AttackStage(1, "Authority Impersonation", "Claims to represent SBI Headquarters.", "Simulated Caller ID"),
                AttackStage(2, "Fabricated Problem", "Claims KYC expired and funds will be permanently frozen.", "Fear trigger"),
                AttackStage(3, "High Urgency", "Gives deadline of today evening.", "Time pressure"),
                AttackStage(4, "Credential Harvest", "Demands immediate 6-digit OTP under threat of suspension.", "OTP Request: DETECTED"),
                AttackStage(5, "Acoustic Forensics", "Monotone F0 pitch standard deviation (5.4 Hz), indicative of TTS voice generation.", "Synthesis Artifacts: Confirmed"),
                AttackStage(6, "Reputation Flag", "Number previously reported 142 times for financial fraud.", "Reputation: 0.92 Risk"),
                AttackStage(7, "VoiceGuard Intervention", "Warning overlay deployed; independent verification initiated.", "Blocked OTP disclosure")
            )
        ),

        // SCENARIO 3: Police / Digital Arrest Threat (CBI / Crime Branch)
        AttackScenario(
            id = "scenario_police_arrest",
            title = "Digital Arrest / CBI Extortion",
            subtitle = "Fake cyber police officer threatening immediate arrest warrant",
            incomingNumber = "+91 88888 99999",
            callerName = "Mumbai Cyber Police",
            claimedIdentity = "CBI Cyber Cell",
            callerReputation = 0.95f,
            language = "hi",
            attackType = "Digital Arrest Coercion",
            expectedThreatLevel = RiskTier.CRITICAL_IMPERSONATION,
            dialogueSteps = listOf(
                CallEventStep(
                    timeOffsetSeconds = 2,
                    speaker = "Inspector Deshmukh",
                    text = "मैं मुंबई पुलिस क्राइम ब्रांच से बोल रहा हूँ, आपके नाम पर अवैध पार्सल पकड़ा गया है।",
                    targetRiskScore = 38,
                    voiceAuthenticity = 0.60f,
                    detectedIntent = "AUTHORITY_IMPERSONATION",
                    urgency = "MEDIUM",
                    alertMessage = null
                ),
                CallEventStep(
                    timeOffsetSeconds = 6,
                    speaker = "Inspector Deshmukh",
                    text = "आपके खिलाफ गैर-जमानती गिरफ्तारी वारंट जारी हुआ है, अभी फोन काटोगे तो पुलिस घर पहुंचेगी।",
                    targetRiskScore = 74,
                    voiceAuthenticity = 0.35f,
                    detectedIntent = "ARREST_THREAT",
                    urgency = "CRITICAL",
                    alertMessage = "SUSPICIOUS: Digital arrest intimidation tactic detected."
                ),
                CallEventStep(
                    timeOffsetSeconds = 10,
                    speaker = "Inspector Deshmukh",
                    text = "मामला रफा-दफा करने के लिए तुरंत 50,000 रुपये सिक्योरिटी डिपॉजिट ट्रांसफर करें।",
                    targetRiskScore = 95,
                    voiceAuthenticity = 0.10f,
                    detectedIntent = "EXTORTION",
                    urgency = "CRITICAL",
                    alertMessage = "CRITICAL FRAUD: Government agencies never conduct 'Digital Arrests' or demand online money transfers."
                )
            ),
            reconstructionStages = listOf(
                AttackStage(1, "Police Pretence", "Impersonates Police Inspector from Mumbai Cyber Cell.", "False badge claim"),
                AttackStage(2, "False Crime Allegation", "Claims illegal narcotics found in a courier parcel.", "Intimidation"),
                AttackStage(3, "Digital Arrest Threat", "Threatens physical police raid if call is disconnected.", "Isolation tactic"),
                AttackStage(4, "Extortion Demand", "Demands ₹50,000 'security deposit' via UPI.", "Financial Extortion"),
                AttackStage(5, "Forensics", "Artificial harmonic phase jitter matching open-source neural TTS.", "Voice authenticity: 10%"),
                AttackStage(6, "Government Rule", "India Cyber Security Cell guidelines confirm Digital Arrest is 100% illegal.", "AICTE Rule PS 26104"),
                AttackStage(7, "Resolution", "Auto-logged incident for one-tap Sanchar Saathi & 1930 reporting.", "Report Prepared")
            )
        ),

        // SCENARIO 4: CEO / Executive Impersonation
        AttackScenario(
            id = "scenario_ceo_wire",
            title = "CEO / Executive Voice Clone",
            subtitle = "Cloned voice of corporate executive demanding urgent vendor wire",
            incomingNumber = "+91 99001 22334",
            callerName = "Vikram Malhotra (CEO)",
            claimedIdentity = "Company CEO",
            callerReputation = 0.50f,
            language = "en",
            attackType = "Executive Voice Spoofing",
            expectedThreatLevel = RiskTier.CRITICAL_IMPERSONATION,
            dialogueSteps = listOf(
                CallEventStep(
                    timeOffsetSeconds = 2,
                    speaker = "CEO (Voice Clone)",
                    text = "Hi, this is Vikram. I'm currently in a confidential board meeting in London.",
                    targetRiskScore = 28,
                    voiceAuthenticity = 0.70f,
                    detectedIntent = "EXECUTIVE_CLAIM",
                    urgency = "LOW",
                    alertMessage = null
                ),
                CallEventStep(
                    timeOffsetSeconds = 6,
                    speaker = "CEO (Voice Clone)",
                    text = "We have an urgent acquisition closing right now. I need you to wire the initial deposit immediately.",
                    targetRiskScore = 65,
                    voiceAuthenticity = 0.38f,
                    detectedIntent = "FINANCIAL_URGENCY",
                    urgency = "HIGH",
                    alertMessage = "Warning: Unusual high-urgency financial directive from executive."
                ),
                CallEventStep(
                    timeOffsetSeconds = 10,
                    speaker = "CEO (Voice Clone)",
                    text = "Process the 5 lakh rupees payment to the new vendor account right away, do not delay.",
                    targetRiskScore = 89,
                    voiceAuthenticity = 0.15f,
                    detectedIntent = "WIRE_FRAUD",
                    urgency = "CRITICAL",
                    alertMessage = "CRITICAL RISK: Voice clone detected. High-frequency synthesis artifacts confirmed."
                )
            ),
            reconstructionStages = listOf(
                AttackStage(1, "Executive Identity Claim", "Caller mimics corporate CEO voice.", "Targeted spear phishing"),
                AttackStage(2, "Artificial Secrecy", "Claims to be in confidential board meeting.", "Circumvents dual authorization"),
                AttackStage(3, "Urgent Wire Directive", "Directs employee to wire funds immediately.", "High-value wire fraud"),
                AttackStage(4, "Forensic Anomaly", "AASIST spectro-temporal graph detector flags abnormal phase boundaries.", "Vocoder Artifact: 85%"),
                AttackStage(5, "Enterprise Safeguard", "Enterprise API integration triggers step-up verification.", "Prevented wire release"),
                AttackStage(6, "Corporate Audit", "Incident logged to enterprise security dashboard.", "SIEM Event Created"),
                AttackStage(7, "Recommended Action", "Independent out-of-band video verification required.", "Dual Key Required")
            )
        ),

        // SCENARIO 5: Normal Genuine Caller (Safe Baseline)
        AttackScenario(
            id = "scenario_genuine_colleague",
            title = "Normal Genuine Caller (Colleague)",
            subtitle = "Routine professional conversation without suspicious acoustic or social indicators",
            incomingNumber = "+91 98201 55667",
            callerName = "Priya Sharma (Colleague)",
            claimedIdentity = "Colleague",
            callerReputation = 0.05f,
            language = "en",
            attackType = "Genuine Human Communication",
            expectedThreatLevel = RiskTier.SAFE,
            dialogueSteps = listOf(
                CallEventStep(
                    timeOffsetSeconds = 2,
                    speaker = "Priya",
                    text = "Hey, do you have a quick minute to review the quarterly security audit report before the cutoff?",
                    targetRiskScore = 8,
                    voiceAuthenticity = 0.98f,
                    detectedIntent = "CASUAL_INQUIRY",
                    urgency = "LOW",
                    alertMessage = null
                ),
                CallEventStep(
                    timeOffsetSeconds = 6,
                    speaker = "Priya",
                    text = "I added the architecture diagrams we discussed earlier. Let me know what you think.",
                    targetRiskScore = 12,
                    voiceAuthenticity = 0.95f,
                    detectedIntent = "PROJECT_DISCUSSION",
                    urgency = "LOW",
                    alertMessage = null
                ),
                CallEventStep(
                    timeOffsetSeconds = 10,
                    speaker = "Priya",
                    text = "Great, talk to you later during the team sync!",
                    targetRiskScore = 10,
                    voiceAuthenticity = 0.97f,
                    detectedIntent = "FAREWELL",
                    urgency = "LOW",
                    alertMessage = null
                )
            ),
            reconstructionStages = listOf(
                AttackStage(1, "Natural Human Speech", "Rich harmonic spectral envelope with natural pitch modulation.", "F0 std: 24.2 Hz"),
                AttackStage(2, "Reputable Caller", "Contact in user phonebook with zero spam reports.", "Reputation: Clean"),
                AttackStage(3, "Zero Coercion", "No credential requests, urgency tactics, or financial demands.", "Semantic Risk: 0.0"),
                AttackStage(4, "System Status", "Risk score remained at 10/100 (Safe).", "Status: PROTECTED")
            )
        )
    )
}
