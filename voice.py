"""Android speech input and spoken output for Wilbed.

Speech recognition is started only after the user taps the Voice control.
This avoids secretly recording audio or keeping the microphone active.
"""
from kivy.utils import platform


class WilbedVoice:
    REQUEST_CODE = 7412

    def __init__(self, on_text=None, on_status=None):
        self.on_text = on_text or (lambda text: None)
        self.on_status = on_status or (lambda text: None)
        self._tts = None
        self._tts_listener = None
        self._activity = None

        if platform == "android":
            self._init_android()

    def _init_android(self):
        try:
            from jnius import autoclass, PythonJavaClass, java_method
            from android import activity

            PythonActivity = autoclass("org.kivy.android.PythonActivity")
            TextToSpeech = autoclass("android.speech.tts.TextToSpeech")
            self._activity = PythonActivity.mActivity
            owner = self

            class InitListener(PythonJavaClass):
                __javainterfaces__ = ["android/speech/tts/TextToSpeech$OnInitListener"]
                __javacontext__ = "app"

                @java_method("(I)V")
                def onInit(self, status):
                    if status == 0:
                        owner.on_status("VOICE READY")

            self._tts_listener = InitListener()
            self._tts = TextToSpeech(self._activity, self._tts_listener)
            self._activity_result = activity
            activity.bind(on_activity_result=self._on_activity_result)
        except Exception as exc:
            self.on_status("VOICE MODULE UNAVAILABLE: " + str(exc))

    def listen(self):
        if platform != "android":
            self.on_status("Voice input is available in the Android app.")
            return

        try:
            from jnius import autoclass
            from android.permissions import Permission, request_permissions

            Intent = autoclass("android.content.Intent")
            RecognizerIntent = autoclass("android.speech.RecognizerIntent")

            def launch(results):
                if not results or not results[0]:
                    self.on_status("MICROPHONE PERMISSION NOT GRANTED")
                    return
                intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Wilbed")
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                self._activity.startActivityForResult(intent, self.REQUEST_CODE)
                self.on_status("LISTENING")

            request_permissions([Permission.RECORD_AUDIO], launch)
        except Exception as exc:
            self.on_status("COULD NOT START VOICE INPUT: " + str(exc))

    def _on_activity_result(self, request_code, result_code, intent):
        if request_code != self.REQUEST_CODE or intent is None:
            return
        try:
            from jnius import autoclass
            RecognizerIntent = autoclass("android.speech.RecognizerIntent")
            results = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if results and results.size() > 0:
                text = str(results.get(0)).strip()
                if text:
                    self.on_status("PROCESSING VOICE")
                    self.on_text(text)
                else:
                    self.on_status("I DID NOT CATCH THAT. TRY AGAIN.")
        except Exception:
            self.on_status("VOICE RECOGNITION FAILED")

    def speak(self, text):
        if platform != "android" or self._tts is None or not text:
            return
        try:
            from jnius import autoclass
            TextToSpeech = autoclass("android.speech.tts.TextToSpeech")
            self._tts.speak(str(text), TextToSpeech.QUEUE_FLUSH, None, "wilbed-reply")
        except Exception:
            self.on_status("SPEECH OUTPUT UNAVAILABLE")

    def shutdown(self):
        try:
            if self._tts is not None:
                self._tts.stop()
                self._tts.shutdown()
            if hasattr(self, "_activity_result"):
                self._activity_result.unbind(on_activity_result=self._on_activity_result)
        except Exception:
            pass
