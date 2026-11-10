package com.anilog.omor.dev.ui.reviewcard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.domain.model.CardAspectRatio
import com.anilog.omor.dev.domain.model.CardTemplate
import com.anilog.omor.dev.util.RatingFormatHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CardCustomization(
    val template: CardTemplate = CardTemplate.NEON_VIOLET,
    val aspectRatio: CardAspectRatio = CardAspectRatio.PORTRAIT,
    val customExcerpt: String? = null,
    val showCover: Boolean = true,
    val showRating: Boolean = true,
    val showStars: Boolean = true,
    val showReview: Boolean = true,
    val showGenres: Boolean = true,
    val showStatus: Boolean = true,
    val showEpisodes: Boolean = true,
    val showRafaelRank: Boolean = true,
    val rafaelRank: Int? = null,
    val showBranding: Boolean = true,
    val showDate: Boolean = true
)

object ReviewCardRenderer {

    suspend fun renderCard(
        context: Context,
        anime: AnimeEntity,
        customization: CardCustomization
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = customization.aspectRatio.width
        val height = customization.aspectRatio.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val coverBitmap = if (customization.showCover) {
            loadCoverBitmap(anime.coverPath, width / 2, height / 2)
        } else null

        when (customization.template) {
            CardTemplate.NEON_VIOLET -> renderNeonViolet(canvas, width, height, anime, coverBitmap, customization)
            CardTemplate.CELESTIAL -> renderCelestial(canvas, width, height, anime, coverBitmap, customization)
            CardTemplate.PREMIUM_COLLECTOR -> renderPremiumCollector(canvas, width, height, anime, coverBitmap, customization)
            CardTemplate.ELEMENTAL -> renderElemental(canvas, width, height, anime, coverBitmap, customization)
            CardTemplate.COLLECTIBLE_CARD -> renderCollectibleCard(canvas, width, height, anime, coverBitmap, customization)
            CardTemplate.MINIMAL_PREMIUM -> renderMinimalPremium(canvas, width, height, anime, coverBitmap, customization)
            CardTemplate.DARK_CINEMATIC -> renderDarkCinematic(canvas, width, height, anime, coverBitmap, customization)
            CardTemplate.CUSTOM_REVIEW -> renderCustomReview(canvas, width, height, anime, coverBitmap, customization)
            CardTemplate.RAFAEL_RANKED -> renderRafaelRanked(canvas, width, height, anime, coverBitmap, customization)
        }

        bitmap
    }

    private fun loadCoverBitmap(path: String?, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } catch (_: Exception) {
            null
        }
    }

    // 1. NEON VIOLET
    private fun renderNeonViolet(
        canvas: Canvas, w: Int, h: Int, anime: AnimeEntity, cover: Bitmap?, opt: CardCustomization
    ) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), Color.parseColor("#150E24"), Color.parseColor("#08060E"), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Violet neon border
        val border = RectF(40f, 40f, w - 40f, h - 40f)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#8A2BE2")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(border, 24f, 24f, borderPaint)

        val isLandscape = w > h
        if (isLandscape) {
            renderLandscapeTwoColumn(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#BA68C8"))
        } else {
            renderPortraitStandard(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#BA68C8"))
        }
    }

    // 2. CELESTIAL ANIME
    private fun renderCelestial(
        canvas: Canvas, w: Int, h: Int, anime: AnimeEntity, cover: Bitmap?, opt: CardCustomization
    ) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), Color.parseColor("#0A1128"), Color.parseColor("#000411"), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Cosmic stars
        val starPaint = Paint().apply { color = Color.parseColor("#80FFFFFF"); isAntiAlias = true }
        for (i in 0 until 40) {
            val sx = ((i * 37) % w).toFloat()
            val sy = ((i * 49) % h).toFloat()
            canvas.drawCircle(sx, sy, if (i % 3 == 0) 2.5f else 1.5f, starPaint)
        }

        val border = RectF(40f, 40f, w - 40f, h - 40f)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(border, 24f, 24f, borderPaint)

        val isLandscape = w > h
        if (isLandscape) {
            renderLandscapeTwoColumn(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#00E5FF"))
        } else {
            renderPortraitStandard(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#00E5FF"))
        }
    }

    // 3. PREMIUM COLLECTOR
    private fun renderPremiumCollector(
        canvas: Canvas, w: Int, h: Int, anime: AnimeEntity, cover: Bitmap?, opt: CardCustomization
    ) {
        canvas.drawColor(Color.parseColor("#0E0F12"))

        // Metallic gold double frame
        val outer = RectF(44f, 44f, w - 44f, h - 44f)
        val inner = RectF(56f, 56f, w - 56f, h - 56f)
        val gold = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(outer, 24f, 24f, gold)
        gold.strokeWidth = 1f
        canvas.drawRoundRect(inner, 18f, 18f, gold)

        val isLandscape = w > h
        if (isLandscape) {
            renderLandscapeTwoColumn(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#FFD700"))
        } else {
            renderPortraitStandard(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#FFD700"))
        }
    }

    // 4. ELEMENTAL CARD
    private fun renderElemental(
        canvas: Canvas, w: Int, h: Int, anime: AnimeEntity, cover: Bitmap?, opt: CardCustomization
    ) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), Color.parseColor("#260810"), Color.parseColor("#0D0507"), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        val border = RectF(42f, 42f, w - 42f, h - 42f)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#FF5722")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(border, 24f, 24f, borderPaint)

        val isLandscape = w > h
        if (isLandscape) {
            renderLandscapeTwoColumn(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#FF7043"))
        } else {
            renderPortraitStandard(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#FF7043"))
        }
    }

    // 5. COLLECTIBLE CARD (Collectible card composition, prominent artwork area, stat panels, decorative borders)
    private fun renderCollectibleCard(
        canvas: Canvas, w: Int, h: Int, anime: AnimeEntity, cover: Bitmap?, opt: CardCustomization
    ) {
        canvas.drawColor(Color.parseColor("#181424"))

        // Ornate card frame
        val cardRect = RectF(36f, 36f, w - 36f, h - 36f)
        val framePaint = Paint().apply {
            color = Color.parseColor("#F5C518")
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 28f, 28f, framePaint)

        val isLandscape = w > h
        if (isLandscape) {
            renderLandscapeTwoColumn(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#F5C518"))
        } else {
            renderPortraitStandard(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#F5C518"))
        }
    }

    // 6. MINIMAL PREMIUM
    private fun renderMinimalPremium(
        canvas: Canvas, w: Int, h: Int, anime: AnimeEntity, cover: Bitmap?, opt: CardCustomization
    ) {
        canvas.drawColor(Color.parseColor("#F9F9FB"))

        val border = RectF(44f, 44f, w - 44f, h - 44f)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#DCDAE3")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(border, 24f, 24f, borderPaint)

        val isLandscape = w > h
        if (isLandscape) {
            renderLandscapeTwoColumn(canvas, w, h, anime, cover, opt, isDark = false, accentColor = Color.parseColor("#4A3B80"))
        } else {
            renderPortraitStandard(canvas, w, h, anime, cover, opt, isDark = false, accentColor = Color.parseColor("#4A3B80"))
        }
    }

    // 7. DARK CINEMATIC
    private fun renderDarkCinematic(
        canvas: Canvas, w: Int, h: Int, anime: AnimeEntity, cover: Bitmap?, opt: CardCustomization
    ) {
        if (cover != null) {
            canvas.drawBitmap(cover, null, Rect(0, 0, w, h), Paint().apply { isFilterBitmap = true })
        } else {
            canvas.drawColor(Color.parseColor("#0C0B12"))
        }

        // Cinematic dark scrim
        val scrim = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), Color.parseColor("#CC08060D"), Color.parseColor("#FA08060D"), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), scrim)

        val isLandscape = w > h
        if (isLandscape) {
            renderLandscapeTwoColumn(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#B388FF"))
        } else {
            renderPortraitStandard(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#B388FF"))
        }
    }

    // 8. CUSTOM REVIEW CARD
    private fun renderCustomReview(
        canvas: Canvas, w: Int, h: Int, anime: AnimeEntity, cover: Bitmap?, opt: CardCustomization
    ) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), Color.parseColor("#171520"), Color.parseColor("#0F0E14"), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        val border = RectF(42f, 42f, w - 42f, h - 42f)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#9C88FF")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(border, 24f, 24f, borderPaint)

        val isLandscape = w > h
        if (isLandscape) {
            renderLandscapeTwoColumn(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#9C88FF"))
        } else {
            renderPortraitStandard(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#9C88FF"))
        }
    }

    // 9. RAFAEL RANKED CARD
    private fun renderRafaelRanked(
        canvas: Canvas, w: Int, h: Int, anime: AnimeEntity, cover: Bitmap?, opt: CardCustomization
    ) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), Color.parseColor("#1A122E"), Color.parseColor("#0B0814"), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        val border = RectF(40f, 40f, w - 40f, h - 40f)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#FFB300")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(border, 24f, 24f, borderPaint)

        val isLandscape = w > h
        if (isLandscape) {
            renderLandscapeTwoColumn(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#FFD54F"))
        } else {
            renderPortraitStandard(canvas, w, h, anime, cover, opt, isDark = true, accentColor = Color.parseColor("#FFD54F"))
        }
    }

    // Standard portrait renderer
    private fun renderPortraitStandard(
        canvas: Canvas, w: Int, h: Int, anime: AnimeEntity, cover: Bitmap?, opt: CardCustomization, isDark: Boolean, accentColor: Int
    ) {
        var currentY = 80f

        // Optional Rafael Rank banner at top
        if (opt.showRafaelRank && opt.rafaelRank != null) {
            val badgePaint = TextPaint().apply {
                color = accentColor
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            drawCenteredText(canvas, "★ RAFAEL RANK #${opt.rafaelRank} ★", w, currentY, badgePaint, w - 120)
            currentY += 50f
        }

        // Cover artwork
        val coverW = (w * 0.44f).coerceIn(360f, 480f)
        val coverH = coverW * 1.36f
        val coverX = (w - coverW) / 2f

        if (cover != null && opt.showCover) {
            val coverRect = RectF(coverX, currentY, coverX + coverW, currentY + coverH)
            drawRoundedBitmap(canvas, cover, coverRect, 20f)
            currentY += coverH + 36f
        } else if (opt.showCover) {
            val coverRect = RectF(coverX, currentY, coverX + coverW, currentY + coverH)
            val placeholder = Paint().apply { color = if (isDark) Color.parseColor("#211C33") else Color.parseColor("#E0DDEB") }
            canvas.drawRoundRect(coverRect, 20f, 20f, placeholder)
            currentY += coverH + 36f
        }

        // Rating & Stars
        if (opt.showRating) {
            val ratingStr = RatingFormatHelper.formatRating(anime.personalRating)
            val scoreText = if (anime.personalRating != null) "★ $ratingStr / 10" else "Unrated"
            val ratingPaint = TextPaint().apply {
                color = accentColor
                textSize = 40f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            drawCenteredText(canvas, scoreText, w, currentY, ratingPaint, w - 120)
            currentY += 54f
        }

        // Title
        val titlePaint = TextPaint().apply {
            color = if (isDark) Color.WHITE else Color.parseColor("#1B1926")
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawCenteredText(canvas, anime.title, w, currentY, titlePaint, w - 140)
        currentY += 65f

        // Status / Episodes / Year
        val metaParts = mutableListOf<String>()
        if (opt.showStatus) metaParts.add(anime.status.displayName)
        if (opt.showEpisodes && anime.totalEpisodes > 0) metaParts.add("${anime.watchedEpisodes}/${anime.totalEpisodes} eps")
        if (anime.releaseYear != null) metaParts.add("${anime.releaseYear}")
        if (opt.showGenres && anime.genres.isNotEmpty()) metaParts.add(anime.genres.take(2).joinToString(", "))

        if (metaParts.isNotEmpty()) {
            val metaPaint = TextPaint().apply {
                color = if (isDark) Color.parseColor("#9E98B3") else Color.parseColor("#69657A")
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            drawCenteredText(canvas, metaParts.joinToString(" • "), w, currentY, metaPaint, w - 140)
            currentY += 46f
        }

        // Review excerpt
        val excerpt = opt.customExcerpt ?: anime.review.ifBlank { anime.notes }
        if (opt.showReview && excerpt.isNotBlank()) {
            val quotePaint = TextPaint().apply {
                color = if (isDark) Color.parseColor("#E4E0F0") else Color.parseColor("#343042")
                textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }
            drawCenteredText(canvas, "“${excerpt.take(160)}${if (excerpt.length > 160) "…" else ""}”", w, currentY, quotePaint, w - 180)
        }

        // Footer Branding
        if (opt.showBranding) {
            val footerPaint = TextPaint().apply {
                color = if (isDark) Color.parseColor("#7E7699") else Color.parseColor("#8E8B99")
                textSize = 26f
                isAntiAlias = true
            }
            val dateStr = if (opt.showDate && anime.completionDate != null) {
                " • " + SimpleDateFormat("MMM yyyy", Locale.US).format(Date(anime.completionDate))
            } else ""
            drawCenteredText(canvas, "AniLog • Personal Anime Library$dateStr", w, h - 80f, footerPaint, w - 100)
        }
    }

    // Landscape two-column renderer
    private fun renderLandscapeTwoColumn(
        canvas: Canvas, w: Int, h: Int, anime: AnimeEntity, cover: Bitmap?, opt: CardCustomization, isDark: Boolean, accentColor: Int
    ) {
        val colLeftW = w * 0.40f
        val colRightX = colLeftW + 40f
        val colRightW = w - colRightX - 60f

        // Draw Cover in left column
        val coverW = (colLeftW - 80f).coerceAtMost(h * 0.70f)
        val coverH = coverW * 1.34f
        val coverX = 60f
        val coverY = (h - coverH) / 2f

        if (cover != null && opt.showCover) {
            drawRoundedBitmap(canvas, cover, RectF(coverX, coverY, coverX + coverW, coverY + coverH), 20f)
        }

        var textY = 120f

        if (opt.showRafaelRank && opt.rafaelRank != null) {
            val badgePaint = TextPaint().apply {
                color = accentColor
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            drawLeftAlignedText(canvas, "★ RAFAEL RANK #${opt.rafaelRank} ★", colRightX, textY, badgePaint, colRightW.toInt())
            textY += 44f
        }

        // Rating
        if (opt.showRating) {
            val ratingStr = RatingFormatHelper.formatRating(anime.personalRating)
            val scoreText = if (anime.personalRating != null) "★ $ratingStr / 10" else "Unrated"
            val ratingPaint = TextPaint().apply {
                color = accentColor
                textSize = 36f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            drawLeftAlignedText(canvas, scoreText, colRightX, textY, ratingPaint, colRightW.toInt())
            textY += 50f
        }

        // Title
        val titlePaint = TextPaint().apply {
            color = if (isDark) Color.WHITE else Color.parseColor("#1B1926")
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawLeftAlignedText(canvas, anime.title, colRightX, textY, titlePaint, colRightW.toInt())
        textY += 70f

        // Review
        val excerpt = opt.customExcerpt ?: anime.review.ifBlank { anime.notes }
        if (opt.showReview && excerpt.isNotBlank()) {
            val quotePaint = TextPaint().apply {
                color = if (isDark) Color.parseColor("#E0DCF0") else Color.parseColor("#343042")
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }
            drawLeftAlignedText(canvas, "“${excerpt.take(180)}${if (excerpt.length > 180) "…" else ""}”", colRightX, textY, quotePaint, colRightW.toInt())
        }

        // Footer
        if (opt.showBranding) {
            val footerPaint = TextPaint().apply {
                color = if (isDark) Color.parseColor("#7E7699") else Color.parseColor("#8E8B99")
                textSize = 24f
                isAntiAlias = true
            }
            drawLeftAlignedText(canvas, "AniLog • Personal Anime Library", colRightX, h - 80f, footerPaint, colRightW.toInt())
        }
    }

    private fun drawCenteredText(canvas: Canvas, text: String, canvasWidth: Int, y: Float, paint: TextPaint, maxWidth: Int) {
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, maxWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(4f, 1f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate((canvasWidth - maxWidth) / 2f, y)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawLeftAlignedText(canvas: Canvas, text: String, x: Float, y: Float, paint: TextPaint, maxWidth: Int) {
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, maxWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4f, 1f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(x, y)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawRoundedBitmap(canvas: Canvas, bitmap: Bitmap, dstRect: RectF, radius: Float) {
        val output = Bitmap.createBitmap(dstRect.width().toInt(), dstRect.height().toInt(), Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }
        val rect = RectF(0f, 0f, dstRect.width(), dstRect.height())
        tempCanvas.drawRoundRect(rect, radius, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        tempCanvas.drawBitmap(bitmap, null, Rect(0, 0, dstRect.width().toInt(), dstRect.height().toInt()), paint)

        canvas.drawBitmap(output, null, dstRect, Paint().apply { isFilterBitmap = true })
        output.recycle()
    }

    suspend fun saveCardToCache(context: Context, bitmap: Bitmap): Uri = withContext(Dispatchers.IO) {
        val cardsDir = File(context.cacheDir, "cards").apply { mkdirs() }
        val file = File(cardsDir, "anilog_card_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(context, "com.anilog.omor.dev.fileprovider", file)
    }

    suspend fun saveCardPermanently(context: Context, bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.filesDir, "saved_cards").apply { mkdirs() }
        val file = File(exportDir, "AniLog_Card_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file
    }
}
