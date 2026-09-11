import os
import asyncio
from celery import Celery
from editing_engine import VideoEditingEngine
from audio_processor import AudioProcessor
from asset_fetcher import AssetFetcher

REDIS_URL = os.getenv("REDIS_URL", "redis://localhost:6379/0")

celery_app = Celery(
    "zaid_video_editor",
    broker=REDIS_URL,
    backend=REDIS_URL
)

celery_app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="UTC",
    enable_utc=True,
    task_track_started=True
)

@celery_app.task(bind=True, name="tasks.process_video_pipeline")
def process_video_pipeline_task(self, project_id: str, file_path: str, options: dict):
    """
    Asynchronous Celery pipeline worker task:
    1. Gemini Vision 0.5s frame analysis
    2. Faster-Whisper large-v3 speech transcription
    3. DeepFilterNet vocal isolation & -16 LUFS EBU R128 normalization
    4. Real-time Freesound/Jamendo SFX retrieval & cache
    5. FFmpeg NVIDIA NVENC hardware render
    """
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)

    engine = VideoEditingEngine()
    audio_proc = AudioProcessor()
    asset_fetcher = AssetFetcher()

    try:
        # Step 1: Visual AI Scene Intelligence (15%)
        self.update_state(state="PROGRESS", meta={"progress": 15, "stage": "تحليل الإطارات كل 0.5 ثانية عبر Gemini Vision"})
        visual_data = loop.run_until_complete(engine.analyze_video_frames_with_gemini(file_path))

        # Step 2: Faster-Whisper Transcription (35%)
        self.update_state(state="PROGRESS", meta={"progress": 35, "stage": "تفريغ الكلمات بدقة أجزاء الثانية بـ Faster-Whisper large-v3"})
        speech_data = loop.run_until_complete(audio_proc.transcribe_speech_whisper(file_path))

        # Step 3: DeepFilterNet Cleaning & EBU R128 (60%)
        self.update_state(state="PROGRESS", meta={"progress": 60, "stage": "عزل الضوضاء بـ DeepFilterNet وضبط الصوت إلى -16 LUFS"})
        cleaned_audio = loop.run_until_complete(audio_proc.clean_and_normalize_audio(file_path, options))

        # Step 4: Asset Retrieval (75%)
        self.update_state(state="PROGRESS", meta={"progress": 75, "stage": "جلب المؤثرات الصوتية الدقيقة من Freesound و Jamendo"})
        sfx_tracks = loop.run_until_complete(asset_fetcher.fetch_assets_for_scenes(visual_data.get("scene_tags", [])))

        # Step 5: FFmpeg NVENC Hardware Accelerated Render (95%)
        self.update_state(state="PROGRESS", meta={"progress": 95, "stage": "رندرة الفيديو النهائي بتسريع NVIDIA NVENC"})
        output_file = loop.run_until_complete(
            engine.render_final_video_nvenc(
                input_video=file_path,
                visual_data=visual_data,
                speech_data=speech_data,
                cleaned_audio=cleaned_audio,
                sfx_tracks=sfx_tracks,
                options=options,
                project_id=project_id
            )
        )

        return {
            "progress": 100,
            "status": "COMPLETED",
            "stage": "اكتمل المونتاج التلقائي بنجاح",
            "output_file": output_file,
            "output_url": f"/api/v1/projects/{project_id}/download",
            "visual_summary": visual_data,
            "speech_summary": speech_data
        }
    except Exception as e:
        self.update_state(state="FAILURE", meta={"error": str(e), "stage": f"خطأ: {str(e)}"})
        raise e
    finally:
        loop.close()
