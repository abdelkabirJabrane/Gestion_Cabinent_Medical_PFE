"""
Script de diagnostic rapide pour tester Groq et Gemini directement.
Lancez: python test_llm.py
"""
import asyncio
import os
from dotenv import load_dotenv

load_dotenv()

GROQ_API_KEY   = os.getenv("GROQ_API_KEY", "")
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
GROQ_MODEL     = os.getenv("GROQ_LLM_MODEL", "llama-3.3-70b-versatile")
GEMINI_MODEL   = os.getenv("GEMINI_MODEL", "gemini-2.0-flash")

print(f"  GROQ_API_KEY  : {'✓ Set (' + GROQ_API_KEY[:12] + '...)' if GROQ_API_KEY else '✗ EMPTY'}")
print(f"  GEMINI_API_KEY: {'✓ Set (' + GEMINI_API_KEY[:12] + '...)' if GEMINI_API_KEY else '✗ EMPTY'}")
print()


# ── Test 1: Groq direct HTTP ─────────────────────────────────────────────────
async def test_groq():
    print("=== TEST 1: Groq API ===")
    try:
        import httpx
        headers = {
            "Authorization": f"Bearer {GROQ_API_KEY}",
            "Content-Type": "application/json",
        }
        payload = {
            "model": GROQ_MODEL,
            "messages": [{"role": "user", "content": "Say hello in one word."}],
            "max_tokens": 10,
        }
        async with httpx.AsyncClient(timeout=30.0, verify=True) as client:
            resp = await client.post(
                "https://api.groq.com/openai/v1/chat/completions",
                headers=headers,
                json=payload,
            )
        print(f"  HTTP Status : {resp.status_code}")
        if resp.status_code == 200:
            text = resp.json()["choices"][0]["message"]["content"]
            print(f"  Response    : {text}")
            print("  ✅ Groq OK\n")
            return True
        else:
            print(f"  ❌ Error body: {resp.text[:300]}\n")
            return False
    except Exception as e:
        print(f"  ❌ Exception : {e}\n")

        # Retry with SSL disabled (for proxy/corporate networks)
        print("  Retrying with SSL verification disabled...")
        try:
            async with httpx.AsyncClient(timeout=30.0, verify=False) as client:
                resp = await client.post(
                    "https://api.groq.com/openai/v1/chat/completions",
                    headers=headers,
                    json=payload,
                )
            print(f"  HTTP Status (no SSL) : {resp.status_code}")
            if resp.status_code == 200:
                text = resp.json()["choices"][0]["message"]["content"]
                print(f"  Response    : {text}")
                print("  ✅ Groq OK (SSL verification was the issue!)\n")
                return "no_ssl"
            else:
                print(f"  ❌ Error: {resp.text[:300]}\n")
        except Exception as e2:
            print(f"  ❌ Also failed without SSL: {e2}\n")
        return False


# ── Test 2: Gemini ───────────────────────────────────────────────────────────
async def test_gemini():
    print("=== TEST 2: Gemini API ===")
    try:
        from google import genai
        from google.genai import types
        client = genai.Client(api_key=GEMINI_API_KEY)
        response = await client.aio.models.generate_content(
            model=GEMINI_MODEL,
            contents="Say hello in one word.",
            config=types.GenerateContentConfig(max_output_tokens=10),
        )
        print(f"  Response : {response.text}")
        print("  ✅ Gemini OK\n")
        return True
    except Exception as e:
        err = str(e)
        if "429" in err or "RESOURCE_EXHAUSTED" in err:
            print("  ⚠️  Gemini: Quota journalier EPUISE — réessayez demain\n")
        else:
            print(f"  ❌ Gemini error: {e}\n")
        return False


async def main():
    print("=" * 50)
    print("  MedAgent LLM Diagnostic Tool")
    print("=" * 50)
    print()

    groq_ok   = await test_groq()
    gemini_ok = await test_gemini()

    print("=" * 50)
    print("  RÉSUMÉ")
    print("=" * 50)
    print(f"  Groq   : {'✅ OK' if groq_ok == True else ('⚠️  OK (désactivez SSL)' if groq_ok == 'no_ssl' else '❌ ECHEC')}")
    print(f"  Gemini : {'✅ OK' if gemini_ok else '❌ ECHEC (quota ou clé invalide)'}")
    print()

    if groq_ok == "no_ssl":
        print("  👉 SOLUTION: Ajoutez 'GROQ_SSL_VERIFY=false' dans .env_")
        print("     et mettez à jour llm.py pour passer verify=False au client Groq")
    elif not groq_ok:
        print("  👉 Vérifiez votre clé Groq sur https://console.groq.com")

asyncio.run(main())
