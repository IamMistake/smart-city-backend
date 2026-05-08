import os

import requests
from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()


class ChatRequest(BaseModel):
    message: str


@router.post("/chat")
def chat(req: ChatRequest):
    try:
        # Try HuggingFace FIRST
        api_key = os.getenv("HUGGINGFACE_API_KEY")

        if api_key:
            try:
                response = requests.post(
                    "https://api-inference.huggingface.co/models/google/flan-t5-base",
                    headers={
                        "Authorization": f"Bearer {api_key}",
                    },
                    json={"inputs": req.message},
                    timeout=5,
                )

                if response.status_code == 200:
                    data = response.json()
                    return {"reply": data[0]["generated_text"]}

            except Exception:
                pass

        # Fallback response
        return {
            "reply": (
                "🤖 Smart City Assistant:\n\n"
                "We are experiencing a high volume of messages right now. "
                "It may take a moment for our assistant to respond."
            )
        }

    except Exception as e:
        print("ERROR:", e)
        return {"reply": "⚠️ Chat service temporarily unavailable"}