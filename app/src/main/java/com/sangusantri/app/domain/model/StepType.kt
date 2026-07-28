package com.sangusantri.app.domain.model

/**
 * Ordered step kinds within an amaliyah version (PRD 10.2). Shared verbatim across
 * the content package DTO, Room entity, and domain layers: the vocabulary has no
 * boundary-specific meaning, so splitting it into three identical enums would be
 * duplication without a boundary reason (CLAUDE.md).
 */
enum class StepType {
    HEADING,
    INSTRUCTION,
    ARABIC_TEXT,
    QURAN_AYAH,
    PRAYER,
    REPEATED_READING,
    DIVIDER,
    CLOSING,
}
