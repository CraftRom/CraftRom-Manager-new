package com.craftrom.manager.ui.fragment.jitter


import android.os.*
import android.view.*
import android.view.Window.OnFrameMetricsAvailableListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.craftrom.manager.R
import com.craftrom.manager.core.utils.interfaces.ToolbarTitleProvider
import com.craftrom.manager.core.utils.jitter.AnimatedBackgroundDrawable
import com.craftrom.manager.core.utils.jitter.PointGraphView
import com.craftrom.manager.databinding.FragmentJitterBinding
import kotlin.math.abs

class JitterFragment : Fragment(), ToolbarTitleProvider {

    private lateinit var jitterReport: TextView
    private lateinit var uiFrameTimeReport: TextView
    private lateinit var renderThreadTimeReport: TextView
    private lateinit var totalFrameTimeReport: TextView
    private lateinit var mostlyTotalFrameTimeReport: TextView
    private lateinit var graph: PointGraphView
    private lateinit var binding: FragmentJitterBinding
    private lateinit var frameMetricsHandler: Handler
    private val thread = HandlerThread("frameMetricsListener")

    private val updateHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val obj = msg.obj
            when (msg.what) {
                R.id.jitter_mma -> jitterReport.text = obj as CharSequence
                R.id.totalish_mma -> mostlyTotalFrameTimeReport.text = obj as CharSequence
                R.id.ui_frametime_mma -> uiFrameTimeReport.text = obj as CharSequence
                R.id.rt_frametime_mma -> renderThreadTimeReport.text = obj as CharSequence
                R.id.total_mma -> totalFrameTimeReport.text = obj as CharSequence
                R.id.graph -> {
                    val jitter = msg.arg1
                    val jitterMma = msg.arg2
                    if (jitter >= 0 && jitterMma >= 0) {
                        graph.addJitterSample(jitter, jitterMma)
                    } else {
                        graph.addJitterSample(0, 0)
                    }
                }
            }
        }
    }

    private val frameMetricsListener = object : OnFrameMetricsAvailableListener {
        private val WEIGHT = 40.0
        private var previousFrameTotal: Long = 0
        private var jitterMma = 0.0
        private var uiFrameTimeMma = 0.0
        private var rtFrameTimeMma = 0.0
        private var totalFrameTimeMma = 0.0
        private var mostlyTotalFrameTimeMma = 0.0
        private var needsFirstValues = true

        override fun onFrameMetricsAvailable(
            window: Window,
            frameMetrics: FrameMetrics,
            dropCountSinceLastInvocation: Int,
        ) {
            if (frameMetrics.getMetric(FrameMetrics.FIRST_DRAW_FRAME) == 1L) {
                return
            }
            val uiDuration = (frameMetrics.getMetric(FrameMetrics.INPUT_HANDLING_DURATION)
                    + frameMetrics.getMetric(FrameMetrics.ANIMATION_DURATION)
                    + frameMetrics.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION)
                    + frameMetrics.getMetric(FrameMetrics.DRAW_DURATION))
            val rtDuration = (frameMetrics.getMetric(FrameMetrics.SYNC_DURATION)
                    + frameMetrics.getMetric(FrameMetrics.COMMAND_ISSUE_DURATION))
            val totalDuration = frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION)
            val jitter = abs(totalDuration - previousFrameTotal)
            if (needsFirstValues) {
                jitterMma = 0.0
                uiFrameTimeMma = uiDuration.toDouble()
                rtFrameTimeMma = rtDuration.toDouble()
                totalFrameTimeMma = totalDuration.toDouble()
                mostlyTotalFrameTimeMma = (uiDuration + rtDuration).toDouble()
                needsFirstValues = false
            } else {
                jitterMma = add(jitterMma, jitter.toDouble())
                uiFrameTimeMma = add(uiFrameTimeMma, uiDuration.toDouble())
                rtFrameTimeMma = add(rtFrameTimeMma, rtDuration.toDouble())
                totalFrameTimeMma = add(totalFrameTimeMma, totalDuration.toDouble())
                mostlyTotalFrameTimeMma =
                    add(mostlyTotalFrameTimeMma, (uiDuration + rtDuration).toDouble())
            }
            previousFrameTotal = totalDuration

            updateHandler.obtainMessage(
                R.id.jitter_mma,
                "Jitter: %.3fms".format(toMs(jitterMma))
            ).sendToTarget()
            updateHandler.obtainMessage(
                R.id.totalish_mma,
                context?.getString(R.string.totalish_mma, toMs(mostlyTotalFrameTimeMma))
            ).sendToTarget()
            updateHandler.obtainMessage(
                R.id.ui_frametime_mma,
                context?.getString(R.string.ui_frametime_mma, toMs(uiFrameTimeMma))
            ).sendToTarget()
            updateHandler.obtainMessage(
                R.id.rt_frametime_mma,
                context?.getString(R.string.rt_frametime_mma, toMs(rtFrameTimeMma))
            ).sendToTarget()
            updateHandler.obtainMessage(
                R.id.total_mma,
                context?.getString(R.string.total_mma, toMs(totalFrameTimeMma))
            ).sendToTarget()
            updateHandler.obtainMessage(
                R.id.graph, (jitter / 1000).toInt(),
                (jitterMma / 1000).toInt()
            ).sendToTarget()
        }

        private fun add(previous: Double, today: Double): Double {
            return ((WEIGHT - 1) * previous + today) / WEIGHT
        }

        private fun toMs(value: Double): Double {
            return value / 1000000
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentJitterBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val content = root.findViewById<View>(R.id.content)

        content.background = AnimatedBackgroundDrawable()
        content.keepScreenOn = true
        thread.start()
        frameMetricsHandler = Handler(thread.looper)
        jitterReport = root.findViewById(R.id.jitter_mma)
        mostlyTotalFrameTimeReport = root.findViewById(R.id.totalish_mma)
        uiFrameTimeReport = root.findViewById(R.id.ui_frametime_mma)
        renderThreadTimeReport = root.findViewById(R.id.rt_frametime_mma)
        totalFrameTimeReport = root.findViewById(R.id.total_mma)
        graph = root.findViewById(R.id.graph)
        jitterReport.text = "abcdefghijklmnopqrstuvwxyz"
        mostlyTotalFrameTimeReport.text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        uiFrameTimeReport.text = "012345689"
        renderThreadTimeReport.text = ",.!()[]{};"
        requireActivity().window.addOnFrameMetricsAvailableListener(frameMetricsListener, frameMetricsHandler)
        return root
    }

    override fun getTitle(): String {
        return getString(R.string.title_jitter)
    }

    override fun getSubtitle(): String {
        return getString(R.string.subtitle_jitter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        thread.quit()
    }
}


