package com.example.glimpse.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

object GlimpseTokens {
    // ── Surfaces ──────────────────────────────────────────────────────────────
    val surfaceDark         = Color(0xFF111113)
    val surfaceRaisedDark   = Color(0xFF1C1C1F)
    val surfaceOverlayDark  = Color(0xFF262629)
    val surfaceLight        = Color(0xFFF7F5F0)
    val surfaceRaisedLight  = Color(0xFFFFFFFF)
    val surfaceOverlayLight = Color(0xFFEDEBE6)

    // ── Text ──────────────────────────────────────────────────────────────────
    val textPrimaryDark     = Color(0xFFF0EDE6)
    val textSecondaryDark   = Color(0xFF8A8880)
    val textTertiaryDark    = Color(0xFF4A4A50)
    val textPrimaryLight    = Color(0xFF14181F)
    val textSecondaryLight  = Color(0xFF6B6860)
    val textTertiaryLight   = Color(0xFFADAB9F)

    // ── Borders ───────────────────────────────────────────────────────────────
    val borderDark          = Color(0xFF2E2E33)
    val borderLight         = Color(0xFFDDD9D0)

    // ── Brand accent ─────────────────────────────────────────────────────────
    val accent              = Color(0xFF0E7C86)
    val accentSoft          = Color(0xFF0D3438)
    val accentSoftLight     = Color(0xFFD4F1F4)
    val onAccent            = Color(0xFFFFFFFF)

    // ── Safety (coral) — SOS only ─────────────────────────────────────────────
    val safety              = Color(0xFFE5484D)
    val safetySoft          = Color(0xFF3A1112)
    val safetySoftLight     = Color(0xFFFFDFDF)

    // ── Status ────────────────────────────────────────────────────────────────
    val statusLive          = Color(0xFF2BB673)
    val statusRecent        = Color(0xFFF59E0B)
    val statusOffline       = Color(0xFF71717A)

    // ── Spacing (4dp grid) ────────────────────────────────────────────────────
    val s4:  Dp = 4.dp
    val s8:  Dp = 8.dp
    val s12: Dp = 12.dp
    val s16: Dp = 16.dp
    val s20: Dp = 20.dp
    val s24: Dp = 24.dp
    val s32: Dp = 32.dp

    // ── Shape radii ───────────────────────────────────────────────────────────
    val radiusSheet:  Dp = 28.dp
    val radiusCard:   Dp = 16.dp
    val radiusTile:   Dp = 10.dp
    val radiusChip:   Dp = 50.dp
    val radiusButton: Dp = 12.dp
}
