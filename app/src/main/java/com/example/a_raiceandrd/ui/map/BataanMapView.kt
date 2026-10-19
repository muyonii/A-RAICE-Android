package com.example.a_raiceandrd.ui.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.repository.api.MapFarmDto
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

class BataanMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class GeoPoint(val lng: Double, val lat: Double)

    data class MunicipalityFeature(
        val slug: String,
        val name: String,
        val rawCoordinates: List<GeoPoint>
    )

    private val features = mutableListOf<MunicipalityFeature>()
    private val projectedPolygons = mutableListOf<List<PointF>>()
    private val polygonPaths = mutableListOf<Path>()
    private val polygonCentroids = mutableListOf<PointF>()

    // Severity mapping slug -> severity string ("low", "medium", "high", or null)
    private val severityMap = mutableMapOf<String, String?>()

    // Farms to display on the map
    private val farms = mutableListOf<MapFarmDto>()
    private val projectedFarmPoints = mutableListOf<Pair<PointF, MapFarmDto>>()

    // Farmer location
    private var farmerFarmPoint: PointF? = null

    // Selection and highlight
    var selectedSlug: String? = null
        private set
    var highlightedSlug: String? = null
        private set

    var isFarmerMode: Boolean = false

    var onMunicipalitySelected: ((slug: String?) -> Unit)? = null
    var onFarmSelected: ((farm: MapFarmDto) -> Unit)? = null

    // Bounding box in projected coordinates
    private var minProjX = Double.MAX_VALUE
    private var maxProjX = -Double.MAX_VALUE
    private var minProjY = Double.MAX_VALUE
    private var maxProjY = -Double.MAX_VALUE
    private var cosLat = 1.0

    // Touch & zoom
    private var scaleFactor = 1.0f
    private var panX = 0f
    private var panY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false

    // Paints
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.2f)
        color = Color.parseColor("#475569")
    }
    private val highlightStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(3f)
        color = ContextCompat.getColor(context, R.color.gold_bright)
    }
    private val farmerHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2.2f)
        color = ContextCompat.getColor(context, R.color.ok_green)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = sp(9.5f)
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        setShadowLayer(dp(2f), 0f, 0f, Color.BLACK)
    }
    private val farmDotFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.gold)
    }
    private val farmDotStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
        color = Color.WHITE
    }

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(1.0f, 4.0f)
            invalidate()
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            handleTap(e.x, e.y)
            return true
        }
    })

    init {
        contentDescription = context.getString(R.string.DMap)
        loadGeoJson()
    }

    private fun loadGeoJson() {
        try {
            val inputStream = context.assets.open("bataan_municipalities.geojson")
            val jsonString = InputStreamReader(inputStream).readText()
            inputStream.close()

            val root = JSONObject(jsonString)
            val featuresArray = root.getJSONArray("features")

            var totalLat = 0.0
            var coordCount = 0

            var minLat = Double.MAX_VALUE
            var maxLat = -Double.MAX_VALUE
            var minLng = Double.MAX_VALUE
            var maxLng = -Double.MAX_VALUE

            for (i in 0 until featuresArray.length()) {
                val fObj = featuresArray.getJSONObject(i)
                val props = fObj.optJSONObject("properties")
                val slug = props?.optString("municipality").orEmpty().lowercase(Locale.ROOT)
                val name = slug.replaceFirstChar { it.uppercase() }

                val geom = fObj.getJSONObject("geometry")
                val type = geom.getString("type")
                val coords = geom.getJSONArray("coordinates")

                val points = mutableListOf<GeoPoint>()
                if (type == "Polygon") {
                    val ring = coords.getJSONArray(0)
                    for (j in 0 until ring.length()) {
                        val pt = ring.getJSONArray(j)
                        val lng = pt.getDouble(0)
                        val lat = pt.getDouble(1)
                        points.add(GeoPoint(lng, lat))

                        minLat = min(minLat, lat)
                        maxLat = max(maxLat, lat)
                        minLng = min(minLng, lng)
                        maxLng = max(maxLng, lng)

                        totalLat += lat
                        coordCount++
                    }
                }
                features.add(MunicipalityFeature(slug, name, points))
            }

            val meanLat = if (coordCount > 0) totalLat / coordCount else 14.67
            cosLat = cos(Math.toRadians(meanLat))

            minProjX = minLng * cosLat
            maxProjX = maxLng * cosLat
            minProjY = minLat
            maxProjY = maxLat

        } catch (e: Exception) {
            Log.e("BataanMapView", "Failed to parse GeoJSON", e)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        recomputePaths()
    }

    private fun recomputePaths() {
        if (width <= 0 || height <= 0 || features.isEmpty()) return

        val padding = dp(14f)
        val availW = width - 2 * padding
        val availH = height - 2 * padding

        val projW = maxProjX - minProjX
        val projH = maxProjY - minProjY
        if (projW <= 0 || projH <= 0) return

        val scale = min(availW / projW, availH / projH)
        val offX = padding + (availW - projW * scale) / 2f
        val offY = padding + (availH - projH * scale) / 2f

        polygonPaths.clear()
        projectedPolygons.clear()
        polygonCentroids.clear()

        for (feature in features) {
            val path = Path()
            val polyPoints = mutableListOf<PointF>()
            var cx = 0f
            var cy = 0f

            for (k in feature.rawCoordinates.indices) {
                val pt = feature.rawCoordinates[k]
                val px = (offX + (pt.lng * cosLat - minProjX) * scale).toFloat()
                val py = (offY + (maxProjY - pt.lat) * scale).toFloat()
                polyPoints.add(PointF(px, py))

                if (k == 0) {
                    path.moveTo(px, py)
                } else {
                    path.lineTo(px, py)
                }
                cx += px
                cy += py
            }
            path.close()
            polygonPaths.add(path)
            projectedPolygons.add(polyPoints)

            val count = max(1, polyPoints.size)
            polygonCentroids.add(PointF(cx / count, cy / count))
        }

        // Recompute farm dots
        projectedFarmPoints.clear()
        for (f in farms) {
            val lat = f.lat
            val lng = f.lng
            if (lat != null && lng != null) {
                val px = (offX + (lng * cosLat - minProjX) * scale).toFloat()
                val py = (offY + (maxProjY - lat) * scale).toFloat()
                projectedFarmPoints.add(PointF(px, py) to f)
            }
        }
    }

    fun setSeverities(severities: Map<String, String?>) {
        severityMap.clear()
        severityMap.putAll(severities)
        invalidate()
    }

    fun setSelectedMunicipality(slug: String?) {
        selectedSlug = slug
        contentDescription = if (slug != null) {
            "$slug selected on Bataan disease map"
        } else {
            context.getString(R.string.DMap)
        }
        invalidate()
    }

    fun setHighlightedMunicipality(slug: String?) {
        highlightedSlug = slug
        invalidate()
    }

    fun setFarmerLocation(lat: Double?, lng: Double?) {
        if (lat != null && lng != null && width > 0 && height > 0) {
            val padding = dp(14f)
            val availW = width - 2 * padding
            val availH = height - 2 * padding
            val scale = min(availW / (maxProjX - minProjX), availH / (maxProjY - minProjY))
            val offX = padding + (availW - (maxProjX - minProjX) * scale) / 2f
            val offY = padding + (availH - (maxProjY - minProjY) * scale) / 2f

            val px = (offX + (lng * cosLat - minProjX) * scale).toFloat()
            val py = (offY + (maxProjY - lat) * scale).toFloat()
            farmerFarmPoint = PointF(px, py)
        } else {
            farmerFarmPoint = null
        }
        invalidate()
    }

    fun setFarms(farmList: List<MapFarmDto>) {
        farms.clear()
        farms.addAll(farmList)
        recomputePaths()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (polygonPaths.isEmpty()) return

        canvas.save()
        canvas.translate(panX, panY)
        canvas.scale(scaleFactor, scaleFactor, width / 2f, height / 2f)

        // 1. Draw polygons
        for (i in features.indices) {
            val feature = features[i]
            val path = polygonPaths[i]
            val severity = severityMap[feature.slug]?.lowercase(Locale.ROOT)
            val isSelected = feature.slug.equals(selectedSlug, ignoreCase = true)
            val isFarmerHome = feature.slug.equals(highlightedSlug, ignoreCase = true)

            fillPaint.color = getFillColor(severity, isSelected)
            canvas.drawPath(path, fillPaint)

            if (isSelected) {
                canvas.drawPath(path, highlightStrokePaint)
            } else if (isFarmerHome) {
                canvas.drawPath(path, farmerHighlightPaint)
            } else {
                canvas.drawPath(path, strokePaint)
            }

            // Draw label
            val centroid = polygonCentroids[i]
            canvas.drawText(feature.name, centroid.x, centroid.y + dp(3.5f), textPaint)
        }

        // 2. Draw admin farm dots
        val dotRadius = dp(5f)
        for ((pt, farm) in projectedFarmPoints) {
            farmDotFillPaint.color = if (farm.scan?.disease != null) {
                ContextCompat.getColor(context, R.color.rust)
            } else {
                ContextCompat.getColor(context, R.color.ok_green)
            }
            canvas.drawCircle(pt.x, pt.y, dotRadius, farmDotFillPaint)
            canvas.drawCircle(pt.x, pt.y, dotRadius, farmDotStrokePaint)
        }

        // 3. Draw farmer's own farm dot
        farmerFarmPoint?.let { pt ->
            farmDotFillPaint.color = ContextCompat.getColor(context, R.color.gold_bright)
            canvas.drawCircle(pt.x, pt.y, dp(6.5f), farmDotFillPaint)
            canvas.drawCircle(pt.x, pt.y, dp(6.5f), farmDotStrokePaint)
            canvas.drawCircle(pt.x, pt.y, dp(2f), strokePaint)
        }

        canvas.restore()
    }

    private fun getFillColor(severity: String?, isSelected: Boolean): Int {
        val alpha = if (isSelected) 0xCC else 0x77
        val baseColor = if (isFarmerMode) {
            when (severity) {
                "high", "medium" -> ContextCompat.getColor(context, R.color.rust)
                "low"            -> ContextCompat.getColor(context, R.color.gold)
                else             -> ContextCompat.getColor(context, R.color.ok_green)
            }
        } else {
            when (severity) {
                "high"   -> ContextCompat.getColor(context, R.color.rust)
                "medium" -> ContextCompat.getColor(context, R.color.gold)
                "low"    -> ContextCompat.getColor(context, R.color.ok_green)
                else     -> ContextCompat.getColor(context, R.color.surface_chip)
            }
        }

        return Color.argb(
            alpha,
            Color.red(baseColor),
            Color.green(baseColor),
            Color.blue(baseColor)
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = false
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && event.pointerCount == 1) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    if (Math.abs(dx) > 6 || Math.abs(dy) > 6) {
                        isDragging = true
                    }
                    if (scaleFactor > 1.05f) {
                        panX += dx
                        panY += dy
                        val maxPanX = (width * (scaleFactor - 1)) / 2f
                        val maxPanY = (height * (scaleFactor - 1)) / 2f
                        panX = panX.coerceIn(-maxPanX, maxPanX)
                        panY = panY.coerceIn(-maxPanY, maxPanY)
                        invalidate()
                    }
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                // When one finger lifts, reset reference point to remaining pointer
                // so the next ACTION_MOVE doesn't jump
                val remainingIndex = if (event.actionIndex == 0) 1 else 0
                if (remainingIndex < event.pointerCount) {
                    lastTouchX = event.getX(remainingIndex)
                    lastTouchY = event.getY(remainingIndex)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun handleTap(rawX: Float, rawY: Float) {
        // Inverse transform to map coordinate
        val cx = width / 2f
        val cy = height / 2f
        val mapX = (rawX - panX - cx) / scaleFactor + cx
        val mapY = (rawY - panY - cy) / scaleFactor + cy

        // 1. Check if a farm dot was tapped
        val touchRadius = dp(18f)
        for ((pt, farm) in projectedFarmPoints) {
            val dist = Math.hypot((pt.x - mapX).toDouble(), (pt.y - mapY).toDouble()).toFloat()
            if (dist <= touchRadius) {
                onFarmSelected?.invoke(farm)
                return
            }
        }

        // 2. Check if a municipality polygon was tapped
        for (i in projectedPolygons.indices) {
            val poly = projectedPolygons[i]
            if (isPointInPolygon(mapX, mapY, poly)) {
                val tappedSlug = features[i].slug
                val wasSelected = selectedSlug == tappedSlug
                selectedSlug = if (wasSelected) null else tappedSlug
                contentDescription = if (selectedSlug != null) {
                    "${features[i].name} selected on Bataan disease map"
                } else {
                    context.getString(R.string.DMap)
                }
                invalidate()
                // Fire callback with null on deselect so callers can show all
                onMunicipalitySelected?.invoke(selectedSlug)
                performClick()
                return
            }
        }
    }

    private fun isPointInPolygon(px: Float, py: Float, poly: List<PointF>): Boolean {
        var inside = false
        var j = poly.size - 1
        for (i in poly.indices) {
            val pi = poly[i]
            val pj = poly[j]
            if ((pi.y > py) != (pj.y > py) &&
                px < (pj.x - pi.x) * (py - pi.y) / (pj.y - pi.y) + pi.x
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun sp(v: Float): Float {
        return android.util.TypedValue.applyDimension(
            android.util.TypedValue.COMPLEX_UNIT_SP, v, resources.displayMetrics
        )
    }
}
