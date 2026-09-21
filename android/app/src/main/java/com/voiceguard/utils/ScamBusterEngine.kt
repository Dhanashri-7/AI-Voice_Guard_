package com.voiceguard.utils

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

enum class ScamBusterPersona(val displayName: String, val emoji: String, val tagline: String) {
    AAJI(
        displayName = "AI आजी (Dadi-ji)",
        emoji = "👵",
        tagline = "भोळी, गोड, पण स्कॅमरची पूर्ण फिरकी घेणारी!"
    ),
    KAKA(
        displayName = "AI काका (Tau-ji)",
        emoji = "👴",
        tagline = "कडक, वैतागलेला, आणि थेट नियमांवरून सुनावणारा!"
    )
}

enum class SpeakerRole {
    SCAMMER_AI,
    HONEYPOT_BOT
}

data class DialogueTurn(
    val turnIndex: Int,
    val speaker: SpeakerRole,
    val speakerDisplayName: String,
    val speechText: String,
    val subtitleText: String,
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val delayBeforeNextMs: Long = 2500L,
    val frustrationLevel: Int = 30
)

data class ScamBusterReply(
    val replyMarathi: String,
    val replyEnglish: String,
    val detectedCategory: String,
    val frustrationIncrease: Int
)

class ScamBusterEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    var isTtsReady: Boolean = false
        private set

    private var selectedLanguage: Locale = Locale("mr", "IN")
    private var currentOnComplete: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsEngine = tts ?: return
            val mrResult = ttsEngine.setLanguage(Locale("mr", "IN"))
            if (mrResult == TextToSpeech.LANG_MISSING_DATA || mrResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                val hiResult = ttsEngine.setLanguage(Locale("hi", "IN"))
                if (hiResult == TextToSpeech.LANG_MISSING_DATA || hiResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsEngine.setLanguage(Locale("en", "IN"))
                    selectedLanguage = Locale("en", "IN")
                } else {
                    selectedLanguage = Locale("hi", "IN")
                }
            } else {
                selectedLanguage = Locale("mr", "IN")
            }
            isTtsReady = true

            ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    currentOnComplete?.invoke()
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    currentOnComplete?.invoke()
                }
            })
        }
    }

    /**
     * Builds a progressive multi-turn conversation between the Scammer AI clone and the Honeypot Bot.
     * Both will talk to each other in sequence through the speaker!
     */
    fun generateMultiTurnDialogue(
        contextSnippet: String,
        persona: ScamBusterPersona
    ): List<DialogueTurn> {
        val lower = contextSnippet.lowercase(Locale.ROOT)
        val isAaji = persona == ScamBusterPersona.AAJI

        return when {
            // Case 1: CBI / Digital Arrest / Police / Warrant
            lower.contains("arrest") || lower.contains("cbi") || lower.contains("police") ||
            lower.contains("warrant") || lower.contains("fir") || lower.contains("कस्टडी") ||
            lower.contains("अरेस्ट") || lower.contains("crime") -> {
                if (isAaji) {
                    listOf(
                        DialogueTurn(
                            turnIndex = 1,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Clone (CBI Impersonator)",
                            speechText = "This is Inspector Vikram Rathore from CBI Cyber Cell, New Delhi! You are under digital arrest! Transfer 2.5 Lakhs immediately!",
                            subtitleText = "[Scammer demanding ₹2.5L under digital arrest threat]",
                            pitch = 0.95f,
                            speechRate = 1.05f,
                            frustrationLevel = 40
                        ),
                        DialogueTurn(
                            turnIndex = 2,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👵 AI आजी (Honeypot Active)",
                            speechText = "अरे बापरे! CBI मधून बोलतोयस? अरे बाबा आमच्या स्वारगेटला तर पोस्ट ऑफिस आहे फक्त! तू आधी घरी चहा प्यायला ये, मी मस्त गरम कांदा भजी केलीयेत!",
                            subtitleText = "CBI? Son, we only have a post office in Swargate! Come over for tea and hot onion pakodas!",
                            pitch = 1.35f,
                            speechRate = 0.85f,
                            frustrationLevel = 60
                        ),
                        DialogueTurn(
                            turnIndex = 3,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Clone (CBI Impersonator)",
                            speechText = "Don't act smart, old lady! This is a criminal investigation! Open your PhonePe or net banking right now!",
                            subtitleText = "[Scammer furious, shouting to open net banking app]",
                            pitch = 0.92f,
                            speechRate = 1.15f,
                            frustrationLevel = 75
                        ),
                        DialogueTurn(
                            turnIndex = 4,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👵 AI आजी (Honeypot Active)",
                            speechText = "फोन-पे? अरे माझ्याकडे तो साधा डबडा फोन आहे! त्यात तर फक्त सापाचा गेम चालतो रे बाळा! तो नंबर जरा मोठ्याने सांग, मी जुन्या डायरीत लिहून घेते...",
                            subtitleText = "PhonePe? Son, I only have a basic keypad phone with snake game! Read the number loudly, I'll write in my diary...",
                            pitch = 1.35f,
                            speechRate = 0.82f,
                            frustrationLevel = 90
                        ),
                        DialogueTurn(
                            turnIndex = 5,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Clone (CBI Impersonator)",
                            speechText = "I am disconnecting this call! You are completely wasting my time! What kind of useless woman are you?!",
                            subtitleText = "[Scammer giving up in extreme rage and disconnecting]",
                            pitch = 0.90f,
                            speechRate = 1.20f,
                            frustrationLevel = 98
                        ),
                        DialogueTurn(
                            turnIndex = 6,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👵 AI आजी (Honeypot Active)",
                            speechText = "अरे फोन ठेवलास पण? उद्या दुपारी चहाला नक्की ये बरं का... काळजी घे!",
                            subtitleText = "Oh, you hung up? Do come tomorrow afternoon for tea... take care!",
                            pitch = 1.35f,
                            speechRate = 0.88f,
                            frustrationLevel = 99
                        )
                    )
                } else {
                    listOf(
                        DialogueTurn(
                            turnIndex = 1,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Clone (CBI Impersonator)",
                            speechText = "This is CBI headquarters! Your identity is implicated in a money laundering case. Verify your details now!",
                            subtitleText = "[Scammer threatening money laundering case]",
                            pitch = 0.95f,
                            speechRate = 1.05f,
                            frustrationLevel = 45
                        ),
                        DialogueTurn(
                            turnIndex = 2,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👴 AI काका (Honeypot Active)",
                            speechText = "काय रे भामट्या! सीबीआय मधून बोलतोयस? मी 35 वर्षे महसूल विभागात वरिष्ठ क्लास वन अधिकारी होतो! आधी तुझा बॅज नंबर आणि सेक्शन सांग!",
                            subtitleText = "You fraud! Claiming CBI? I was a Class-1 Revenue officer for 35 years! State your badge number and section right now!",
                            pitch = 0.82f,
                            speechRate = 0.92f,
                            frustrationLevel = 68
                        ),
                        DialogueTurn(
                            turnIndex = 3,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Clone (CBI Impersonator)",
                            speechText = "Sir, we have non-bailable arrest warrant from Delhi court! You must deposit ₹1 Lakh verification fee!",
                            subtitleText = "[Scammer aggressively demanding ₹1L security deposit]",
                            pitch = 0.92f,
                            speechRate = 1.10f,
                            frustrationLevel = 82
                        ),
                        DialogueTurn(
                            turnIndex = 4,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👴 AI काका (Honeypot Active)",
                            speechText = "अटक वॉरंट? अरे मूर्खा, कलम ४१ वाचला आहेस का कधी? मी स्वतः पोलीस अधीक्षक देशमुखांशी बोलतो आताच! तू स्वारगेट पोलीस चौकीत ये समोरासमोर!",
                            subtitleText = "Arrest warrant? Fool, have you read CrPC Section 41? I am calling the SP right now, meet me at the police station!",
                            pitch = 0.80f,
                            speechRate = 0.95f,
                            frustrationLevel = 95
                        ),
                        DialogueTurn(
                            turnIndex = 5,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Clone (CBI Impersonator)",
                            speechText = "Wrong number! Don't call this number again!",
                            subtitleText = "[Scammer panicked and hung up]",
                            pitch = 0.90f,
                            speechRate = 1.20f,
                            frustrationLevel = 99
                        )
                    )
                }
            }

            // Case 2: OTP / Banking / Password theft
            lower.contains("otp") || lower.contains("pin") || lower.contains("password") ||
            lower.contains("code") || lower.contains("ओटीपी") || lower.contains("digit") ||
            lower.contains("bank") || lower.contains("kyc") || lower.contains("card") ||
            lower.contains("खाते") -> {
                if (isAaji) {
                    listOf(
                        DialogueTurn(
                            turnIndex = 1,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Clone (Bank Phisher)",
                            speechText = "Your State Bank account has been frozen! We have sent a 6-digit verification code. Read the OTP immediately!",
                            subtitleText = "[Scammer demanding urgent 6-digit OTP]",
                            pitch = 0.96f,
                            speechRate = 1.08f,
                            frustrationLevel = 38
                        ),
                        DialogueTurn(
                            turnIndex = 2,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👵 AI आजी (Honeypot Active)",
                            speechText = "अरे बाळा, कोणता OTP विचारतोयस? त्या गॅस सिलिंडरवाल्याचा आलाय का रे? जरा थांब, माझा चष्मा शोधू दे आधी कपाटात...",
                            subtitleText = "Which OTP dear? Is it for the cooking gas cylinder? Wait, let me find my glasses in the cupboard...",
                            pitch = 1.35f,
                            speechRate = 0.85f,
                            frustrationLevel = 58
                        ),
                        DialogueTurn(
                            turnIndex = 3,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Clone (Bank Phisher)",
                            speechText = "Hurry up! The OTP expires in 30 seconds! Open the SMS and read the numbers!",
                            subtitleText = "[Scammer creating artificial panic and urgency]",
                            pitch = 0.94f,
                            speechRate = 1.18f,
                            frustrationLevel = 78
                        ),
                        DialogueTurn(
                            turnIndex = 4,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👵 AI आजी (Honeypot Active)",
                            speechText = "नंबर वाचू का? ९... २... अरे ५ आहे की ७? जरा थांब, शेजारच्या जोशीकाकूंना चष्मा लावून वाचायला सांगते... जोशीकाकू, ओ जोशीकाकू!",
                            subtitleText = "Should I read? 9... 2... wait, is that a 5 or 7? Let me call neighbour Mrs. Joshi to read it with her glasses!",
                            pitch = 1.35f,
                            speechRate = 0.82f,
                            frustrationLevel = 92
                        ),
                        DialogueTurn(
                            turnIndex = 5,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Clone (Bank Phisher)",
                            speechText = "I can't take this anymore! You are totally wasting my time! Call is ended!",
                            subtitleText = "[Scammer hanging up in frustration]",
                            pitch = 0.90f,
                            speechRate = 1.20f,
                            frustrationLevel = 99
                        )
                    )
                } else {
                    listOf(
                        DialogueTurn(
                            turnIndex = 1,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Clone (Bank Phisher)",
                            speechText = "Your debit card is blocked! Tell me your 16-digit card number and OTP right now!",
                            subtitleText = "[Scammer demanding debit card and OTP]",
                            pitch = 0.96f,
                            speechRate = 1.08f,
                            frustrationLevel = 42
                        ),
                        DialogueTurn(
                            turnIndex = 2,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👴 AI काका (Honeypot Active)",
                            speechText = "OTP? अरे माझ्या मोबाईलमध्ये बॅलन्सच नाहीये! तू आधी मला १०० रुपयांचा रिचार्ज कर, मग मी तुला OTP बघून सांगतो!",
                            subtitleText = "OTP? My phone has zero balance! First recharge my phone with ₹100, then I'll read your OTP!",
                            pitch = 0.82f,
                            speechRate = 0.92f,
                            frustrationLevel = 70
                        ),
                        DialogueTurn(
                            turnIndex = 3,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Clone (Bank Phisher)",
                            speechText = "Why will I recharge your phone?! I am bank manager! Give me the OTP immediately!",
                            subtitleText = "[Scammer losing patience]",
                            pitch = 0.92f,
                            speechRate = 1.15f,
                            frustrationLevel = 88
                        ),
                        DialogueTurn(
                            turnIndex = 4,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👴 AI काका (Honeypot Active)",
                            speechText = "मॅनेजर आहेस ना? मग पाटसंस्थेत येऊन ५००० रुपये माझ्या खात्यात जमा कर! फुकटचे नियम शिकवू नकोस मला!",
                            subtitleText = "You are a manager? Then deposit ₹5000 in my account first! Don't teach me fake rules!",
                            pitch = 0.80f,
                            speechRate = 0.95f,
                            frustrationLevel = 99
                        )
                    )
                }
            }

            // Case 3: Hospital / Emergency Family Clone Scam
            else -> {
                if (isAaji) {
                    listOf(
                        DialogueTurn(
                            turnIndex = 1,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Voice Clone (Cloned Distress)",
                            speechText = "Aaji, help me please! I had a bad accident in Pune, transfer 50,000 to this hospital UPI immediately!",
                            subtitleText = "[Cloned voice pretending to be grandchild in accident]",
                            pitch = 1.02f,
                            speechRate = 1.05f,
                            frustrationLevel = 35
                        ),
                        DialogueTurn(
                            turnIndex = 2,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👵 AI आजी (Honeypot Active)",
                            speechText = "अरे बाळा, अपघातात लागलं तुला? पण तू तर आताच आतल्या खोलीत बसून पुरणपोळी खातोयस रे! तू कसला डॉक्टर आहेस? आधी घरी ये, तुला तुळशीचा काढा करून देते!",
                            subtitleText = "Oh dear, an accident? But my grandchild is sitting in the next room eating puran poli right now! Who are you?",
                            pitch = 1.35f,
                            speechRate = 0.85f,
                            frustrationLevel = 70
                        ),
                        DialogueTurn(
                            turnIndex = 3,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Voice Clone (Cloned Distress)",
                            speechText = "No no, this is an emergency! Don't you care about your family? Pay right now!",
                            subtitleText = "[Scammer attempting coercive emotional extortion]",
                            pitch = 0.95f,
                            speechRate = 1.15f,
                            frustrationLevel = 88
                        ),
                        DialogueTurn(
                            turnIndex = 4,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👵 AI आजी (Honeypot Active)",
                            speechText = "अरे ओरडू नकोस रे बाळा, माझं बीपी वाढतंय! तू आधी पत्ता सांग, मी शेजारच्या पाटलांची रिक्षा घेऊन स्वतः डबा घेऊन येते दवाखान्यात!",
                            subtitleText = "Don't shout son, my BP is rising! Give me the hospital address, I will hire Patil's auto and bring a lunchbox myself!",
                            pitch = 1.35f,
                            speechRate = 0.82f,
                            frustrationLevel = 98
                        )
                    )
                } else {
                    listOf(
                        DialogueTurn(
                            turnIndex = 1,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Voice Clone (Cloned Distress)",
                            speechText = "Uncle, urgent emergency! Need 40,000 rupees right now or hospital will stop treatment!",
                            subtitleText = "[Cloned voice demanding urgent medical transfer]",
                            pitch = 1.0f,
                            speechRate = 1.05f,
                            frustrationLevel = 40
                        ),
                        DialogueTurn(
                            turnIndex = 2,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👴 AI काका (Honeypot Active)",
                            speechText = "पैसे पाठवू? अरे माझ्याकडे सगळी रोख कॅश आहे! तू दवाखान्याचा पत्ता सांग मी स्वतः गाडी काढून ५ मिनिटांत तिथे येतो, थांब तू!",
                            subtitleText = "Send money? I have full hard cash in hand! Give me the clinic address, I am taking my car and coming in 5 minutes!",
                            pitch = 0.82f,
                            speechRate = 0.92f,
                            frustrationLevel = 75
                        ),
                        DialogueTurn(
                            turnIndex = 3,
                            speaker = SpeakerRole.SCAMMER_AI,
                            speakerDisplayName = "🔴 Scammer AI Voice Clone (Cloned Distress)",
                            speechText = "No cash! Only UPI or online transfer! Hurry up!",
                            subtitleText = "[Scammer insisting strictly on online UPI]",
                            pitch = 0.94f,
                            speechRate = 1.15f,
                            frustrationLevel = 90
                        ),
                        DialogueTurn(
                            turnIndex = 4,
                            speaker = SpeakerRole.HONEYPOT_BOT,
                            speakerDisplayName = "👴 AI काका (Honeypot Active)",
                            speechText = "ऑनलाइन नाही जमणार! रोख पैसे घ्यायचे तर घे, नाहीतर पोलीस ठाण्यात तक्रार दाखल करायला निघालोय मी!",
                            subtitleText = "No online! If you want cash take it, otherwise I am heading to the police station to file an FIR!",
                            pitch = 0.80f,
                            speechRate = 0.95f,
                            frustrationLevel = 99
                        )
                    )
                }
            }
        }
    }

    /**
     * Helper for single reply generation if needed
     */
    fun generateReply(scammerSpeech: String, persona: ScamBusterPersona): ScamBusterReply {
        val dialogue = generateMultiTurnDialogue(scammerSpeech, persona)
        val botTurn = dialogue.firstOrNull { it.speaker == SpeakerRole.HONEYPOT_BOT }
            ?: dialogue.last()
        return ScamBusterReply(
            replyMarathi = botTurn.speechText,
            replyEnglish = botTurn.subtitleText,
            detectedCategory = if (scammerSpeech.contains("cbi", true) || scammerSpeech.contains("arrest", true)) "🚨 Digital Arrest Fraud" else "🔑 Credential Theft",
            frustrationIncrease = 28
        )
    }

    /**
     * Speaks an individual turn out loud with specific pitch, rate, and completion callback.
     */
    fun speakTurn(turn: DialogueTurn, onComplete: () -> Unit = {}) {
        val ttsEngine = tts ?: return
        if (!isTtsReady) {
            onComplete()
            return
        }

        currentOnComplete = onComplete

        ttsEngine.setPitch(turn.pitch)
        ttsEngine.setSpeechRate(turn.speechRate)

        // Choose language: If text contains Devanagari script, use Marathi/Hindi, otherwise use English
        val hasDevanagari = turn.speechText.any { it in '\u0900'..'\u097F' }
        if (hasDevanagari) {
            ttsEngine.setLanguage(Locale("mr", "IN"))
        } else {
            ttsEngine.setLanguage(Locale("en", "IN"))
        }

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "HoneypotTurn_${turn.turnIndex}_${System.currentTimeMillis()}")
        }

        ttsEngine.speak(turn.speechText, TextToSpeech.QUEUE_FLUSH, params, "HoneypotUtterance")
    }

    fun speakOutLoud(reply: ScamBusterReply, persona: ScamBusterPersona, onComplete: () -> Unit = {}) {
        val dummyTurn = DialogueTurn(
            turnIndex = 1,
            speaker = SpeakerRole.HONEYPOT_BOT,
            speakerDisplayName = persona.displayName,
            speechText = reply.replyMarathi,
            subtitleText = reply.replyEnglish,
            pitch = if (persona == ScamBusterPersona.AAJI) 1.35f else 0.82f,
            speechRate = if (persona == ScamBusterPersona.AAJI) 0.85f else 0.92f
        )
        speakTurn(dummyTurn, onComplete)
    }

    fun stopSpeaking() {
        currentOnComplete = null
        tts?.stop()
    }

    fun shutdown() {
        currentOnComplete = null
        tts?.stop()
        tts?.shutdown()
    }
}
