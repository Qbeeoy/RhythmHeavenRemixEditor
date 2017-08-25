package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.stage.FalseCheckbox
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel


class InfoScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, InfoScreen>(main) {

    companion object {

        const val DEFAULT_AUTOSAVE_TIME = 5
        val timers = listOf(0, 1, 2, 3, 4, 5, 10, 15)

    }

    override val stage: Stage<InfoScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val preferences: Preferences
        get() = main.preferences
    private val editor: Editor
        get() = ScreenRegistry.getNonNullAsType<EditorScreen>("editor").editor

    init {
        stage as GenericStage<InfoScreen>
        val palette = stage.palette

        stage.titleIcon.apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_info"))
        }
        stage.titleLabel.apply {
            this.text = "editor.info"
        }
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("editor")
        }

        stage.bottomStage.elements += object : Button<InfoScreen>(palette, stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                Gdx.net.openURI(RHRE3.GITHUB)
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.textWrapping = false
                this.text = Localization["screen.info.github", RHRE3.GITHUB]
                this.fontScaleMultiplier = 0.9f
            })

            this.location.set(screenX = 0.1875f, screenWidth = 0.625f)
        }
        stage.bottomStage.elements += object : Button<InfoScreen>(palette, stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                main.screen = ScreenRegistry.getNonNull("credits")
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = true
                this.text = "screen.info.credits"
                this.fontScaleMultiplier = 0.9f
            })

            this.stage.updatePositions()
            this.location.set(screenWidth = this.stage.percentageOfWidth(this.stage.location.realHeight) * 2f)
            this.location.set(screenX = 1f - this.location.screenWidth)
        }

        stage.centreStage.also { centre ->
            val padding = 0.05f
            val buttonWidth = 0.4f
            val buttonHeight = 0.15f
            val fontScale = 0.8f

            centre.elements += TextLabel(palette, centre, centre).apply {
                this.location.set(screenX = padding,
                                  screenY = 1f - (padding + buttonHeight * 0.8f),
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight * 0.8f)
                this.isLocalizationKey = true
                this.text = "screen.info.settings"
            }

            // Disable minimap
            centre.elements += object : FalseCheckbox<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_MINIMAP, checked).flush()
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_MINIMAP, false)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.disableMinimap"
                }

                this.location.set(screenX = padding,
                                  screenY = padding,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Chase camera
            centre.elements += object : TrueCheckbox<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_CHASE_CAMERA, checked).flush()
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_CHASE_CAMERA, false)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.chaseCamera"
                }

                this.location.set(screenX = padding,
                                  screenY = padding * 2 + buttonHeight,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Autosave timer
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                private fun updateText() {
                    textLabel.text = Localization["screen.info.autosaveTimer",
                            if (timers[index] == 0) Localization["screen.info.autosaveTimerOff"]
                            else Localization["screen.info.autosaveTimerMin", timers[index]]]
                    editor.resetAutosaveTimer()
                }

                private fun persist() {
                    preferences.putInteger(PreferenceKeys.SETTINGS_AUTOSAVE, timers[index]).flush()
                }

                private var index: Int = run {
                    val default = DEFAULT_AUTOSAVE_TIME
                    val pref = preferences.getInteger(PreferenceKeys.SETTINGS_AUTOSAVE, default)
                    timers.indexOf(timers.find { it == pref } ?: default).coerceIn(0, timers.size - 1)
                }

                private val textLabel: TextLabel<InfoScreen>
                    get() = labels.first() as TextLabel<InfoScreen>

                override fun render(screen: InfoScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                    if (textLabel.text.isEmpty()) {
                        updateText()
                    }
                    super.render(screen, batch, shapeRenderer)
                }

                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    index++
                    if (index >= timers.size)
                        index = 0

                    persist()
                    updateText()
                }

                override fun onRightClick(xPercent: Float, yPercent: Float) {
                    super.onRightClick(xPercent, yPercent)
                    index--
                    if (index < 0)
                        index = timers.size - 1

                    persist()
                    updateText()
                }
            }.apply {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = false
                    this.text = ""
                    this.fontScaleMultiplier = fontScale
                })

                this.location.set(screenX = padding,
                                  screenY = padding * 3 + buttonHeight * 2,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }
        }

        stage.updatePositions()
    }

    override fun renderUpdate() {
        super.renderUpdate()
        stage as GenericStage
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }
}