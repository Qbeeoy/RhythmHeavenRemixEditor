package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.screen.InfoScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.setHSB


class InfoButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                 stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    private val infoScreen: InfoScreen by lazy { ScreenRegistry.getNonNullAsType<InfoScreen>("info") }

    override fun getHoverText(): String {
        return Localization["editor.info"]
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        editor.main.screen = infoScreen
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (labels.isNotEmpty()) {
            val first = labels.first()
            if (first is ImageLabel) {
                if (InfoScreen.shouldSeePartners) {
                    first.tint.setHSB(MathHelper.getSawtoothWave(1.5f), 0.3f, 0.75f)
                } else {
                    first.tint.set(1f, 1f, 1f, 1f)
                }
            }
        }
        super.render(screen, batch, shapeRenderer)
    }
}