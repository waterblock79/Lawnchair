/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.deletescape.lawnchair.colors.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import ch.deletescape.lawnchair.ViewPagerAdapter
import ch.deletescape.lawnchair.colors.*
import com.android.launcher3.R
import kotlinx.android.synthetic.main.tabbed_color_picker.view.*
import me.priyesh.chroma.*

@SuppressLint("ViewConstructor")
class TabbedPickerView(context: Context, val key: String, initialColor: Int, val colorMode: ColorMode, val resolvers: Array<String>, private val dismiss: () -> Unit)
    : RelativeLayout(context, null) {

    private val engine = ColorEngine.getInstance(context)
    private val colors = resolvers.mapToResolvers(engine)

    private val isLandscape = orientation(context) == Configuration.ORIENTATION_LANDSCAPE

    private val minItemHeight = context.resources.getDimensionPixelSize(R.dimen.color_preview_height)
    private val minItemWidthLandscape = context.resources.getDimensionPixelSize(R.dimen.color_preview_width)
    private val chromaViewHeight = context.resources.getDimensionPixelSize(R.dimen.chroma_view_height)
    private val viewHeightLandscape = context.resources.getDimensionPixelSize(R.dimen.chroma_dialog_height)
    private var resolver by ColorEngine.getInstance(context).getOrCreateResolver(key)

    val chromaView = ChromaView(initialColor, colorMode, context).apply {
        enableButtonBar(object : ChromaView.ButtonBarListener {
            override fun onNegativeButtonClick() = dismiss()
            override fun onPositiveButtonClick(color: Int) {
                //TODO support HSV and RGBA aswell (create new resolvers for those two)
                val red = Color.red(color)
                val green = Color.green(color)
                val blue = Color.blue(color)
                resolver = RGBColorResolver(
                        ColorEngine.ColorResolver.Config(key, engine, args = listOf("$red", "$green", "$blue")))
                dismiss()
            }
        })
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.tabbed_color_picker, this)
        measure(MeasureSpec.EXACTLY,0)
        viewPager.adapter = ViewPagerAdapter(listOf(
                Pair(context.getString(R.string.color_presets), initPresetList()),
                Pair(context.getString(R.string.color_custom), chromaView)
        ))
        if (!isLandscape) {
            viewPager.layoutParams.height = chromaViewHeight
        } else {
            viewPager.layoutParams.height = viewHeightLandscape
        }
        viewPager.childFilter = { it is ChromaView }
        tabLayout.setupWithViewPager(viewPager)
        val color = engine.accent
        tabLayout.tabRippleColor = ColorStateList.valueOf(color)
        tabLayout.setSelectedTabIndicatorColor(color)
        if (resolver is RGBColorResolver) {
            viewPager.currentItem = 1
        }
    }

    @SuppressLint("InflateParams")
    private fun initPresetList(): View {
        return ExpandFillLinearLayout(context, null).apply {
            orientation = if (isLandscape) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
            childWidth = minItemWidthLandscape
            childHeight = minItemHeight

            colors.forEach {
                val preview = LayoutInflater.from(context).inflate(R.layout.color_preview, null) as ColorPreviewView
                preview.colorResolver = it
                preview.setOnClickListener { _ ->
                    resolver = it
                    dismiss()
                }
                addView(preview)
            }
        }
    }
}
