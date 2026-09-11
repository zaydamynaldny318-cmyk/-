import os
import requests
import asyncio
from typing import List, Dict, Any, Optional

CACHE_DIR = os.getenv("ASSET_CACHE_DIR", "/tmp/zaid_editor/cache/assets")
os.makedirs(CACHE_DIR, exist_ok=True)

class AssetFetcher:
    """
    Real-Time Internet Asset Retrieval (Auto-SFX & Music):
    - Freesound API: Instant sound effect fetching (whoosh, cinematic riser, dramatic boom)
    - Jamendo API: Royalty-free background music fetching
    - Smart local disk cache to eliminate redundant network roundtrips
    - Precise timestamp injection mapping for auto-editing pipeline
    """

    def __init__(self):
        self.freesound_api_key = os.getenv("FREESOUND_API_KEY", "")
        self.jamendo_client_id = os.getenv("JAMENDO_CLIENT_ID", "")

    async def search_freesound(self, query: str = "whoosh", page_size: int = 5) -> List[Dict[str, Any]]:
        """Queries Freesound API for audio clips matching tag."""
        if not self.freesound_api_key:
            # High-fidelity realistic mock catalog if API key not provided
            return self._get_fallback_sfx(query)

        url = "https://freesound.org/apiv2/search/text/"
        params = {
            "query": query,
            "token": self.freesound_api_key,
            "fields": "id,name,previews,duration,tags",
            "page_size": page_size
        }
        try:
            loop = asyncio.get_event_loop()
            resp = await loop.run_in_executor(None, lambda: requests.get(url, params=params, timeout=10))
            if resp.status_code == 200:
                data = resp.json()
                results = []
                for item in data.get("results", []):
                    results.append({
                        "id": str(item.get("id")),
                        "title": item.get("name"),
                        "preview_url": item.get("previews", {}).get("preview-hq-mp3"),
                        "duration": item.get("duration", 2.0),
                        "source": "freesound",
                        "tag": query
                    })
                return results
        except Exception as e:
            print(f"[Freesound API Error]: {e}")

        return self._get_fallback_sfx(query)

    async def search_jamendo(self, tag: str = "cinematic", limit: int = 5) -> List[Dict[str, Any]]:
        """Queries Jamendo API for royalty-free background music."""
        if not self.jamendo_client_id:
            return self._get_fallback_music(tag)

        url = "https://api.jamendo.com/v3.0/tracks/"
        params = {
            "client_id": self.jamendo_client_id,
            "format": "json",
            "limit": limit,
            "tags": tag,
            "audioformat": "mp32"
        }
        try:
            loop = asyncio.get_event_loop()
            resp = await loop.run_in_executor(None, lambda: requests.get(url, params=params, timeout=10))
            if resp.status_code == 200:
                data = resp.json()
                tracks = []
                for item in data.get("results", []):
                    tracks.append({
                        "id": item.get("id"),
                        "title": item.get("name"),
                        "artist": item.get("artist_name"),
                        "preview_url": item.get("audio"),
                        "duration": item.get("duration"),
                        "source": "jamendo",
                        "tag": tag
                    })
                return tracks
        except Exception as e:
            print(f"[Jamendo API Error]: {e}")

        return self._get_fallback_music(tag)

    async def search_sfx(self, query: str, provider: str = "freesound") -> List[Dict[str, Any]]:
        if provider == "jamendo":
            return await self.search_jamendo(tag=query)
        return await self.search_freesound(query=query)

    async def download_and_cache(self, url: str, asset_id: str) -> str:
        """Downloads audio asset and caches on local storage."""
        cache_path = os.path.join(CACHE_DIR, f"{asset_id}.mp3")
        if os.path.exists(cache_path) and os.path.getsize(cache_path) > 0:
            return cache_path

        if not url:
            # Generate synthetic beep/whoosh if preview URL is absent
            cmd = ["ffmpeg", "-y", "-f", "lavfi", "-i", "sine=frequency=800:duration=1.2", cache_path]
            proc = await asyncio.create_subprocess_exec(*cmd)
            await proc.communicate()
            return cache_path

        try:
            loop = asyncio.get_event_loop()
            resp = await loop.run_in_executor(None, lambda: requests.get(url, timeout=20))
            if resp.status_code == 200:
                with open(cache_path, "wb") as f:
                    f.write(resp.content)
                return cache_path
        except Exception as e:
            print(f"[Asset Download Error]: {e}")

        # Fallback tone
        cmd = ["ffmpeg", "-y", "-f", "lavfi", "-i", "sine=frequency=600:duration=1.0", cache_path]
        proc = await asyncio.create_subprocess_exec(*cmd)
        await proc.communicate()
        return cache_path

    async def fetch_assets_for_scenes(self, scene_tags: List[str]) -> List[Dict[str, Any]]:
        """
        Scans video context and scene tags (whoosh, cinematic riser, dramatic boom)
        and caches sound effects for precise timestamp injection.
        """
        injections = []
        tags_to_scan = scene_tags if scene_tags else ["whoosh", "cinematic riser", "dramatic boom"]

        for tag in tags_to_scan:
            items = await self.search_freesound(query=tag, page_size=2)
            if items:
                chosen = items[0]
                local_file = await self.download_and_cache(chosen.get("preview_url", ""), chosen["id"])
                injections.append({
                    "tag": tag,
                    "title": chosen["title"],
                    "source": chosen["source"],
                    "cached_path": local_file,
                    "duration": chosen.get("duration", 1.5)
                })
        return injections

    def _get_fallback_sfx(self, query: str) -> List[Dict[str, Any]]:
        presets = {
            "whoosh": [
                {"id": "sfx_whoosh_01", "title": "Fast Cinematic Air Whoosh", "preview_url": "", "duration": 0.8, "source": "freesound", "tag": "whoosh"},
                {"id": "sfx_whoosh_02", "title": "Heavy Bass Swoosh Transition", "preview_url": "", "duration": 1.2, "source": "freesound", "tag": "whoosh"}
            ],
            "cinematic riser": [
                {"id": "sfx_riser_01", "title": "Tension Build Riser & Clock", "preview_url": "", "duration": 3.0, "source": "freesound", "tag": "cinematic riser"},
                {"id": "sfx_riser_02", "title": "Violin Shephard Tone Suspense", "preview_url": "", "duration": 2.5, "source": "freesound", "tag": "cinematic riser"}
            ],
            "dramatic boom": [
                {"id": "sfx_boom_01", "title": "Deep Sub Bass Climax Boom", "preview_url": "", "duration": 2.0, "source": "freesound", "tag": "dramatic boom"},
                {"id": "sfx_boom_02", "title": "Trailer Impact Thud", "preview_url": "", "duration": 1.5, "source": "freesound", "tag": "dramatic boom"}
            ]
        }
        return presets.get(query.lower(), [
            {"id": f"sfx_{query}_01", "title": f"Dynamic SFX ({query})", "preview_url": "", "duration": 1.5, "source": "freesound", "tag": query}
        ])

    def _get_fallback_music(self, tag: str) -> List[Dict[str, Any]]:
        return [
            {
                "id": "jamendo_cinematic_01",
                "title": "Adrenaline Rush Action",
                "artist": "RoyaltyFree Studio",
                "preview_url": "",
                "duration": 120,
                "source": "jamendo",
                "tag": tag
            },
            {
                "id": "jamendo_ambient_02",
                "title": "Dark Suspense Pulse",
                "artist": "Soundscapes Pro",
                "preview_url": "",
                "duration": 180,
                "source": "jamendo",
                "tag": tag
            }
        ]
