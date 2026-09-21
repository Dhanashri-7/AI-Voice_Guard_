"""
VoiceGuard ML Engine - ONNX Export & Optimization Pipeline
Exports PyTorch SpectroCNNAttention to ONNX with dynamic batch and time dimensions.
Optimized for ONNX Runtime Mobile / Android deployment.
"""

import os
import torch
import sys

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from models.spectro_cnn import SpectroCNNAttention

def export_to_onnx():
    base_dir = os.path.dirname(__file__)
    model_path = os.path.join(base_dir, "..", "models", "voiceguard_spectro_cnn.pt")
    output_onnx_path = os.path.join(base_dir, "..", "models", "voiceguard_spectro_cnn.onnx")

    device = torch.device("cpu")
    model = SpectroCNNAttention(n_mels=80, num_classes=1).to(device)

    if os.path.exists(model_path):
        print(f"[*] Loading PyTorch weights from: {model_path}")
        model.load_state_dict(torch.load(model_path, map_location=device))
    else:
        print("[!] Weights not found, exporting with initialized weights.")

    model.eval()

    # Dummy input: [Batch=1, Channels=1, Mel=80, Time=94]
    dummy_input = torch.randn(1, 1, 80, 94, dtype=torch.float32)

    print(f"[*] Exporting model to ONNX: {output_onnx_path}")
    try:
        torch.onnx.export(
            model,
            dummy_input,
            output_onnx_path,
            export_params=True,
            opset_version=14,
            do_constant_folding=True,
            input_names=["input_mel_spectrogram"],
            output_names=["synthetic_voice_probability"],
            dynamic_axes={
                "input_mel_spectrogram": {0: "batch_size", 3: "time_frames"},
                "synthetic_voice_probability": {0: "batch_size"}
            },
            dynamo=False
        )
    except TypeError:
        torch.onnx.export(
            model,
            dummy_input,
            output_onnx_path,
            export_params=True,
            opset_version=14,
            do_constant_folding=True,
            input_names=["input_mel_spectrogram"],
            output_names=["synthetic_voice_probability"],
            dynamic_axes={
                "input_mel_spectrogram": {0: "batch_size", 3: "time_frames"},
                "synthetic_voice_probability": {0: "batch_size"}
            }
        )

    print(f"[+] ONNX export successful: {output_onnx_path} ({os.path.getsize(output_onnx_path) / 1024:.1f} KB)")

if __name__ == "__main__":
    export_to_onnx()
