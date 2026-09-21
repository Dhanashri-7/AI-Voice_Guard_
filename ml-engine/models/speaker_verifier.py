"""
VoiceGuard ML Engine - Speaker Verification & Consistency Subsystem
Extracts d-vector / speaker embeddings from acoustic features and measures
cosine similarity against enrolled voice prints of trusted contacts.
"""

import torch
import torch.nn as nn
import torch.nn.functional as F
import numpy as np

class SpeakerVerifier(nn.Module):
    def __init__(self, in_mels: int = 80, embedding_dim: int = 192):
        super().__init__()
        # 1D Dilated Convolutions (TDNN-style)
        self.layer1 = nn.Sequential(
            nn.Conv1d(in_mels, 128, kernel_size=5, dilation=1, padding=2),
            nn.BatchNorm1d(128),
            nn.ReLU()
        )
        self.layer2 = nn.Sequential(
            nn.Conv1d(128, 128, kernel_size=3, dilation=2, padding=2),
            nn.BatchNorm1d(128),
            nn.ReLU()
        )
        self.layer3 = nn.Sequential(
            nn.Conv1d(128, 256, kernel_size=3, dilation=3, padding=3),
            nn.BatchNorm1d(256),
            nn.ReLU()
        )

        # Statistical Pooling (Mean + Std)
        self.segment1 = nn.Linear(512, 256)
        self.segment2 = nn.Linear(256, embedding_dim)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        # x: [B, 1, Mels, Time] or [B, Mels, Time]
        if x.dim() == 4:
            x = x.squeeze(1)
        out1 = self.layer1(x)
        out2 = self.layer2(out1)
        out3 = self.layer3(out2)

        mean = torch.mean(out3, dim=2)
        std = torch.std(out3, dim=2)
        stat_pool = torch.cat([mean, std], dim=1) # [B, 512]

        h = F.relu(self.segment1(stat_pool))
        emb = self.segment2(h)
        # L2 Normalize embedding onto unit sphere
        return F.normalize(emb, p=2, dim=1)

    @staticmethod
    def compute_similarity(emb1: np.ndarray, emb2: np.ndarray) -> float:
        """
        Computes cosine similarity between two normalized speaker embeddings.
        Returns a score in range [0.0, 1.0].
        """
        emb1 = np.asarray(emb1, dtype=np.float32).flatten()
        emb2 = np.asarray(emb2, dtype=np.float32).flatten()
        dot = np.dot(emb1, emb2)
        norm1 = np.linalg.norm(emb1)
        norm2 = np.linalg.norm(emb2)
        if norm1 == 0 or norm2 == 0:
            return 0.0
        cos_sim = dot / (norm1 * norm2)
        # Rescale [-1, 1] to [0, 1]
        score = float((cos_sim + 1.0) / 2.0)
        return max(0.0, min(1.0, score))
