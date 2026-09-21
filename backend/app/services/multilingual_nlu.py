"""
VoiceGuard ML Engine & Backend - Indic Multilingual Semantic Risk Engine
Detects social engineering, psychological pressure, financial extortion,
and impersonation triggers in English, Hindi, and Marathi.
"""

import re
from typing import Dict, Any, List

class MultilingualNLUEngine:
    def __init__(self):
        # High-risk financial & credential keywords (English + Hindi + Marathi)
        self.credential_patterns = [
            r"\b(otp|one time password|pin|mpin|cvv|password|passcode)\b",
            r"(ओटीपी|पिन|पासवर्ड|सीवीवी|गुपित क्रमांक|otp)"
        ]

        # Financial transfer & UPI patterns
        self.financial_patterns = [
            r"\b(transfer|send money|gpay|phonepe|paytm|upi|neft|rtgs|rupees|rs|lakh|crore)\b",
            r"(पैसे भेजो|पैसे डालो|रुपये|रुपए|ट्रांसफर|पेटीएम|गूगल पे|पैसे पाठवा|खात्यात टाका|पैसे भरा)"
        ]

        # Urgency and coercion patterns
        self.urgency_patterns = [
            r"\b(immediately|right now|urgent|within 10 minutes|hurry|emergency|blocked today)\b",
            r"(तुरंत|अभी के अभी|जल्दी|तत्काल|10 मिनट|बंद हो जाएगा|त्वरित|आत्ताच|लगेच|धोक्यात|खाते बंद होईल)"
        ]

        # Authority & Digital Arrest / Police coercion patterns
        self.authority_patterns = [
            r"\b(cbi|police|cyber cell|rbi|customs|court|arrest warrant|fir|narcotics)\b",
            r"(पुलिस|सीबीआई|आरबीआई|अदालत|गिरफ्तारी|कस्टम्स|वारंट|पोलीस|न्यायालय|अटक|गुन्हे शाखा|कस्टम अधिकारी)"
        ]

        # Bank / KYC impersonation patterns
        self.kyc_bank_patterns = [
            r"\b(sbi|hdfc|icici|bank manager|kyc update|account suspend|pan card verify|sim block)\b",
            r"(बैंक मैनेजर|खाता ब्लॉक|केवाईसी|पैन कार्ड|सिम बंद|बँक व्यवस्थापक|खाते गोठवले)"
        ]

        # Secrecy & isolation coercion patterns
        self.secrecy_patterns = [
            r"\b(don't tell anyone|keep this confidential|stay on line|don't disconnect|secret)\b",
            r"(किसी को मत बताना|फोन मत काटना|लाइन पर रहो|गुप्त रखो|कोणालाही सांगू नका|फोन कापू नका|गुप्त ठेवा)"
        ]

    def detect_language(self, text: str) -> str:
        """Detects language script (Devanagari vs Latin)."""
        devanagari_count = len(re.findall(r'[\u0900-\u097F]', text))
        if devanagari_count > 3:
            # Distinguish Marathi specific words
            marathi_markers = ["आहे", "नाही", "करा", "पाठवा", "आलो", "गेलो", "होईल", "मी", "खात्यात", "लगेच"]
            for m in marathi_markers:
                if m in text:
                    return "mr"
            return "hi"
        return "en"

    def analyze_transcript(self, text: str) -> Dict[str, Any]:
        """
        Analyzes spoken dialogue transcript segment for social-engineering triggers.
        Returns risk scores and detected malicious intent markers.
        """
        if not text:
            return {
                "detected_language": "en",
                "semantic_risk_score": 0.0,
                "urgency_level": "LOW",
                "intents": [],
                "sensitive_info_requested": False,
                "financial_request": False,
                "authority_threat": False,
                "secrecy_demanded": False,
                "flagged_keywords": []
            }

        text_lower = text.lower()
        lang = self.detect_language(text)

        intents = []
        flagged_keywords = []
        risk_accum = 0.0

        # Check Credential theft
        for pat in self.credential_patterns:
            matches = re.findall(pat, text_lower, flags=re.IGNORECASE)
            if matches:
                intents.append("CREDENTIAL_THEFT")
                flagged_keywords.extend(matches)
                risk_accum += 0.40
                break

        # Check Financial transfer request
        for pat in self.financial_patterns:
            matches = re.findall(pat, text_lower, flags=re.IGNORECASE)
            if matches:
                intents.append("FINANCIAL_FRAUD")
                flagged_keywords.extend(matches)
                risk_accum += 0.30
                break

        # Check Urgency / Coercion
        urgency_found = False
        for pat in self.urgency_patterns:
            matches = re.findall(pat, text_lower, flags=re.IGNORECASE)
            if matches:
                intents.append("URGENCY_MANIPULATION")
                flagged_keywords.extend(matches)
                risk_accum += 0.20
                urgency_found = True
                break

        # Check Authority & Digital Arrest
        authority_found = False
        for pat in self.authority_patterns:
            matches = re.findall(pat, text_lower, flags=re.IGNORECASE)
            if matches:
                intents.append("AUTHORITY_IMPERSONATION")
                flagged_keywords.extend(matches)
                risk_accum += 0.35
                authority_found = True
                break

        # Check KYC / Bank Impersonation
        for pat in self.kyc_bank_patterns:
            matches = re.findall(pat, text_lower, flags=re.IGNORECASE)
            if matches:
                intents.append("KYC_BANK_SCAM")
                flagged_keywords.extend(matches)
                risk_accum += 0.25
                break

        # Check Secrecy Demands
        secrecy_found = False
        for pat in self.secrecy_patterns:
            matches = re.findall(pat, text_lower, flags=re.IGNORECASE)
            if matches:
                intents.append("ISOLATION_TACTIC")
                flagged_keywords.extend(matches)
                risk_accum += 0.20
                secrecy_found = True
                break

        semantic_risk = min(1.0, risk_accum)
        urgency_level = "CRITICAL" if (urgency_found and (authority_found or "CREDENTIAL_THEFT" in intents)) else ("HIGH" if urgency_found else "MEDIUM" if semantic_risk > 0.2 else "LOW")

        return {
            "detected_language": lang,
            "semantic_risk_score": round(semantic_risk, 3),
            "urgency_level": urgency_level,
            "intents": list(set(intents)),
            "sensitive_info_requested": "CREDENTIAL_THEFT" in intents,
            "financial_request": "FINANCIAL_FRAUD" in intents,
            "authority_threat": authority_found,
            "secrecy_demanded": secrecy_found,
            "flagged_keywords": list(set(flagged_keywords))
        }
