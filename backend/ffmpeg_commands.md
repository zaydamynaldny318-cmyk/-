# FFmpeg CLI Commands & Pipelines - زايد للمونتاج التلقائي بالذكاء الاصطناعي

This document contains the exact production-grade FFmpeg CLI commands used in the autonomous video editing engine.

---

## 1. Dynamic Audio Ducking (-14dB Speech / -3dB Silent Pauses)

When voice is detected via **Faster-Whisper** word timestamps (e.g. `[1.2s - 4.5s]` and `[6.0s - 9.2s]`), background music is ducked dynamically to **-14dB** (`0.1995` amplitude), and restored to **-3dB** (`0.7079` amplitude) during pauses:

```bash
ffmpeg -y -i vocal_clean.wav -i bg_music.mp3 \
  -filter_complex "\
    [1:a]volume=eval=frame:volume='if(gte(between(t,1.2,4.5)+between(t,6.0,9.2),1),0.1995,0.7079)'[bg_ducked];\
    [0:a][bg_ducked]amix=inputs=2:duration=first:dropout_transition=2[mixed];\
    [mixed]loudnorm=I=-16:TP=-1.5:LRA=11[aout]" \
  -map "[aout]" -c:a aac -b:a 320k ducked_master.m4a
```

---

## 2. Dynamic 1.15x Smooth Zoom Cut (Auto-Suspense Hook)

Applied at suspense hook timestamps (e.g. from `t=2.5s` to `t=4.0s`), smoothly cropping the frame by `1.15x` and scaling back to target resolution:

```bash
ffmpeg -y -i input.mp4 \
  -vf "crop=w='if(between(t,2.5,4.0),iw/1.15,iw)':h='if(between(t,2.5,4.0),ih/1.15,ih)':x='(iw-ow)/2':y='(ih-oh)/2',scale=1080:1920" \
  -c:v h264_nvenc -preset p6 -cq 19 -r 60 -c:a copy output_zoom.mp4
```

---

## 3. Climax Flash & Vignette Transition

Triggered on climax moments (e.g., impact at `t=5.0s`), combining a 120ms optical flash with a radial cinematic vignette:

```bash
ffmpeg -y -i input.mp4 \
  -vf "eq=brightness='if(between(t,5.0,5.12),0.45,0)':eval=frame,vignette='PI/3.5*if(between(t,5.0,6.2),1,0)'" \
  -c:v h264_nvenc -preset p6 -cq 19 -c:a copy output_climax.mp4
```

---

## 4. Speed Ramp (0.8x Slow-Mo on Dramatic Action)

Applies speed ramping on action intervals (e.g. `t=7.0s` to `t=9.0s`), slowing video to `0.8x` (`PTS * 1.25`) with matching pitch-corrected audio (`atempo=0.8`):

```bash
ffmpeg -y -i input.mp4 \
  -filter_complex "\
    [0:v]trim=start=0:end=7.0,setpts=PTS-STARTPTS[v1];\
    [0:a]atrim=start=0:end=7.0,asetpts=PTS-STARTPTS[a1];\
    [0:v]trim=start=7.0:end=9.0,setpts=1.25*(PTS-STARTPTS)[v2];\
    [0:a]atrim=start=7.0:end=9.0,asetpts=PTS-STARTPTS,atempo=0.8[a2];\
    [0:v]trim=start=9.0,setpts=PTS-STARTPTS[v3];\
    [0:a]atrim=start=9.0,asetpts=PTS-STARTPTS[a3];\
    [v1][a1][v2][a2][v3][a3]concat=n=3:v=1:a=1[vout][aout]" \
  -map "[vout]" -map "[aout]" -c:v h264_nvenc -preset p6 -c:a aac -b:a 320k output_slowmo.mp4
```

---

## 5. Multi-Track SFX Injection (Whoosh & Dramatic Boom)

Injecting downloaded Freesound/Jamendo sound effects at precise timestamps (`adelay=2500|2500` for 2.5s, `adelay=4000|4000` for 4.0s):

```bash
ffmpeg -y -i master_video.mp4 \
  -i whoosh_sfx.mp3 \
  -i boom_climax.mp3 \
  -filter_complex "\
    [1:a]adelay=2500|2500,volume=0.85[sfx1];\
    [2:a]adelay=4000|4000,volume=1.0[sfx2];\
    [0:a][sfx1][sfx2]amix=inputs=3:dropout_transition=2[aout]" \
  -map 0:v:0 -map "[aout]" -c:v copy -c:a aac -b:a 320k final_sfx_merged.mp4
```

---

## 6. End-to-End NVIDIA NVENC Hyper-Fast Master Render

Final production render utilizing CUDA hardware decoding and NVENC encoding:

```bash
ffmpeg -y -hwaccel cuda -hwaccel_output_format cuda -i raw_input.mp4 \
  -vf "scale_cuda=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920" \
  -c:v h264_nvenc -preset p6 -tune hq -rc vbr -cq 19 \
  -b:v 8M -maxrate 12M -bufsize 24M \
  -c:a aac -b:a 320k zaid_master_export.mp4
```
