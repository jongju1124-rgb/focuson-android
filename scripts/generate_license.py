#!/usr/bin/env python3
"""
라이선스 키 발급 CLI.

사용법:
    python scripts/generate_license.py <email>

예:
    python scripts/generate_license.py jangjongju@gmail.com
    → HMAC-SHA256 16자 Pro 키 출력

⚠ 실행 전: 환경변수 LICENSE_SECRET 또는 keystore.properties 의 licenseSecret 을 확인.
    keystore.properties 와 반드시 같은 값을 사용해야 앱에서 검증 통과됨.
"""

import hmac
import hashlib
import os
import re
import sys
from pathlib import Path

TIER_ID = "pro"


def load_secret() -> str:
    env = os.environ.get("LICENSE_SECRET")
    if env:
        return env
    # keystore.properties 에서 읽기
    ks = Path(__file__).resolve().parent.parent / "keystore.properties"
    if ks.exists():
        for line in ks.read_text(encoding="utf-8").splitlines():
            m = re.match(r"^\s*licenseSecret\s*=\s*(.+?)\s*$", line)
            if m:
                return m.group(1)
    print("⚠  LICENSE_SECRET 환경변수 또는 keystore.properties 의 licenseSecret 이 필요합니다.", file=sys.stderr)
    sys.exit(1)


def generate(email: str, secret: str) -> str:
    payload = f"focuson|{email.strip().lower()}|{TIER_ID}".encode("utf-8")
    digest = hmac.new(secret.encode("utf-8"), payload, hashlib.sha256).hexdigest()
    return digest.upper()[:16]


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    email = sys.argv[1].strip().lower()
    secret = load_secret()
    key = generate(email, secret)

    print()
    print("=" * 50)
    print(f"이메일: {email}")
    print(f"티어: {TIER_ID} (Pro 2,900원)")
    print(f"라이선스 키: {key}")
    print("=" * 50)
    print()
    print("이메일 본문 템플릿:")
    print()
    print(f"안녕하세요, 포커스온 Pro 라이선스 키입니다.")
    print(f"")
    print(f"· 이메일: {email}")
    print(f"· 키: {key}")
    print(f"")
    print(f"앱 → 홈 → 💛 Pro → [이미 라이선스 키가 있어요]")
    print(f"이메일·키를 위와 똑같이 입력하시면 됩니다.")
    print(f"")
    print(f"감사합니다!")


if __name__ == "__main__":
    main()
