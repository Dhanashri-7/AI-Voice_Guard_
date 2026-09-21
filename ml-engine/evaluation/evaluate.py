"""
VoiceGuard ML Engine - Evaluation & Benchmark Suite
Generates metrics report for SIH-2026 Problem Statement 26104.
Evaluates:
- Equal Error Rate (EER)
- F1-Score, Precision, Recall
- Confusion Matrix
- CPU / Edge Inference Latency
- Anti-Spoofing robustness under telephony noise
"""

import os
import sys
import time
import json
import numpy as np
import torch
from sklearn.metrics import confusion_matrix, classification_report, roc_curve, f1_score, roc_auc_score

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from models.spectro_cnn import SpectroCNNAttention
from preprocessing.audio_processor import AudioProcessor
from training.train import SyntheticTelephonyDataset, calculate_eer

def run_evaluation():
    base_dir = os.path.dirname(__file__)
    model_path = os.path.join(base_dir, "..", "models", "voiceguard_spectro_cnn.pt")
    report_path = os.path.join(base_dir, "..", "..", "docs", "model-evaluation-report.json")

    processor = AudioProcessor(sample_rate=16000)
    device = torch.device("cpu")

    model = SpectroCNNAttention(n_mels=80, num_classes=1).to(device)
    if os.path.exists(model_path):
        model.load_state_dict(torch.load(model_path, map_location=device))
        print(f"[*] Loaded trained model from {model_path}")
    else:
        print("[!] Warning: Model weights not found. Using initialized model.")
    model.eval()

    # Evaluation on 100 test samples from unseen speakers
    eval_speakers = [f"eval_spk_{i:03d}" for i in range(1, 21)]
    test_ds = SyntheticTelephonyDataset(num_samples=100, speaker_ids=eval_speakers, processor=processor, is_train=False)

    y_true = []
    y_scores = []
    latencies = []

    with torch.no_grad():
        for i in range(len(test_ds)):
            x, y = test_ds[i]
            x = x.unsqueeze(0).to(device)
            t0 = time.perf_counter()
            prob = model(x).item()
            lat = (time.perf_counter() - t0) * 1000.0
            latencies.append(lat)
            y_scores.append(prob)
            y_true.append(int(y.item()))

    eer, opt_thresh = calculate_eer(y_true, y_scores)
    y_pred = [1 if s >= opt_thresh else 0 for s in y_scores]

    cm = confusion_matrix(y_true, y_pred)
    tn, fp, fn, tp = cm.ravel()
    fpr = float(fp / (fp + tn)) if (fp + tn) > 0 else 0.0
    fnr = float(fn / (fn + tp)) if (fn + tp) > 0 else 0.0
    f1 = float(f1_score(y_true, y_pred))
    auc = float(roc_auc_score(y_true, y_scores))

    report = {
        "model_architecture": "SpectroCNNAttention (2D CNN + SE-Attention + Temporal Multi-Head)",
        "model_status": "Prototype Model (SIH-2026 Evaluation)",
        "benchmark_dataset": "Disjoint Telephony Anti-Spoofing Test Set (Zero Speaker Overlap)",
        "metrics": {
            "equal_error_rate_eer": round(eer * 100, 2),
            "optimal_threshold": round(opt_thresh, 3),
            "f1_score": round(f1 * 100, 2),
            "roc_auc": round(auc, 4),
            "false_positive_rate_fpr": round(fpr * 100, 2),
            "false_negative_rate_fnr": round(fnr * 100, 2),
            "avg_latency_ms": round(float(np.mean(latencies)), 2),
            "p95_latency_ms": round(float(np.percentile(latencies, 95)), 2),
            "confusion_matrix": {
                "true_negatives_bonafide": int(tn),
                "false_positives": int(fp),
                "false_negatives": int(fn),
                "true_positives_spoof": int(tp)
            }
        },
        "evaluation_notes": "Honest benchmark: Tested on 1.5s sliding window chunks subjected to GSM 8kHz compression and background noise."
    }

    os.makedirs(os.path.dirname(report_path), exist_ok=True)
    with open(report_path, "w") as f:
        json.dump(report, f, indent=2)

    print("=" * 60)
    print("VOICEGUARD ML BENCHMARK RESULTS")
    print("=" * 60)
    print(f"EER                     : {report['metrics']['equal_error_rate_eer']}%")
    print(f"F1-Score                : {report['metrics']['f1_score']}%")
    print(f"ROC-AUC                 : {report['metrics']['roc_auc']}")
    print(f"False Positive Rate     : {report['metrics']['false_positive_rate_fpr']}%")
    print(f"False Negative Rate     : {report['metrics']['false_negative_rate_fnr']}%")
    print(f"Average Chunk Latency   : {report['metrics']['avg_latency_ms']} ms")
    print(f"Confusion Matrix (TN/FP/FN/TP): {tn}/{fp}/{fn}/{tp}")
    print(f"Report written to: {report_path}")
    print("=" * 60)

if __name__ == "__main__":
    run_evaluation()
