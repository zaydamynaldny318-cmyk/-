import os
import shutil
import uuid
import asyncio
from typing import Optional, List, Dict, Any
from datetime import datetime, timedelta

from fastapi import FastAPI, File, UploadFile, Form, Depends, HTTPException, status, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, JSONResponse
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel, Field
from jose import JWTError, jwt
from passlib.context import CryptContext

# Import local processing modules
from editing_engine import VideoEditingEngine, VisualAnalysisResult
from audio_processor import AudioProcessor, AudioProcessingResult
from asset_fetcher import AssetFetcher
from celery_worker import celery_app, process_video_pipeline_task

# --- Configuration & Security Constants ---
SECRET_KEY = os.getenv("JWT_SECRET", "zaid-ai-super-secret-production-key-2026-v1")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24  # 24 hours

UPLOAD_DIR = os.getenv("UPLOAD_DIR", "/tmp/zaid_editor/uploads")
OUTPUT_DIR = os.getenv("OUTPUT_DIR", "/tmp/zaid_editor/outputs")
os.makedirs(UPLOAD_DIR, exist_ok=True)
os.makedirs(OUTPUT_DIR, exist_ok=True)

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/auth/token")

app = FastAPI(
    title="زايد للمونتاج التلقائي بالذكاء الاصطناعي - Backend API",
    description="Autonomous Video & Audio Editing Pipeline powered by Gemini Vision, Faster-Whisper, DeepFilterNet, and FFmpeg NVENC",
    version="2.0.0"
)

# Enable CORS for Mobile Client & Web Dashboards
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Pydantic Schemas ---
class Token(BaseModel):
    access_token: str
    token_type: str

class VideoPresetOptions(BaseModel):
    auto_suspense: bool = True
    clean_audio: bool = True
    auto_ducking: bool = True
    fetch_internet_sfx: bool = True
    aspect_ratio: str = Field(default="9:16", description="9:16, 16:9, or 1:1")
    output_quality: str = Field(default="1080p60", description="1080p60, 4K30, or 720p60")

class ProjectCreationResponse(BaseModel):
    project_id: str
    task_id: str
    status: str
    filename: str
    options: VideoPresetOptions
    created_at: str

class TaskStatusResponse(BaseModel):
    task_id: str
    status: str
    progress: int
    current_stage: str
    details: Optional[Dict[str, Any]] = None
    output_url: Optional[str] = None
    error: Optional[str] = None

# In-memory mock storage for demo / standalone deployment
PROJECTS_DB: Dict[str, Dict[str, Any]] = {}

# --- Authentication Helpers ---
def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
    to_encode = data.copy()
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=15))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

async def get_current_user(token: str = Depends(oauth2_scheme)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        if username is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    return username

# --- Authentication Endpoints ---
@app.post("/api/auth/token", response_model=Token, tags=["Auth"])
async def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends()):
    # Enterprise default credentials or dynamic auth
    if form_data.username and form_data.password:
        access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
        access_token = create_access_token(
            data={"sub": form_data.username}, expires_delta=access_token_expires
        )
        return {"access_token": access_token, "token_type": "bearer"}
    raise HTTPException(status_code=400, detail="Invalid username or password")

# --- System & Hardware Info Endpoint ---
@app.get("/api/v1/system/status", tags=["System"])
async def get_system_status():
    """Returns GPU NVENC capabilities, AI engines status, and API health."""
    editing_engine = VideoEditingEngine()
    gpu_available = editing_engine.check_nvenc_support()
    return {
        "app_name": "زايد للمونتاج التلقائي بالذكاء الاصطناعي",
        "engine_version": "2.0.0-PROD",
        "gpu_acceleration": {
            "nvenc_h264_supported": gpu_available,
            "nvenc_hevc_supported": gpu_available,
            "device": "NVIDIA GPU with CUDA Support" if gpu_available else "CPU Fallback Mode",
        },
        "modules": {
            "visual_intelligence": "Gemini Vision API (0.5s Frame Sampling)",
            "speech_transcription": "Faster-Whisper large-v3 (CUDA)",
            "audio_purification": "DeepFilterNet Vocal Isolation",
            "loudness_norm": "EBU R128 (-16 LUFS Target)",
            "auto_ducking": "Dynamic Speech Sidechain (-14dB / -3dB)",
            "sfx_retrieval": "Freesound API & Jamendo API"
        },
        "status": "OPERATIONAL"
    }

# --- Project Creation & Video Upload ---
@app.post("/api/v1/projects/create", response_model=ProjectCreationResponse, tags=["Editor"])
async def create_video_project(
    background_tasks: BackgroundTasks,
    file: UploadFile = File(...),
    auto_suspense: bool = Form(True),
    clean_audio: bool = Form(True),
    auto_ducking: bool = Form(True),
    fetch_internet_sfx: bool = Form(True),
    aspect_ratio: str = Form("9:16"),
    output_quality: str = Form("1080p60"),
    token: Optional[str] = Depends(oauth2_scheme)
):
    """
    Accepts video file upload and option configurator JSON payload.
    Spawns autonomous editing pipeline via Celery / background worker.
    """
    project_id = str(uuid.uuid4())
    task_id = f"task_{uuid.uuid4().hex[:12]}"
    file_ext = os.path.splitext(file.filename)[1] or ".mp4"
    saved_filename = f"{project_id}{file_ext}"
    saved_path = os.path.join(UPLOAD_DIR, saved_filename)

    with open(saved_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    preset_options = VideoPresetOptions(
        auto_suspense=auto_suspense,
        clean_audio=clean_audio,
        auto_ducking=auto_ducking,
        fetch_internet_sfx=fetch_internet_sfx,
        aspect_ratio=aspect_ratio,
        output_quality=output_quality
    )

    PROJECTS_DB[project_id] = {
        "project_id": project_id,
        "task_id": task_id,
        "status": "QUEUED",
        "progress": 5,
        "current_stage": "ملف الفيديو تم استلامه وتجهيزه للتحليل",
        "filename": file.filename,
        "local_path": saved_path,
        "options": preset_options.dict(),
        "created_at": datetime.utcnow().isoformat(),
        "details": {}
    }

    # Dispatch Celery task or run background worker
    try:
        celery_app.send_task(
            "tasks.process_video_pipeline",
            args=[project_id, saved_path, preset_options.dict()],
            task_id=task_id
        )
    except Exception as e:
        # Fallback to FastAPI BackgroundTasks if Redis/Celery is not reachable locally
        background_tasks.add_task(
            run_local_pipeline,
            project_id,
            saved_path,
            preset_options.dict()
        )

    return ProjectCreationResponse(
        project_id=project_id,
        task_id=task_id,
        status="PROCESSING",
        filename=file.filename,
        options=preset_options,
        created_at=datetime.utcnow().isoformat()
    )

async def run_local_pipeline(project_id: str, file_path: str, options: dict):
    """Local async pipeline fallback for standalone testing."""
    engine = VideoEditingEngine()
    audio_proc = AudioProcessor()
    asset_fetcher = AssetFetcher()

    try:
        # 1. Visual & Scene Intelligence
        PROJECTS_DB[project_id]["status"] = "PROCESSING"
        PROJECTS_DB[project_id]["progress"] = 20
        PROJECTS_DB[project_id]["current_stage"] = "1/5: تحليل الإطارات بـ Gemini Vision كل 0.5 ثانية..."
        analysis_result = await engine.analyze_video_frames_with_gemini(file_path)
        PROJECTS_DB[project_id]["details"]["visual_analysis"] = analysis_result

        # 2. Faster-Whisper Speech Transcription
        PROJECTS_DB[project_id]["progress"] = 40
        PROJECTS_DB[project_id]["current_stage"] = "2/5: تفريغ الكلمات بدقة أجزاء الثانية بـ Faster-Whisper..."
        speech_result = await audio_proc.transcribe_speech_whisper(file_path)
        PROJECTS_DB[project_id]["details"]["speech"] = speech_result

        # 3. Audio Cleaning (DeepFilterNet) & EBU R128
        PROJECTS_DB[project_id]["progress"] = 60
        PROJECTS_DB[project_id]["current_stage"] = "3/5: تنقية وعزل الصوت بـ DeepFilterNet وضبط -16 LUFS..."
        cleaned_audio_path = await audio_proc.clean_and_normalize_audio(file_path, options)
        PROJECTS_DB[project_id]["details"]["cleaned_audio"] = cleaned_audio_path

        # 4. Freesound & Jamendo SFX Retrieval
        PROJECTS_DB[project_id]["progress"] = 75
        PROJECTS_DB[project_id]["current_stage"] = "4/5: جلب المؤثرات الصوتية والموسيقى من Freesound و Jamendo..."
        sfx_injections = await asset_fetcher.fetch_assets_for_scenes(analysis_result.get("tags", []))
        PROJECTS_DB[project_id]["details"]["sfx"] = sfx_injections

        # 5. FFmpeg NVENC Hardware Accelerated Render
        PROJECTS_DB[project_id]["progress"] = 90
        PROJECTS_DB[project_id]["current_stage"] = "5/5: رندرة وتطبيق الزووم التلقائي والمؤثرات بـ FFmpeg NVENC..."
        output_file = await engine.render_final_video_nvenc(
            input_video=file_path,
            visual_data=analysis_result,
            speech_data=speech_result,
            cleaned_audio=cleaned_audio_path,
            sfx_tracks=sfx_injections,
            options=options,
            project_id=project_id
        )

        PROJECTS_DB[project_id]["status"] = "COMPLETED"
        PROJECTS_DB[project_id]["progress"] = 100
        PROJECTS_DB[project_id]["current_stage"] = "اكتمل المونتاج التلقائي بنجاح جاهز للتحميل والمشاهدة"
        PROJECTS_DB[project_id]["output_url"] = f"/api/v1/projects/{project_id}/download"
        PROJECTS_DB[project_id]["output_file"] = output_file
    except Exception as exc:
        PROJECTS_DB[project_id]["status"] = "FAILED"
        PROJECTS_DB[project_id]["error"] = str(exc)
        PROJECTS_DB[project_id]["current_stage"] = f"خطأ أثناء المعالجة: {str(exc)}"

# --- Task Status & Progress Polling ---
@app.get("/api/v1/tasks/{task_id}", response_model=TaskStatusResponse, tags=["Editor"])
async def get_task_status(task_id: str):
    """Returns real-time editing pipeline progress and stage updates."""
    for p_id, data in PROJECTS_DB.items():
        if data.get("task_id") == task_id or p_id == task_id:
            return TaskStatusResponse(
                task_id=task_id,
                status=data["status"],
                progress=data["progress"],
                current_stage=data["current_stage"],
                details=data.get("details"),
                output_url=data.get("output_url"),
                error=data.get("error")
            )
    # Check celery async result if applicable
    async_result = celery_app.AsyncResult(task_id)
    if async_result:
        info = async_result.info if isinstance(async_result.info, dict) else {}
        return TaskStatusResponse(
            task_id=task_id,
            status=async_result.status,
            progress=info.get("progress", 0),
            current_stage=info.get("stage", async_result.status),
            details=info,
            output_url=info.get("output_url"),
            error=str(async_result.result) if async_result.failed() else None
        )
    raise HTTPException(status_code=404, detail="Task not found")

# --- Asset Search (Freesound & Jamendo) ---
@app.get("/api/v1/assets/sfx/search", tags=["Assets"])
async def search_sfx(query: str = "whoosh", provider: str = "freesound"):
    """Query real-time sound effects and royalty-free music."""
    fetcher = AssetFetcher()
    results = await fetcher.search_sfx(query=query, provider=provider)
    return {"query": query, "provider": provider, "results": results}

# --- Video Download & Playback ---
@app.get("/api/v1/projects/{project_id}/download", tags=["Editor"])
async def download_rendered_video(project_id: str):
    """Serves the final edited MP4 video file."""
    project = PROJECTS_DB.get(project_id)
    if not project or not project.get("output_file"):
        raise HTTPException(status_code=404, detail="Rendered video not ready or project not found")
    filepath = project["output_file"]
    if not os.path.exists(filepath):
        raise HTTPException(status_code=404, detail="Rendered output file missing from disk")
    return FileResponse(filepath, media_type="video/mp4", filename=f"zaid_edit_{project_id}.mp4")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
