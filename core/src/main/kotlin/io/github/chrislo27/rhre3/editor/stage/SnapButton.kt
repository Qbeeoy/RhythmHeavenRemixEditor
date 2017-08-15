package io.github.chrislo27.rhre3.editor.stage

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.*


class SnapButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                 stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    companion object {
        val snapLevels = intArrayOf(4, 6, 8, 12, 16, 24, 32)
    }

    private var index: Int = 0
        set(value) {
            field = value
            fractionString = "1/$snapLevel"
        }
    private val snapLevel: Int
        get() = snapLevels[index]
    private val snapFloat: Float
        get() = 1f / snapLevel
    private var fractionString: String = "1/$snapLevel"
    private val label: TextLabel<EditorScreen> = object : TextLabel<EditorScreen>(palette, this, stage) {
        override fun getRealText(): String {
            return fractionString
        }
    }.apply {
        this@SnapButton.addLabel(this)
    }

    private fun updateAndFlash() {
        editor.subbeatSection.setFlash(0.75f)
        editor.snap = snapFloat
    }

    override fun getHoverText(): String {
        return Localization["editor.snap", fractionString]
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        index++
        if (index >= snapLevels.size)
            index = 0
        updateAndFlash()
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        index = 0
        updateAndFlash()
    }
}