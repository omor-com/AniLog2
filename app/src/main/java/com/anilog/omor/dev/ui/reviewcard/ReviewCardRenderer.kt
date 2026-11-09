package com.anilog.omor.dev.ui.reviewcard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
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
import com.anilog.omor.dev.domain.model.CardTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReviewCardRenderer {

    suspend fun renderCard(
        context: Context,
        anime: AnimeEntity,
        customExcerpt: String?,
        template: CardTemplate,
        includeGenre: Boolean = true,
        includeDate: Boolean = true
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Load cover bitmap if present
        val coverBitmap = loadCoverBitmap(anime.coverPath, 800, 1000)

        when (template) {
            CardTemplate.PREMIUM_DARK -> renderPremiumDark(canvas, width, height, anime, coverBitmap, customExcerpt, includeGenre, includeDate)
            CardTemplate.MINIMAL_LIGHT -> renderMinimalLight(canvas, width, height, anime, coverBitmap, customExcerpt, includeGenre, includeDate)
            CardTemplate.CINEMATIC -> renderCinematic(canvas, width, height, anime, coverBitmap, customExcerpt, includeGenre, includeDate)
            CardTemplate.COMPACT_REVIEW -> renderCompact(canvas, width, height, anime, coverBitmap, customExcerpt, includeGenre, includeDate)
            CardTemplate.TOP_RATED -> renderTopRated(canvas, width, height, anime, coverBitmap, customExcerpt, includeGenre, includeDate)
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

    private fun renderPremiumDark(
        canvas: Canvas,
        w: Int,
        h: Int,
        anime: AnimeEntity,
        cover: Bitmap?,
        customExcerpt: String?,
        includeGenre: Boolean,
        includeDate: Boolean
    ) {
        // Deep Charcoal Background #100E17 with subtle violet gradient at top
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), Color.parseColor("#171324"), Color.parseColor("#0C0A10"), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Subtle glowing violet border card
        val cardMargin = 54f
        val cardRect = RectF(cardMargin, cardMargin, w - cardMargin, h - cardMargin)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#342852")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, borderPaint)

        // Draw cover image centered upper half
        val coverW = 480f
        val coverH = 640f
        val coverX = (w - coverW) / 2f
        val coverY = 120f
        val coverRect = RectF(coverX, coverY, coverX + coverW, coverY + coverH)

        if (cover != null) {
            drawRoundedBitmap(canvas, cover, coverRect, 24f)
        } else {
            val placeholderPaint = Paint().apply {
                color = Color.parseColor("#211A33")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(coverRect, 24f, 24f, placeholderPaint)
        }

        // Draw 10-star rating badge and stars
        val ratingY = coverY + coverH + 50f
        drawRatingSection(canvas, w, ratingY, anime.personalRating, isDark = true)

        // Title
        val titleY = ratingY + 90f
        val titlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 52f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        drawCenteredText(canvas, anime.title, w, titleY, titlePaint, w - 160)

        // Review text excerpt
        val excerpt = customExcerpt ?: anime.review.ifBlank { anime.notes }
        if (excerpt.isNotBlank()) {
            val quoteY = titleY + 80f
            val quotePaint = TextPaint().apply {
                color = Color.parseColor("#D0CBE0")
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }
            drawCenteredText(canvas, "“${excerpt.take(160)}${if (excerpt.length > 160) "..." else ""}”", w, quoteY, quotePaint, w - 200)
        }

        // Footer Branding: ordinary interface text
        val footerY = h - 90f
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#8E82A8")
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val dateStr = if (includeDate && anime.completionDate != null) {
            " • Finished " + SimpleDateFormat("MMM yyyy", Locale.US).format(Date(anime.completionDate))
        } else ""
        drawCenteredText(canvas, "AniLog • Personal Anime Journal$dateStr", w, footerY, footerPaint, w - 100)
    }

    private fun renderMinimalLight(
        canvas: Canvas,
        w: Int,
        h: Int,
        anime: AnimeEntity,
        cover: Bitmap?,
        customExcerpt: String?,
        includeGenre: Boolean,
        includeDate: Boolean
    ) {
        // Crisp White Background
        canvas.drawColor(Color.parseColor("#FAFAFC"))

        // Subtle thin dark border
        val cardMargin = 50f
        val cardRect = RectF(cardMargin, cardMargin, w - cardMargin, h - cardMargin)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E4E2EB")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 28f, 28f, borderPaint)

        // Cover image
        val coverW = 460f
        val coverH = 620f
        val coverX = (w - coverW) / 2f
        val coverY = 130f
        val coverRect = RectF(coverX, coverY, coverX + coverW, coverY + coverH)

        if (cover != null) {
            drawRoundedBitmap(canvas, cover, coverRect, 20f)
        } else {
            val placeholderPaint = Paint().apply { color = Color.parseColor("#EDEBF2") }
            canvas.drawRoundRect(coverRect, 20f, 20f, placeholderPaint)
        }

        // Rating
        val ratingY = coverY + coverH + 50f
        drawRatingSection(canvas, w, ratingY, anime.personalRating, isDark = false)

        // Title
        val titleY = ratingY + 86f
        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#181622")
            textSize = 50f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawCenteredText(canvas, anime.title, w, titleY, titlePaint, w - 160)

        // Excerpt
        val excerpt = customExcerpt ?: anime.review.ifBlank { anime.notes }
        if (excerpt.isNotBlank()) {
            val quoteY = titleY + 80f
            val quotePaint = TextPaint().apply {
                color = Color.parseColor("#4A4658")
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            drawCenteredText(canvas, "“${excerpt.take(160)}${if (excerpt.length > 160) "..." else ""}”", w, quoteY, quotePaint, w - 200)
        }

        // Footer
        val footerY = h - 90f
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#7A768A")
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        drawCenteredText(canvas, "AniLog • Personal Anime Journal", w, footerY, footerPaint, w - 100)
    }

    private fun renderCinematic(
        canvas: Canvas,
        w: Int,
        h: Int,
        anime: AnimeEntity,
        cover: Bitmap?,
        customExcerpt: String?,
        includeGenre: Boolean,
        includeDate: Boolean
    ) {
        // Dimmed cover background stretched with cinematic dark gradient scrim
        if (cover != null) {
            val bgPaint = Paint().apply { isFilterBitmap = true }
            canvas.drawBitmap(cover, null, Rect(0, 0, w, h), bgPaint)
        } else {
            canvas.drawColor(Color.parseColor("#12111A"))
        }

        // Dark gradient overlay
        val scrim = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(Color.parseColor("#B308070D"), Color.parseColor("#E608070D"), Color.parseColor("#FA08070D")),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), scrim)

        // Foreground featured card
        val coverW = 440f
        val coverH = 600f
        val coverX = (w - coverW) / 2f
        val coverY = 140f
        val coverRect = RectF(coverX, coverY, coverX + coverW, coverY + coverH)

        if (cover != null) {
            drawRoundedBitmap(canvas, cover, coverRect, 20f)
            // Accent border
            val p = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
                color = Color.parseColor("#80FFFFFF")
                isAntiAlias = true
            }
            canvas.drawRoundRect(coverRect, 20f, 20f, p)
        }

        // Rating
        val ratingY = coverY + coverH + 55f
        drawRatingSection(canvas, w, ratingY, anime.personalRating, isDark = true)

        // Title
        val titleY = ratingY + 90f
        val titlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawCenteredText(canvas, anime.title, w, titleY, titlePaint, w - 160)

        // Excerpt
        val excerpt = customExcerpt ?: anime.review.ifBlank { anime.notes }
        if (excerpt.isNotBlank()) {
            val quoteY = titleY + 80f
            val quotePaint = TextPaint().apply {
                color = Color.parseColor("#E6E0F5")
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }
            drawCenteredText(canvas, "“${excerpt.take(150)}${if (excerpt.length > 150) "..." else ""}”", w, quoteY, quotePaint, w - 200)
        }

        // Footer
        val footerY = h - 90f
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#B3A8CC")
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        drawCenteredText(canvas, "AniLog • Cinematic Collection Card", w, footerY, footerPaint, w - 100)
    }

    private fun renderCompact(
        canvas: Canvas,
        w: Int,
        h: Int,
        anime: AnimeEntity,
        cover: Bitmap?,
        customExcerpt: String?,
        includeGenre: Boolean,
        includeDate: Boolean
    ) {
        // Dark compact theme
        canvas.drawColor(Color.parseColor("#14131E"))

        val cardRect = RectF(60f, 160f, w - 60f, h - 160f)
        val cardPaint = Paint().apply {
            color = Color.parseColor("#1F1C2E")
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

        // Cover on top or left
        val coverW = 380f
        val coverH = 520f
        val coverX = (w - coverW) / 2f
        val coverY = cardRect.top + 50f
        val coverRect = RectF(coverX, coverY, coverX + coverW, coverY + coverH)

        if (cover != null) {
            drawRoundedBitmap(canvas, cover, coverRect, 20f)
        }

        val ratingY = coverY + coverH + 50f
        drawRatingSection(canvas, w, ratingY, anime.personalRating, isDark = true)

        val titleY = ratingY + 85f
        val titlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawCenteredText(canvas, anime.title, w, titleY, titlePaint, w - 200)

        val excerpt = customExcerpt ?: anime.review.ifBlank { anime.notes }
        if (excerpt.isNotBlank()) {
            val quoteY = titleY + 75f
            val quotePaint = TextPaint().apply {
                color = Color.parseColor("#CFC9E0")
                textSize = 32f
                isAntiAlias = true
            }
            drawCenteredText(canvas, excerpt.take(140), w, quoteY, quotePaint, w - 220)
        }

        // Footer
        val footerY = cardRect.bottom - 40f
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#877F9E")
            textSize = 26f
            isAntiAlias = true
        }
        drawCenteredText(canvas, "AniLog Journal", w, footerY, footerPaint, w - 160)
    }

    private fun renderTopRated(
        canvas: Canvas,
        w: Int,
        h: Int,
        anime: AnimeEntity,
        cover: Bitmap?,
        customExcerpt: String?,
        includeGenre: Boolean,
        includeDate: Boolean
    ) {
        // Deep Obsidian with Gold / Amber Accents
        canvas.drawColor(Color.parseColor("#0F0E14"))

        // Golden double frame
        val outerBorder = RectF(50f, 50f, w - 50f, h - 50f)
        val innerBorder = RectF(66f, 66f, w - 66f, h - 66f)
        val goldPaint = Paint().apply {
            color = Color.parseColor("#FFC837")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(outerBorder, 30f, 30f, goldPaint)
        goldPaint.strokeWidth = 1.5f
        canvas.drawRoundRect(innerBorder, 22f, 22f, goldPaint)

        // Top Banner Text
        val headerPaint = TextPaint().apply {
            color = Color.parseColor("#FFD97D")
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawCenteredText(canvas, "★ TOP RATED ANIME SELECTION ★", w, 120f, headerPaint, w - 160)

        // Cover
        val coverW = 460f
        val coverH = 620f
        val coverX = (w - coverW) / 2f
        val coverY = 180f
        val coverRect = RectF(coverX, coverY, coverX + coverW, coverY + coverH)

        if (cover != null) {
            drawRoundedBitmap(canvas, cover, coverRect, 22f)
            val coverBorder = Paint().apply {
                color = Color.parseColor("#FFC837")
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            canvas.drawRoundRect(coverRect, 22f, 22f, coverBorder)
        }

        // Score Badge
        val ratingY = coverY + coverH + 60f
        drawRatingSection(canvas, w, ratingY, anime.personalRating, isDark = true, accentGold = true)

        // Title
        val titleY = ratingY + 90f
        val titlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 52f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        drawCenteredText(canvas, anime.title, w, titleY, titlePaint, w - 160)

        // Excerpt
        val excerpt = customExcerpt ?: anime.review.ifBlank { anime.notes }
        if (excerpt.isNotBlank()) {
            val quoteY = titleY + 80f
            val quotePaint = TextPaint().apply {
                color = Color.parseColor("#E6D7AD")
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }
            drawCenteredText(canvas, "“${excerpt.take(150)}${if (excerpt.length > 150) "..." else ""}”", w, quoteY, quotePaint, w - 200)
        }

        // Footer
        val footerY = h - 100f
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#B3A278")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        drawCenteredText(canvas, "AniLog • Hall of Fame Collection", w, footerY, footerPaint, w - 100)
    }

    private fun drawRatingSection(
        canvas: Canvas,
        w: Int,
        y: Float,
        rating: Float?,
        isDark: Boolean,
        accentGold: Boolean = false
    ) {
        val ratingStr = if (rating != null) "★ ${"%.1f".format(rating)} / 10" else "Unrated"
        val paint = TextPaint().apply {
            color = when {
                accentGold -> Color.parseColor("#FFC837")
                isDark -> Color.parseColor("#B39DDB")
                else -> Color.parseColor("#6200EA")
            }
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawCenteredText(canvas, ratingStr, w, y, paint, w - 100)
    }

    private fun drawCenteredText(
        canvas: Canvas,
        text: String,
        canvasWidth: Int,
        y: Float,
        paint: TextPaint,
        maxWidth: Int
    ) {
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

    /**
     * Save generated review card bitmap into app cache and return FileProvider content URI
     */
    suspend fun saveCardToCache(context: Context, bitmap: Bitmap): Uri = withContext(Dispatchers.IO) {
        val cardsDir = File(context.cacheDir, "cards").apply { mkdirs() }
        val file = File(cardsDir, "anilog_card_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(context, "com.anilog.omor.dev.fileprovider", file)
    }

    /**
     * Save card to permanent app storage or downloads
     */
    suspend fun saveCardPermanently(context: Context, bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.filesDir, "saved_cards").apply { mkdirs() }
        val file = File(exportDir, "AniLog_Review_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file
    }
}
