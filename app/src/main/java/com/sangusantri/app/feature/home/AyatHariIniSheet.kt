package com.sangusantri.app.feature.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.sangusantri.app.R
import com.sangusantri.app.domain.model.AyatHariIni
import com.sangusantri.app.domain.model.QuranArabicFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Handoff turn 5 §3 — what "Selengkapnya" pays off with: the ayah in full, rendered as the very
 * card that would be shared, and two actions under it.
 *
 * Deliberately no hide or block action and no confirm states: nothing here can lose anything, so
 * nothing needs guarding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyatHariIniSheet(
    ayat: AyatHariIni,
    arabicFont: QuranArabicFont,
    onDismiss: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shareFailedMessage = stringResource(R.string.beranda_ayat_share_failed)
    val copiedMessage = stringResource(R.string.beranda_ayat_copied)
    // The preview on screen is also the capture source, which is what guarantees the picture that
    // gets sent is the one that was approved.
    val cardLayer = rememberGraphicsLayer()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // Opens fully expanded rather than at the half-height default: the sheet is short, and at
        // half height "Salin teks" sat below the fold with nothing to suggest a second action was
        // there — readers took the sheet for share-only.
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) },
        shape = RoundedCornerShape(topStart = SheetCornerRadius, topEnd = SheetCornerRadius),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SheetBottomPadding),
        ) {
            CardPreview(ayat = ayat, arabicFont = arabicFont, cardLayer = cardLayer)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            SheetAction(
                icon = Icons.Outlined.IosShare,
                title = stringResource(R.string.beranda_ayat_share_title),
                supporting = stringResource(R.string.beranda_ayat_share_supporting),
                onClick = {
                    scope.launch {
                        val captured = cardLayer.toImageBitmap()
                        val shared = context.shareCard(captured, ayat)
                        if (shared) onDismiss() else onMessage(shareFailedMessage)
                    }
                },
            )
            SheetAction(
                icon = Icons.Outlined.ContentCopy,
                title = stringResource(R.string.beranda_ayat_copy_title),
                supporting = stringResource(R.string.beranda_ayat_copy_supporting),
                onClick = {
                    context.copyAyatText(ayat)
                    onMessage(copiedMessage)
                    onDismiss()
                },
            )
        }
    }
}

/** The card, recording itself into [cardLayer] as it draws, so the share action has a capture of
 * exactly what is on screen without composing the card a second time. */
@Composable
private fun CardPreview(
    ayat: AyatHariIni,
    arabicFont: QuranArabicFont,
    cardLayer: GraphicsLayer,
) {
    AyatShareCard(
        ayat = ayat,
        arabicFont = arabicFont,
        modifier =
            Modifier
                .width(PreviewCardSize)
                .drawWithContent {
                    cardLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(cardLayer)
                },
    )
    Text(
        text = stringResource(R.string.beranda_ayat_share_card_caption),
        fontSize = CaptionSize,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = CaptionTopPadding, bottom = CaptionBottomPadding),
    )
}

@Composable
private fun SheetAction(
    icon: ImageVector,
    title: String,
    supporting: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ActionIconGap),
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = ActionHorizontalPadding, vertical = ActionVerticalPadding),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(ActionIconSize),
        )
        Column {
            Text(
                text = title,
                fontSize = ActionTitleSize,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = supporting,
                fontSize = ActionSupportingSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = ActionSupportingTopPadding),
            )
        }
    }
}

/** Writes the captured card and hands it to the system share sheet. `false` when the file could not
 * be written, so the caller says so instead of opening a chooser with nothing behind it. */
private suspend fun Context.shareCard(
    captured: ImageBitmap,
    ayat: AyatHariIni,
): Boolean {
    val uri = withContext(Dispatchers.IO) { writeShareImage(captured) } ?: return false
    startActivity(buildShareIntent(uri, ayat))
    return true
}

/**
 * Arabic, blank line, translation, blank line, citation — the same three parts the card carries, so
 * a pasted quote and a shared picture say the same thing. The Kemenag strings go on the clipboard
 * verbatim.
 */
private fun Context.ayatPlainText(ayat: AyatHariIni): String =
    "${ayat.arabicText}\n\n${ayat.translation}\n\n${ayatReference(ayat)}"

private fun Context.copyAyatText(ayat: AyatHariIni) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(
        ClipData.newPlainText(getString(R.string.beranda_ayat_copy_label), ayatPlainText(ayat)),
    )
}

/**
 * The captured card as a 1080x1080 PNG in `cacheDir`, handed out through the app's `FileProvider`.
 *
 * Two conversions before it can be written. The capture may be a hardware bitmap, which `compress`
 * refuses, so it is copied into a software config first; and it is captured at whatever pixel size
 * 296dp works out to on this device, so it is scaled to the fixed export size — the picture people
 * receive should not vary with the sender's screen density.
 *
 * One fixed file name, not a unique one: yesterday's card has no value once a new one is written,
 * and the cache directory is not a gallery. `null` on failure, so the caller can say so rather than
 * open a share sheet with nothing behind it.
 */
private fun Context.writeShareImage(captured: ImageBitmap): Uri? =
    runCatching {
        val software =
            captured.asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false)
                ?: return@runCatching null
        val scaled = Bitmap.createScaledBitmap(software, SHARE_IMAGE_PX, SHARE_IMAGE_PX, true)
        val file = File(cacheDir, SHARE_DIRECTORY).apply { mkdirs() }.resolve(SHARE_FILE_NAME)
        file.outputStream().use { scaled.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
        FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    }.getOrNull()

private fun Context.buildShareIntent(
    uri: Uri,
    ayat: AyatHariIni,
): Intent =
    Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, ayatPlainText(ayat))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        },
        null,
    )

private const val SHARE_DIRECTORY = "shared_images"
private const val SHARE_FILE_NAME = "ayat-hari-ini.png"
private const val PNG_QUALITY = 100

/** The handoff's export size for the card. */
private const val SHARE_IMAGE_PX = 1080

private val SheetCornerRadius = 28.dp
private val SheetBottomPadding = 18.dp
private val PreviewCardSize = 296.dp
private val CaptionSize = 11.sp
private val CaptionTopPadding = 9.dp
private val CaptionBottomPadding = 11.dp
private val ActionIconGap = 14.dp
private val ActionIconSize = 22.dp
private val ActionHorizontalPadding = 20.dp
private val ActionVerticalPadding = 14.dp
private val ActionTitleSize = 15.sp
private val ActionSupportingSize = 12.5.sp
private val ActionSupportingTopPadding = 2.dp
