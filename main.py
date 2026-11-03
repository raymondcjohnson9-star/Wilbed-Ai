
from kivy.app import App
from kivy.clock import Clock
from kivy.core.window import Window
from kivy.metrics import dp, sp
from kivy.animation import Animation
from kivy.graphics import Color, Ellipse
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.floatlayout import FloatLayout
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.button import Button
from kivy.uix.scrollview import ScrollView
from kivy.uix.widget import Widget

from assistant import get_response
from voice import WilbedVoice
from kivy.network.urlrequest import UrlRequest
import json

try:
    from config import API_BASE_URL
except ImportError:
    API_BASE_URL = ""


# WILBED AI COLOURS
BG = (0.015, 0.025, 0.07, 1)
PANEL = (0.035, 0.055, 0.12, 1)
PURPLE = (0.55, 0.25, 1, 1)
CYAN = (0.0, 0.85, 1, 1)
WHITE = (0.92, 0.95, 1, 1)
GREY = (0.48, 0.55, 0.68, 1)


Window.clearcolor = BG


class AIOrb(Widget):

    def __init__(self, **kwargs):
        super().__init__(**kwargs)

        with self.canvas:
            self.glow_color = Color(*PURPLE)
            self.glow = Ellipse()

            self.core_color = Color(*CYAN)
            self.core = Ellipse()

        self.bind(pos=self.draw_orb, size=self.draw_orb)

        self.animation = Animation(a=0.35, duration=1.2) + \
                         Animation(a=1, duration=1.2)

        self.animation.repeat = True
        self.animation.start(self.glow_color)

    def draw_orb(self, *args):
        diameter = min(self.width, self.height) * 0.65

        x = self.center_x - diameter / 2
        y = self.center_y - diameter / 2

        self.glow.pos = (x - dp(12), y - dp(12))
        self.glow.size = (diameter + dp(24), diameter + dp(24))

        self.core.pos = (x, y)
        self.core.size = (diameter, diameter)


class WilbedAI(App):

    def build(self):

        self.title = "WILBED AI"

        self.root_layout = BoxLayout(
            orientation="vertical",
            padding=dp(16),
            spacing=dp(10)
        )

        self.root_layout.canvas.before.clear()

        with self.root_layout.canvas.before:
            Color(*BG)
            self.background = Ellipse(
                pos=self.root_layout.pos,
                size=self.root_layout.size
            )

        self.root_layout.bind(
            pos=self.update_background,
            size=self.update_background
        )

        self.create_header()
        self.create_ai_core()
        self.create_status()
        self.voice = WilbedVoice(
            on_text=self.on_voice_text,
            on_status=self.on_voice_status
        )
        self.previous_response_id = None
        self.create_chat()
        self.create_input_area()
        self.create_footer()

        return self.root_layout

    def update_background(self, instance, value):
        self.background.pos = instance.pos
        self.background.size = instance.size

    def create_header(self):

        header = BoxLayout(
            size_hint_y=None,
            height=dp(55)
        )

        title = Label(
            text="[b][color=00D9FF]W[/color]ILBED AI[/b]",
            markup=True,
            font_size=sp(24),
            halign="left"
        )

        menu = Button(
            text="☰",
            size_hint_x=None,
            width=dp(50),
            background_normal="",
            background_color=PANEL,
            color=CYAN,
            font_size=sp(23)
        )

        header.add_widget(title)
        header.add_widget(menu)

        self.root_layout.add_widget(header)

    def create_ai_core(self):

        core_area = FloatLayout(
            size_hint_y=None,
            height=dp(190)
        )

        orb = AIOrb(
            size_hint=(None, None),
            size=(dp(180), dp(180)),
            pos_hint={"center_x": 0.5, "center_y": 0.5}
        )

        core_area.add_widget(orb)

        self.root_layout.add_widget(core_area)

    def create_status(self):

        self.status = Label(
            text="[color=00D9FF]●[/color] WILBED SYSTEM ONLINE",
            markup=True,
            size_hint_y=None,
            height=dp(30),
            font_size=sp(13)
        )

        self.root_layout.add_widget(self.status)

        subtitle = Label(
            text="YOUR PERSONAL AI ASSISTANT",
            color=GREY,
            size_hint_y=None,
            height=dp(25),
            font_size=sp(11)
        )

        self.root_layout.add_widget(subtitle)

    def create_chat(self):

        self.chat_scroll = ScrollView(
            size_hint=(1, 1),
            bar_width=dp(3)
        )

        self.chat_box = BoxLayout(
            orientation="vertical",
            size_hint_y=None,
            spacing=dp(12),
            padding=dp(10)
        )

        self.chat_box.bind(
            minimum_height=self.chat_box.setter("height")
        )

        self.add_message(
            "WILBED",
            "System initialized. Hello! I am Wilbed, your personal AI assistant."
        )

        self.chat_scroll.add_widget(self.chat_box)
        self.root_layout.add_widget(self.chat_scroll)

    def add_message(self, sender, message):

        color = "00D9FF" if sender == "WILBED" else "B78AFF"

        bubble = BoxLayout(
            orientation="vertical",
            size_hint_y=None,
            padding=dp(12),
            spacing=dp(5)
        )

        name = Label(
            text=f"[b][color={color}]{sender}[/color][/b]",
            markup=True,
            size_hint_y=None,
            height=dp(22),
            halign="left"
        )
        name.bind(size=name.setter("text_size"))

        content = Label(
            text=message,
            color=WHITE,
            font_size=sp(14),
            size_hint_y=None,
            halign="left",
            valign="middle"
        )

        content.bind(
            width=lambda instance, width:
            setattr(instance, "text_size", (width, None))
        )

        content.bind(
            texture_size=lambda instance, size:
            setattr(instance, "height", size[1] + dp(10))
        )

        bubble.add_widget(name)
        bubble.add_widget(content)

        bubble.bind(
            minimum_height=bubble.setter("height")
        )

        self.chat_box.add_widget(bubble)

        Clock.schedule_once(
            lambda dt: setattr(
                self.chat_scroll,
                "scroll_y",
                0
            ),
            0.1
        )

    def create_input_area(self):

        area = BoxLayout(
            size_hint_y=None,
            height=dp(55),
            spacing=dp(7)
        )

        self.message_input = TextInput(
            hint_text="Message Wilbed...",
            multiline=False,
            font_size=sp(14),
            foreground_color=WHITE,
            hint_text_color=GREY,
            background_color=PANEL,
            cursor_color=CYAN,
            padding=[dp(12), dp(15)],
            size_hint_x=1
        )

        self.message_input.bind(
            on_text_validate=self.send_message
        )

        send = Button(
            text="➤",
            size_hint_x=None,
            width=dp(55),
            background_normal="",
            background_color=PURPLE,
            color=WHITE,
            font_size=sp(23)
        )

        send.bind(on_press=self.send_message)

        area.add_widget(self.message_input)
        area.add_widget(send)

        self.root_layout.add_widget(area)

    def create_footer(self):

        footer = BoxLayout(
            size_hint_y=None,
            height=dp(55),
            spacing=dp(8)
        )

        buttons = [
            ("⌂", "HOME"),
            ("◉", "VOICE"),
            ("◈", "MEMORY"),
            ("⚙", "SETTINGS"),
            ("⌘", "CODE")
        ]

        for symbol, name in buttons:

            button = Button(
                text=f"{symbol}\n{name}",
                background_normal="",
                background_color=PANEL,
                color=CYAN,
                font_size=sp(10)
            )

            button.bind(
                on_press=lambda instance, n=name:
                self.footer_action(n)
            )

            footer.add_widget(button)

        self.root_layout.add_widget(footer)

    def footer_action(self, name):

        if name == "CODE":
            self.add_message("WILBED", get_response("teach me programming"))
            return

        if name == "VOICE":
            self.voice.listen()
            return
        else:
            self.status.text = (
                f"[color=00D9FF]● WILBED {name}[/color]"
            )

    def send_message(self, instance=None):

        message = self.message_input.text.strip()

        if not message:
            return

        self.add_message("YOU", message)

        self.message_input.text = ""

        # Use the secure OpenAI backend when configured; otherwise keep
        # the offline assistant available.
        if API_BASE_URL.strip():
            self.status.text = "[color=FFB347]● WILBED IS THINKING[/color]"
            UrlRequest(
                API_BASE_URL.rstrip("/") + "/v1/ask",
                req_body=json.dumps({
                    "message": message,
                    "previous_response_id": self.previous_response_id
                }),
                req_headers={"Content-Type": "application/json"},
                on_success=self.on_ai_success,
                on_failure=self.on_ai_failure,
                on_error=self.on_ai_error,
                method="POST",
                timeout=45
            )
        else:
            reply = get_response(message)
            self.add_message("WILBED", reply)
            self.voice.speak(reply)
            self.status.text = "[color=00D9FF]● OFFLINE MODE[/color]"

    def on_voice_text(self, text):
        self.message_input.text = text
        self.send_message()

    def on_voice_status(self, status):
        self.status.text = f"[color=00D9FF]● {status}[/color]"

    def on_ai_success(self, request, result):
        reply = result.get("reply", "I couldn't form a response.")
        self.previous_response_id = result.get("response_id")
        self.add_message("WILBED", reply)
        self.voice.speak(reply)
        self.status.text = "[color=00D9FF]● WILBED SYSTEM ONLINE[/color]"

    def on_ai_failure(self, request, result):
        self.status.text = "[color=FFB347]● AI SERVER REJECTED THE REQUEST[/color]"
        self.add_message("WILBED", "I couldn't reach the AI service. Please try again.")

    def on_ai_error(self, request, error):
        self.status.text = "[color=FFB347]● CONNECTION ERROR[/color]"
        self.add_message("WILBED", "I couldn't connect to the AI server. Check your connection.")


if __name__ == "__main__":
    WilbedAI().run()
