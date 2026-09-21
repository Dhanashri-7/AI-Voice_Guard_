"""
VoiceGuard ML Engine - SpectroCNNAttention Model
Lightweight 2D CNN with Squeeze-and-Excitation and Temporal Self-Attention
Optimized for edge inference and real-time audio chunk verification.
"""

import torch
import torch.nn as nn
import torch.nn.functional as F

class SEBlock(nn.Module):
    """Squeeze-and-Excitation Channel Attention."""
    def __init__(self, channels: int, reduction: int = 8):
        super().__init__()
        self.fc = nn.Sequential(
            nn.AdaptiveAvgPool2d(1),
            nn.Flatten(),
            nn.Linear(channels, channels // reduction, bias=False),
            nn.ReLU(inplace=True),
            nn.Linear(channels // reduction, channels, bias=False),
            nn.Sigmoid()
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        b, c, _, _ = x.shape
        w = self.fc(x).view(b, c, 1, 1)
        return x * w

class SpectroCNNAttention(nn.Module):
    def __init__(self, n_mels: int = 80, num_classes: int = 1):
        super().__init__()
        # Conv Block 1
        self.conv1 = nn.Conv2d(1, 32, kernel_size=(5, 5), stride=(1, 1), padding=(2, 2))
        self.bn1 = nn.BatchNorm2d(32)
        self.pool1 = nn.MaxPool2d((2, 2))
        self.se1 = SEBlock(32)

        # Conv Block 2
        self.conv2 = nn.Conv2d(32, 64, kernel_size=(3, 3), stride=(1, 1), padding=(1, 1))
        self.bn2 = nn.BatchNorm2d(64)
        self.pool2 = nn.MaxPool2d((2, 2))
        self.se2 = SEBlock(64)

        # Conv Block 3
        self.conv3 = nn.Conv2d(64, 128, kernel_size=(3, 3), stride=(1, 1), padding=(1, 1))
        self.bn3 = nn.BatchNorm2d(128)
        self.pool3 = nn.MaxPool2d((2, 2))
        self.se3 = SEBlock(128)

        # Conv Block 4 (high-frequency artifact capture)
        self.conv4 = nn.Conv2d(128, 128, kernel_size=(3, 3), stride=(1, 1), padding=(1, 1))
        self.bn4 = nn.BatchNorm2d(128)
        self.se4 = SEBlock(128)

        # Temporal dimension projection
        freq_dim = n_mels // 8
        self.proj = nn.Linear(128 * freq_dim, 128)

        # Temporal Multi-head Attention
        self.attn = nn.MultiheadAttention(embed_dim=128, num_heads=4, batch_first=True)

        # Classifier Head
        self.fc = nn.Sequential(
            nn.Linear(128, 64),
            nn.ReLU(inplace=True),
            nn.Dropout(0.3),
            nn.Linear(64, num_classes)
        )

        # Hook storage for Grad-CAM
        self.gradients = None
        self.activations = None

    def activations_hook(self, grad):
        self.gradients = grad

    def forward(self, x: torch.Tensor, extract_features: bool = False):
        # x: [Batch, 1, n_mels, Time]
        x = F.relu(self.bn1(self.conv1(x)))
        x = self.pool1(x)
        x = self.se1(x)

        x = F.relu(self.bn2(self.conv2(x)))
        x = self.pool2(x)
        x = self.se2(x)

        x = F.relu(self.bn3(self.conv3(x)))
        x = self.pool3(x)
        x = self.se3(x)

        x = F.relu(self.bn4(self.conv4(x)))
        x = self.se4(x)

        if x.requires_grad:
            x.register_hook(self.activations_hook)
        self.activations = x

        # Reshape to [Batch, Time, Channels * Freq]
        b, c, f, t = x.shape
        x_reshaped = x.permute(0, 3, 1, 2).contiguous().view(b, t, c * f)
        temporal_emb = self.proj(x_reshaped) # [b, t, 128]

        # Temporal Self-Attention
        attn_out, _ = self.attn(temporal_emb, temporal_emb, temporal_emb)
        pooled = torch.mean(attn_out, dim=1) # Global average pooling over time [b, 128]

        logits = self.fc(pooled)
        probs = torch.sigmoid(logits)

        if extract_features:
            return probs, logits, pooled
        return probs

    def get_activations_gradient(self):
        return self.gradients

    def get_activations(self):
        return self.activations
