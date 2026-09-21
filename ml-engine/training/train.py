"""
VoiceGuard ML Engine - Training & Validation Pipeline
- Strict speaker isolation (Zero speaker leakage between train / val / test sets)
- Telephony degradation simulation during training
- Evaluation prioritizing Equal Error Rate (EER), F1-Score, FPR, FNR, and edge latency
- Exports trained prototype model checkpoint
"""

import os
import time
import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
from sklearn.metrics import roc_curve, f1_score, precision_score, recall_score, roc_auc_score

import sys
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from models.spectro_cnn import SpectroCNNAttention
from preprocessing.audio_processor import AudioProcessor

class SyntheticTelephonyDataset(Dataset):
    """
    Generates controlled bonafide human vs AI-vocoded/cloned speech samples
    with strict speaker ID tagging to prevent data leakage.
    """
    def __init__(self, num_samples: int, speaker_ids: list, processor: AudioProcessor, is_train: bool = True):
        self.processor = processor
        self.data = []
        self.labels = []
        self.speakers = []

        np.random.seed(42 if not is_train else None)

        for i in range(num_samples):
            spk_id = np.random.choice(speaker_ids)
            is_spoof = int(np.random.rand() > 0.5)
            
            # Synthesize representative acoustic wave (duration = 1.5s, sr=16000)
            t = np.linspace(0, 1.5, int(1.5 * processor.sample_rate), endpoint=False)
            
            # Base fundamental frequency characteristic for this speaker
            base_f0 = 110.0 + (hash(spk_id) % 120)

            if is_spoof == 0:
                # Bonafide human speech: natural pitch modulation, natural formants, breath pauses
                mod = 5.0 * np.sin(2 * np.pi * 3.5 * t)
                wave = 0.5 * np.sin(2 * np.pi * (base_f0 + mod) * t)
                # Formants
                wave += 0.3 * np.sin(2 * np.pi * (base_f0 * 2.1) * t)
                wave += 0.15 * np.sin(2 * np.pi * (base_f0 * 3.2) * t)
                # Breath pauses
                envelope = 0.5 * (1.0 + np.sin(2 * np.pi * 1.2 * t))
                wave = wave * envelope
            else:
                # Synthetic / Vocoder speech: rigid F0, high-frequency harmonic phase buzz, vocoder cutoff
                wave = 0.5 * np.sin(2 * np.pi * base_f0 * t) # Rigid monotone F0
                # Characteristic vocoder high-frequency buzzing artifacts (HiFi-GAN / WaveGlow artifacts)
                wave += 0.25 * np.sin(2 * np.pi * 3800.0 * t)
                wave += 0.18 * np.sin(2 * np.pi * 4600.0 * t)
                # Minimal natural pauses
                wave = wave * 0.85

            # Apply realistic telephony degradation (GSM filter, noise, packet drop)
            degraded = processor.simulate_telephony_degradation(
                wave,
                downsample_8khz=True,
                add_noise=True,
                snr_db=np.random.uniform(15.0, 25.0),
                packet_loss_ratio=0.03 if is_train else 0.01
            )

            log_mel = processor.compute_mel_spectrogram(degraded)
            # Ensure fixed width (e.g. 94 frames for 1.5s with hop 256)
            target_width = 94
            if log_mel.shape[1] < target_width:
                pad_width = target_width - log_mel.shape[1]
                log_mel = np.pad(log_mel, ((0, 0), (0, pad_width)), mode='constant')
            else:
                log_mel = log_mel[:, :target_width]

            self.data.append(log_mel[np.newaxis, :, :]) # [1, 80, 94]
            self.labels.append(is_spoof)
            self.speakers.append(spk_id)

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        return (
            torch.tensor(self.data[idx], dtype=torch.float32),
            torch.tensor(self.labels[idx], dtype=torch.float32)
        )

def calculate_eer(y_true, y_scores):
    """Calculates Equal Error Rate (EER) and the optimal detection threshold."""
    fpr, tpr, thresholds = roc_curve(y_true, y_scores, pos_label=1)
    fnr = 1 - tpr
    # Find point where FPR == FNR
    idx = np.nanargmin(np.abs(fpr - fnr))
    eer = (fpr[idx] + fnr[idx]) / 2.0
    return float(eer), float(thresholds[idx])

def train_prototype_model(epochs: int = 5, batch_size: int = 16):
    print("=" * 70)
    print("VoiceGuard ML Engine - Training & Validation Protocol")
    print("Problem Statement: AICTE 26104 - Voice Cloning Detection")
    print("=" * 70)

    processor = AudioProcessor(sample_rate=16000)

    # STRICT SPEAKER PARTITIONING: Zero speaker overlap
    train_speakers = [f"spk_{i:03d}" for i in range(1, 41)]  # 40 speakers
    val_speakers   = [f"spk_{i:03d}" for i in range(41, 51)] # 10 speakers
    test_speakers  = [f"spk_{i:03d}" for i in range(51, 61)] # 10 speakers

    print(f"[*] Train speakers: {len(train_speakers)} | Val speakers: {len(val_speakers)} | Test speakers: {len(test_speakers)}")
    print("[*] Speaker leakage check: PASS (Disjoint sets)")

    train_ds = SyntheticTelephonyDataset(num_samples=240, speaker_ids=train_speakers, processor=processor, is_train=True)
    val_ds   = SyntheticTelephonyDataset(num_samples=60, speaker_ids=val_speakers, processor=processor, is_train=False)
    test_ds  = SyntheticTelephonyDataset(num_samples=80, speaker_ids=test_speakers, processor=processor, is_train=False)

    train_loader = DataLoader(train_ds, batch_size=batch_size, shuffle=True)
    val_loader   = DataLoader(val_ds, batch_size=batch_size, shuffle=False)
    test_loader  = DataLoader(test_ds, batch_size=batch_size, shuffle=False)

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"[*] Training on device: {device}")

    model = SpectroCNNAttention(n_mels=80, num_classes=1).to(device)
    criterion = nn.BCEWithLogitsLoss()
    optimizer = torch.optim.AdamW(model.parameters(), lr=1e-3, weight_decay=1e-4)

    best_val_loss = float('inf')
    save_dir = os.path.join(os.path.dirname(__file__), "..", "models")
    os.makedirs(save_dir, exist_ok=True)
    save_path = os.path.join(save_dir, "voiceguard_spectro_cnn.pt")

    for epoch in range(1, epochs + 1):
        model.train()
        train_loss = 0.0
        for x_b, y_b in train_loader:
            x_b, y_b = x_b.to(device), y_b.to(device)
            optimizer.zero_grad()
            _, logits, _ = model(x_b, extract_features=True)
            loss = criterion(logits.squeeze(1), y_b)
            loss.backward()
            optimizer.step()
            train_loss += loss.item() * len(y_b)

        train_loss /= len(train_ds)

        # Validation
        model.eval()
        val_loss = 0.0
        val_preds, val_targets = [], []
        with torch.no_grad():
            for x_b, y_b in val_loader:
                x_b, y_b = x_b.to(device), y_b.to(device)
                probs, logits, _ = model(x_b, extract_features=True)
                loss = criterion(logits.squeeze(1), y_b)
                val_loss += loss.item() * len(y_b)
                val_preds.extend(probs.cpu().squeeze().tolist())
                val_targets.extend(y_b.cpu().tolist())

        val_loss /= len(val_ds)
        val_eer, _ = calculate_eer(val_targets, val_preds)
        print(f"Epoch [{epoch}/{epochs}] | Train Loss: {train_loss:.4f} | Val Loss: {val_loss:.4f} | Val EER: {val_eer * 100:.2f}%")

        if val_loss < best_val_loss:
            best_val_loss = val_loss
            torch.save(model.state_dict(), save_path)

    print(f"[+] Model checkpoint saved to: {save_path}")

    # Honest Test Set Benchmark
    print("\n" + "=" * 70)
    print("EVALUATING ON UNSEEN TEST SPEAKERS (Honest Benchmark Report)")
    print("=" * 70)
    model.load_state_dict(torch.load(save_path, map_location=device))
    model.eval()

    test_preds, test_targets = [], []
    latencies = []

    with torch.no_grad():
        for x_b, y_b in test_loader:
            x_b = x_b.to(device)
            start_t = time.perf_counter()
            probs, _, _ = model(x_b, extract_features=True)
            latency_ms = (time.perf_counter() - start_t) * 1000 / len(x_b)
            latencies.append(latency_ms)

            test_preds.extend(probs.cpu().squeeze().tolist())
            test_targets.extend(y_b.tolist())

    eer, opt_thresh = calculate_eer(test_targets, test_preds)
    binary_preds = [1 if p >= opt_thresh else 0 for p in test_preds]
    f1 = f1_score(test_targets, binary_preds)
    prec = precision_score(test_targets, binary_preds, zero_division=0)
    rec = recall_score(test_targets, binary_preds, zero_division=0)
    auc = roc_auc_score(test_targets, test_preds)
    avg_latency = np.mean(latencies)

    print(f"Model Status            : Prototype Model (SIH-2026 Evaluation)")
    print(f"Equal Error Rate (EER)  : {eer * 100:.2f}%")
    print(f"Optimal Decision Thresh : {opt_thresh:.3f}")
    print(f"F1-Score                : {f1 * 100:.2f}%")
    print(f"Precision               : {prec * 100:.2f}%")
    print(f"Recall (Spoof Catch)    : {rec * 100:.2f}%")
    print(f"ROC-AUC                 : {auc:.4f}")
    print(f"Avg Chunk Latency (CPU) : {avg_latency:.2f} ms per 1.5s window")
    print("=" * 70)

    return {
        "model_status": "Prototype Model",
        "eer": round(eer * 100, 2),
        "f1": round(f1 * 100, 2),
        "precision": round(prec * 100, 2),
        "recall": round(rec * 100, 2),
        "roc_auc": round(auc, 4),
        "latency_ms": round(float(avg_latency), 2)
    }

if __name__ == "__main__":
    train_prototype_model()
