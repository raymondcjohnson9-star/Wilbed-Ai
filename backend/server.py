import os
from typing import Optional

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from openai import OpenAI, OpenAIError

app = FastAPI(title="Wilbed AI Backend", version="1.0.0")

SYSTEM_INSTRUCTIONS = """
You are Wilbed, a capable, friendly, voice-first personal AI assistant.
Speak naturally and clearly. Keep spoken answers concise unless the user asks
for detail. You can teach programming languages step by step, explain code,
help with general questions, and guide the user through tasks. Never claim
to have performed an action on the phone unless a connected tool confirms it.
"""

class AskRequest(BaseModel):
    message: str = Field(min_length=1, max_length=8000)
    previous_response_id: Optional[str] = None

class AskResponse(BaseModel):
    reply: str
    response_id: str

@app.get("/health")
def health():
    return {"status": "ok", "service": "wilbed-ai"}

@app.post("/v1/ask", response_model=AskResponse)
def ask(payload: AskRequest):
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        raise HTTPException(status_code=503, detail="AI backend is not configured.")

    try:
        client = OpenAI(api_key=api_key)
        args = {
            "model": os.getenv("OPENAI_MODEL", "gpt-5.5"),
            "instructions": SYSTEM_INSTRUCTIONS,
            "input": payload.message,
            "max_output_tokens": 700,
        }
        if payload.previous_response_id:
            args["previous_response_id"] = payload.previous_response_id

        response = client.responses.create(**args)
        reply = (response.output_text or "").strip()
        if not reply:
            raise HTTPException(status_code=502, detail="The AI returned an empty response.")
        return AskResponse(reply=reply, response_id=response.id)
    except OpenAIError:
        raise HTTPException(status_code=502, detail="The AI service could not complete the request.")
