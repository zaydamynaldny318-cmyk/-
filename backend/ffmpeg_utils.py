"""
FFmpeg Exact CLI Commands & Python Wrappers
Used for Audio Ducking, Dynamic 1.15x Zoom-In Keyframing, Climax Vignette/Flash Transitions, and Multi-track Merging.
"""

import subprocess
from typing import List, Tuple

def build_zoom_in_keyframe_command(
    input_video: str,
    output_video: str,
    t_start: float,
    t_end: float,
    max_zoom: float = 1.15,
    fps: int = 60
) -> str:
    """
    Returns exact CLI command for 1.15x dynamic smooth zoom cut on suspense hook.
    Uses FFmpeg crop dynamic expression:
    """
    cmd = (
        f'ffmpeg -y -i "{input_video}" '
        f'-vf "crop=w=\'if(between(t,{t_start},{t_end}),iw/{max_zoom},iw)\':'
        f'h=\'if(between(t,{t_start},{t_end}),ih/{max_zoom},ih)\':'
        f'x=\'(iw-ow)/2\':y=\'(ih-oh)/2\',scale=1080:1920" '
        f'-c:v h264_nvenc -preset p6 -cq 19 -r {fps} -c:a copy "{output_video}"'
    )
    return cmd

def build_climax_flash_vignette_command(
    input_video: str,
    output_video: str,
    t_climax: float,
    duration: float = 0.8
) -> str:
    """
    Returns exact CLI command for flash and vignette climax moment:
    """
    t_end = t_climax + duration
    cmd = (
        f'ffmpeg -y -i "{input_video}" '
        f'-vf "eq=brightness=\'if(between(t,{t_climax},{t_climax+0.12}),0.45,0)\':eval=frame,'
        f'vignette=\'PI/3.5*if(between(t,{t_climax},{t_end}),1,0)\'" '
        f'-c:v h264_nvenc -preset p6 -cq 19 -c:a copy "{output_video}"'
    )
    return cmd

def build_speed_ramp_command(
    input_video: str,
    output_video: str,
    t_start: float,
    t_end: float,
    speed_factor: float = 0.8
) -> str:
    """
    Returns exact CLI command for speed ramp (0.8x dramatic slow motion).
    PTS multiplier = 1 / speed_factor (1 / 0.8 = 1.25)
    Audio atempo filter = 0.8
    """
    pts_mult = 1.0 / speed_factor
    cmd = (
        f'ffmpeg -y -i "{input_video}" '
        f'-filter_complex "[0:v]trim=start=0:end={t_start},setpts=PTS-STARTPTS[v1];'
        f'[0:a]atrim=start=0:end={t_start},asetpts=PTS-STARTPTS[a1];'
        f'[0:v]trim=start={t_start}:end={t_end},setpts={pts_mult}*(PTS-STARTPTS)[v2];'
        f'[0:a]atrim=start={t_start}:end={t_end},asetpts=PTS-STARTPTS,atempo={speed_factor}[a2];'
        f'[0:v]trim=start={t_end},setpts=PTS-STARTPTS[v3];'
        f'[0:a]atrim=start={t_end},asetpts=PTS-STARTPTS[a3];'
        f'[v1][a1][v2][a2][v3][a3]concat=n=3:v=1:a=1[vout][aout]" '
        f'-map "[vout]" -map "[aout]" -c:v h264_nvenc -preset p6 -c:a aac "{output_video}"'
    )
    return cmd

def build_dynamic_audio_ducking_command(
    vocal_track: str,
    music_track: str,
    output_audio: str,
    speech_intervals: List[Tuple[float, float]]
) -> str:
    """
    Returns exact CLI command for dynamic audio ducking:
    - Background music ducks to -14dB (linear gain 0.1995) during speech
    - Restores to -3dB (linear gain 0.7079) during silence/pauses
    - Combined using amix with normalized EBU R128 loudness
    """
    if speech_intervals:
        cond_terms = "+".join([f"between(t,{s},{e})" for s, e in speech_intervals])
        duck_expr = f"if(gte({cond_terms},1),0.1995,0.7079)"
    else:
        duck_expr = "0.7079"

    cmd = (
        f'ffmpeg -y -i "{vocal_track}" -i "{music_track}" '
        f'-filter_complex "[1:a]volume=eval=frame:volume=\'{duck_expr}\'[bg_ducked];'
        f'[0:a][bg_ducked]amix=inputs=2:duration=first:dropout_transition=2[mixed];'
        f'[mixed]loudnorm=I=-16:TP=-1.5:LRA=11[out_a]" '
        f'-map "[out_a]" -c:a aac -b:a 320k "{output_audio}"'
    )
    return cmd

def build_nvenc_full_pipeline_command(
    input_video: str,
    output_video: str,
    vf_chain: str,
    aspect_ratio: str = "9:16",
    quality: str = "1080p60"
) -> str:
    """
    Production NVIDIA NVENC command for 1080p60 / 4K30 master export.
    """
    cmd = (
        f'ffmpeg -y -hwaccel cuda -hwaccel_output_format cuda -i "{input_video}" '
        f'-vf "{vf_chain}" '
        f'-c:v h264_nvenc -preset p6 -tune hq -rc vbr -cq 19 '
        f'-b:v 8M -maxrate 12M -bufsize 24M '
        f'-c:a aac -b:a 320k "{output_video}"'
    )
    return cmd
