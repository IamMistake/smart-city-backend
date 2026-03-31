from __future__ import annotations

from functools import lru_cache
from typing import Any

import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jwt import PyJWKClient
from jwt.exceptions import PyJWTError
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.core.config import settings
from app.db.session import get_db

bearer_scheme = HTTPBearer(auto_error=False)


@lru_cache(maxsize=1)
def get_jwk_client() -> PyJWKClient:
	jwks_url = settings.resolved_clerk_jwks_url
	if not jwks_url:
		raise RuntimeError("Missing Clerk JWKS configuration")

	return PyJWKClient(jwks_url)


def decode_clerk_token(token: str) -> dict[str, Any]:
	if not settings.clerk_issuer_url:
		raise HTTPException(
			status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
			detail="CLERK_ISSUER_URL is not configured",
		)

	try:
		signing_key = get_jwk_client().get_signing_key_from_jwt(token)
		decode_kwargs: dict[str, Any] = {
			"algorithms": ["RS256"],
			"issuer": settings.clerk_issuer_url,
		}

		if settings.clerk_audience:
			decode_kwargs["audience"] = settings.clerk_audience
		else:
			decode_kwargs["options"] = {"verify_aud": False}

		claims = jwt.decode(token, signing_key.key, **decode_kwargs)
		if not isinstance(claims, dict):
			raise HTTPException(
				status_code=status.HTTP_401_UNAUTHORIZED,
				detail="Invalid token payload",
			)

		return claims
	except RuntimeError as error:
		raise HTTPException(
			status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
			detail=str(error),
		) from error
	except PyJWTError as error:
		raise HTTPException(
			status_code=status.HTTP_401_UNAUTHORIZED,
			detail="Invalid authentication token",
		) from error


def get_clerk_claims(
	credentials: HTTPAuthorizationCredentials | None = Depends(bearer_scheme),
) -> dict[str, Any]:
	if not credentials or credentials.scheme.lower() != "bearer":
		raise HTTPException(
			status_code=status.HTTP_401_UNAUTHORIZED,
			detail="Missing bearer token",
		)

	claims = decode_clerk_token(credentials.credentials)
	if not claims.get("sub"):
		raise HTTPException(
			status_code=status.HTTP_401_UNAUTHORIZED,
			detail="Token is missing subject",
		)

	return claims


def get_active_user(
	claims: dict[str, Any] = Depends(get_clerk_claims),
	db: Session = Depends(get_db),
) -> dict[str, Any]:
	clerk_user_id = str(claims["sub"])
	result = db.execute(
		text(
			"""
			SELECT
				id,
				clerk_user_id,
				email,
				full_name,
				role,
				avatar_url,
				is_active
			FROM core.user_profiles
			WHERE clerk_user_id = :clerk_user_id
				AND deleted_at IS NULL
			LIMIT 1
			""",
		),
		{"clerk_user_id": clerk_user_id},
	).mappings().first()

	if result is None:
		raise HTTPException(
			status_code=status.HTTP_403_FORBIDDEN,
			detail="User profile not found",
		)

	if not result["is_active"]:
		raise HTTPException(
			status_code=status.HTTP_403_FORBIDDEN,
			detail="User is not active",
		)

	return {
		"id": str(result["id"]),
		"clerk_user_id": result["clerk_user_id"],
		"email": result["email"],
		"full_name": result["full_name"],
		"role": result["role"],
		"avatar_url": result["avatar_url"],
		"is_active": bool(result["is_active"]),
	}
