"""
VoiceGuard ML Engine - AASISTLight Model
Lightweight Spectro-Temporal Graph-Attention Architecture for Anti-Spoofing.
Detects neural vocoder phase and spectral boundary discontinuities.
"""

import torch
import torch.nn as nn
import torch.nn.functional as F

class GraphAttentionLayer(nn.Module):
    """Simplified Graph Attention for spectral-temporal node correlations."""
    def __init__(self, in_features: int, out_features: int):
        super().__init__()
        self.fc = nn.Linear(in_features, out_features, bias=False)
        self.attn_src = nn.Parameter(torch.zeros(size=(1, 1, out_features)))
        self.attn_dst = nn.Parameter(torch.zeros(size=(1, 1, out_features)))
        nn.init.xavier_uniform_(self.attn_src.data, gain=1.414)
        nn.init.xavier_uniform_(self.attn_dst.data, gain=1.414)
        self.leaky_relu = nn.LeakyReLU(0.2)

    def forward(self, h: torch.Tensor) -> torch.Tensor:
        # h: [B, N, D]
        wh = self.fc(h) # [B, N, Out]
        attn_src = torch.matmul(wh, self.attn_src.transpose(1, 2)) # [B, N, 1]
        attn_dst = torch.matmul(wh, self.attn_dst.transpose(1, 2)) # [B, N, 1]
        e = self.leaky_relu(attn_src + attn_dst.transpose(1, 2)) # [B, N, N]
        a = F.softmax(e, dim=-1)
        return torch.matmul(a, wh)

class AASISTLight(nn.Module):
    def __init__(self, in_mels: int = 80, num_classes: int = 1):
        super().__init__()
        # Sinc/1D spectral projection
        self.spec_conv = nn.Sequential(
            nn.Conv1d(in_mels, 64, kernel_size=5, padding=2),
            nn.BatchNorm1d(64),
            nn.LeakyReLU(0.2),
            nn.MaxPool1d(2),
            nn.Conv1d(64, 128, kernel_size=3, padding=1),
            nn.BatchNorm1d(128),
            nn.LeakyReLU(0.2),
            nn.MaxPool1d(2)
        )

        # Spectro-temporal GAT
        self.gat1 = GraphAttentionLayer(128, 64)
        self.gat2 = GraphAttentionLayer(64, 64)

        # Output head
        self.classifier = nn.Sequential(
            nn.Linear(64, 32),
            nn.ReLU(),
            nn.Linear(32, num_classes)
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        # Input: [B, 1, Mels, Time] or [B, Mels, Time]
        if x.dim() == 4:
            x = x.squeeze(1)
        feat = self.spec_conv(x) # [B, 128, Time//4]
        nodes = feat.transpose(1, 2) # [B, N, 128]
        g1 = F.elu(self.gat1(nodes))
        g2 = self.gat2(g1)
        pooled = torch.mean(g2, dim=1)
        logits = self.classifier(pooled)
        return torch.sigmoid(logits)
