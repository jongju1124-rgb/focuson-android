#!/usr/bin/env python3
"""
라이선스 키 발급 CLI.

사용법:
    python scripts/generate_license.py <email> <tier>

예:
    python scripts/generate_license.py jangjongju@gmail.com tier3
    → HMAC-SHA256 16자 키 출력

tier 값: tier1 / tier2 / tier3

⚠ 실행 전: 환경변수 LICENSE_SECRET 또는 keystore.properties 의 licenseSecret 을 확인.
    keystore.properties 와 반드시 같은 값을 사용해야 앱에서 검증 통과됨.
"""

import hmac
import hashlib
import os
import re
import sys
from pathlib import Path


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


def generate(email: str, tier_id: str, secret: str) -> str:
    payload = f"focuson|{email.strip().lower()}|{tier_id}".encode("utf-8")
    digest = hmac.new(secret.encode("utf-8"), payload, hashlib.sha256).hexdigest()
    return digest.upper()[:16]


def main():
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)
    email = sys.argv[1].strip().lower()
    tier = sys.argv[2].strip().lower()
    if tier not in ("tier1", "tier2", "tier3"):
        print("tier 는 tier1 / tier2 / tier3 중 하나여야 합니다.", file=sys.stderr)
        sys.exit(1)
    secret = load_secret()
    key = generate(email, tier, secret)

    print()
    print("=" * 50)
    print(f"이메일: {email}")
    print(f"티어: {tier}")
    print(f"라이선스 키: {key}")
    print("=" * 50)
    print()
    print("이메일 본문 템플릿:")
    print()
    print(f"포커스온 Pro 라이선스 키입니다.")
    print(f"")
    print(f"· 이메일: {email}")
    print(f"· 티어: {tier}")
    print(f"· 키: {key}")
    print(f"")
    print(f"앱 → 홈 → 💛 Pro → [이미 라이선스 키가 있어요]")
    print(f"에서 위 정보 그대로 입력하시면 됩니다.")


if __name__ == "__main__":
    main()
