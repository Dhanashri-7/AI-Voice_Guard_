import os
import cv2
import numpy as np
from PIL import Image, ImageDraw, ImageFont

def render_video():
    brain_dir = r"C:\Users\sp305\.gemini\antigravity\brain\0e6ea2c3-3b2f-49d7-9db7-56b8addee3bd"
    output_dir = r"C:\Users\sp305\.gemini\antigravity\scratch\voiceguard\assets"
    os.makedirs(output_dir, exist_ok=True)
    output_path = os.path.join(output_dir, "voiceguard_demo_video.mp4")
    artifact_output = os.path.join(brain_dir, "voiceguard_demo_video.mp4")

    scenes = [
        {
            "img": os.path.join(brain_dir, "scam_victim_fear_1790313564563.jpg"),
            "title": "THE THREAT: AI VOICE CLONING SCAMS",
            "desc": "Fraudsters clone voices in seconds to trigger panic, harvest OTPs, and steal savings.",
            "zoom": "in"
        },
        {
            "img": os.path.join(brain_dir, "scam_victims_society_1790313636752.jpg"),
            "title": "A NATIONWIDE DIGITAL CRISIS",
            "desc": "Every day, thousands of elders, students, and families fall victim to deepfake extortion.",
            "zoom": "out"
        },
        {
            "img": os.path.join(brain_dir, "voiceguard_solution_active_1790313714778.jpg"),
            "title": "THE SOLUTION: VOICEGUARD ACTIVE DEFENSE",
            "desc": "On-device acoustic forensics detects synthetic vocoder phase anomalies in real time.",
            "zoom": "in"
        },
        {
            "img": os.path.join(brain_dir, "voiceguard_peace_security_1790313795834.jpg"),
            "title": "TOTAL CITIZEN PEACE OF MIND",
            "desc": "Active ScamBuster Honeypot, Zero Raw Audio Retention & DPDP Act 2023 compliance.",
            "zoom": "out"
        },
        {
            "img": os.path.join(brain_dir, "voiceguard_title_outro_1790313901836.jpg"),
            "title": "VOICEGUARD — AI VOICE INTEGRITY",
            "desc": "Safety Behind Every Call. | Smart India Hackathon 2026",
            "zoom": "in"
        }
    ]

    width, height = 1920, 1080
    fps = 30
    scene_duration_sec = 5.5
    fade_duration_sec = 0.8
    frames_per_scene = int(fps * scene_duration_sec)
    fade_frames = int(fps * fade_duration_sec)

    # Use mp4v fourcc
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter(output_path, fourcc, fps, (width, height))

    try:
        font_title = ImageFont.truetype("arialbd.ttf", 36)
        font_desc = ImageFont.truetype("arial.ttf", 26)
    except:
        font_title = ImageFont.load_default()
        font_desc = ImageFont.load_default()

    def create_scene_frame(img_pil, zoom_factor, title_text, desc_text):
        # Apply subtle zoom
        w, h = img_pil.size
        crop_w = int(w / zoom_factor)
        crop_h = int(h / zoom_factor)
        x1 = (w - crop_w) // 2
        y1 = (h - crop_h) // 2
        cropped = img_pil.crop((x1, y1, x1 + crop_w, y1 + crop_h))
        resized = cropped.resize((width, height), Image.Resampling.LANCZOS)

        # Draw semi-transparent subtitle card at bottom
        draw = ImageDraw.Draw(resized, "RGBA")
        
        # Subtitle overlay box
        box_y1 = height - 160
        box_y2 = height - 40
        box_x1 = 120
        box_x2 = width - 120
        
        # Rounded rectangle background
        draw.rounded_rectangle(
            [(box_x1, box_y1), (box_x2, box_y2)],
            radius=18,
            fill=(10, 15, 30, 215),
            outline=(56, 189, 248, 160),
            width=2
        )

        # Center title
        title_bbox = draw.textbbox((0, 0), title_text, font=font_title)
        title_w = title_bbox[2] - title_bbox[0]
        draw.text(((width - title_w) // 2, box_y1 + 16), title_text, font=font_title, fill=(253, 230, 138, 255))

        # Center description
        desc_bbox = draw.textbbox((0, 0), desc_text, font=font_desc)
        desc_w = desc_bbox[2] - desc_bbox[0]
        draw.text(((width - desc_w) // 2, box_y1 + 65), desc_text, font=font_desc, fill=(255, 255, 255, 240))

        # Convert back to BGR for cv2
        frame_rgb = np.array(resized.convert("RGB"))
        return cv2.cvtColor(frame_rgb, cv2.COLOR_RGB2BGR)

    rendered_scenes = []
    print("Pre-rendering scene frames with smooth camera motion...")

    for s_idx, scene in enumerate(scenes):
        print(f"Loading scene {s_idx + 1}/{len(scenes)}: {scene['title']}")
        raw_img = Image.open(scene['img']).convert("RGB")
        raw_img = raw_img.resize((width + 200, height + 112), Image.Resampling.LANCZOS)
        
        scene_frames = []
        for f in range(frames_per_scene):
            t = f / float(frames_per_scene)
            if scene['zoom'] == 'in':
                zoom = 1.0 + (0.08 * t)
            else:
                zoom = 1.08 - (0.08 * t)
            
            frame_bgr = create_scene_frame(raw_img, zoom, scene['title'], scene['desc'])
            scene_frames.append(frame_bgr)
        rendered_scenes.append(scene_frames)

    print("Writing video with cross-dissolve transitions...")
    for s_idx, scene_frames in enumerate(rendered_scenes):
        next_frames = rendered_scenes[s_idx + 1] if s_idx + 1 < len(rendered_scenes) else None
        
        for f_idx in range(len(scene_frames)):
            frame = scene_frames[f_idx]
            
            # Transition cross-fade at the end of the scene
            if next_frames is not None and f_idx >= len(scene_frames) - fade_frames:
                alpha = (f_idx - (len(scene_frames) - fade_frames)) / float(fade_frames)
                next_frame = next_frames[f_idx - (len(scene_frames) - fade_frames)]
                blended = cv2.addWeighted(frame, 1.0 - alpha, next_frame, alpha, 0)
                out.write(blended)
            elif next_frames is None and f_idx >= len(scene_frames) - fade_frames:
                # Fade to black on last scene
                alpha = (f_idx - (len(scene_frames) - fade_frames)) / float(fade_frames)
                black = np.zeros_like(frame)
                blended = cv2.addWeighted(frame, 1.0 - alpha, black, alpha, 0)
                out.write(blended)
            else:
                out.write(frame)

    out.release()
    print(f"Video saved successfully at: {output_path}")

    # Copy to brain artifact directory as well
    import shutil
    shutil.copyfile(output_path, artifact_output)
    print(f"Artifact copy saved at: {artifact_output}")

    filesize_mb = os.path.getsize(output_path) / (1024 * 1024)
    print(f"Output File Size: {filesize_mb:.2f} MB")

if __name__ == "__main__":
    render_video()
