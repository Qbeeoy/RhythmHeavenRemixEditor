package io.github.chrislo27.rhre3.credits

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.discord.DiscordHelper
import io.github.chrislo27.rhre3.discord.PresenceState
import io.github.chrislo27.rhre3.git.GitHelper
import io.github.chrislo27.rhre3.screen.HidesVersionText
import io.github.chrislo27.rhre3.util.TempoUtils
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul
import rhmodding.bccadeditor.bccad.Animation
import rhmodding.bccadeditor.bccad.BCCAD
import rhmodding.bccadeditor.bccad.Sprite
import kotlin.math.roundToInt


class CreditsGame(main: RHRE3Application) : ToolboksScreen<RHRE3Application, CreditsGame>(main), HidesVersionText {

    companion object {
        private const val TEMPO = 175f
        private const val DURATION = 223f
        private const val LAST_SHAKE = 210
        private const val OFFSET = -512f
    }

    private inner class DanceState(val durationFrames: Int, val dancers: Animation, val vocalist: Animation, val lead: Animation)
    private class TimedDanceState(val startFrame: Int, val danceState: DanceState, val linger: Int = 0)

    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 240 / 9f * 16f, 240f)
    }
    private val music: Music by lazy { Gdx.audio.newMusic(GitHelper.SOUNDS_DIR.child("etc/jumpinjazzsfx.ogg")) }
    private val bccad: BCCAD = BCCAD(Gdx.files.internal("credits/frog.bccad"))
    private val sheet: Texture by lazy { AssetRegistry.get<Texture>("credits_frog") }
    private val bgTex: Texture by lazy { AssetRegistry.get<Texture>("credits_bg") }
    private val frameBuffer: FrameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, camera.viewportWidth.roundToInt(), camera.viewportHeight.roundToInt(), false)

    private val checkeredRegion: TextureRegion by lazy { TextureRegion(bgTex, 2, 1, 499, 365) }
    private val stageRegion: TextureRegion by lazy { TextureRegion(bgTex, 504, 0, 289, 176) }
    private val gradientRegion: TextureRegion by lazy { TextureRegion(bgTex, 801, 1, 23, 366) }
    private val twoLightsRegion: TextureRegion by lazy { TextureRegion(bgTex, 833, 242, 164, 230) }
    private val fourLightsRegion: TextureRegion by lazy { TextureRegion(bgTex, 537, 306, 231, 157) }

    private val dancersReady: Animation = bccad.animations.first { it.name == "D_ready" }
    private val dancersBeat: Animation = bccad.animations.first { it.name == "D_beat" }
    private val dancersDanceL: Animation = bccad.animations.first { it.name == "D_danceL" }
    private val dancersDanceR: Animation = bccad.animations.first { it.name == "D_danceR" }
    private val dancersSlowL: Animation = bccad.animations.first { it.name == "D_slowL" }
    private val dancersSlowR: Animation = bccad.animations.first { it.name == "D_slowR" }
    private val dancersChargeL: Animation = bccad.animations.first { it.name == "D_chargeL" }
    private val dancersChargeR: Animation = bccad.animations.first { it.name == "D_chargeR" }
    private val dancersTurnL: Animation = bccad.animations.first { it.name == "D_turnL" }
    private val dancersTurnR: Animation = bccad.animations.first { it.name == "D_turnR" }
    private val dancersFace: Animation = bccad.animations.first { it.name == "D_face" }
    private val dancersSing0: Animation = bccad.animations.first { it.name == "D_face_sing00" }
    private val dancersSing1: Animation = bccad.animations.first { it.name == "D_face_sing01" }

    private val vocalistReady: Animation = bccad.animations.first { it.name == "V_ready" }
    private val vocalistBeat: Animation = bccad.animations.first { it.name == "V_beat" }
    private val vocalistDanceL: Animation = bccad.animations.first { it.name == "V_danceL" }
    private val vocalistDanceR: Animation = bccad.animations.first { it.name == "V_danceR" }
    private val vocalistSlowL: Animation = bccad.animations.first { it.name == "V_slowL" }
    private val vocalistSlowR: Animation = bccad.animations.first { it.name == "V_slowR" }
    private val vocalistChargeL: Animation = bccad.animations.first { it.name == "V_chargeL" }
    private val vocalistChargeR: Animation = bccad.animations.first { it.name == "V_chargeR" }
    private val vocalistTurnL: Animation = bccad.animations.first { it.name == "V_turnL" }
    private val vocalistTurnR: Animation = bccad.animations.first { it.name == "V_turnR" }
    private val vocalistFace: Animation = bccad.animations.first { it.name == "V_face" }
    private val vocalistSing0: Animation = bccad.animations.first { it.name == "V_face_sing00" }
    private val vocalistSing1: Animation = bccad.animations.first { it.name == "V_face_sing01" }

    private val leadReady: Animation = bccad.animations.first { it.name == "L_ready" }
    private val leadBeat: Animation = bccad.animations.first { it.name == "L_beat" }
    private val leadDanceL: Animation = bccad.animations.first { it.name == "L_danceL" }
    private val leadDanceR: Animation = bccad.animations.first { it.name == "L_danceR" }
    private val leadSlowL: Animation = bccad.animations.first { it.name == "L_slowL" }
    private val leadSlowR: Animation = bccad.animations.first { it.name == "L_slowR" }
    private val leadChargeL: Animation = bccad.animations.first { it.name == "L_chargeL" }
    private val leadChargeR: Animation = bccad.animations.first { it.name == "L_chargeR" }
    private val leadTurnL: Animation = bccad.animations.first { it.name == "L_turnL" }
    private val leadTurnR: Animation = bccad.animations.first { it.name == "L_turnR" }
    private val leadFace: Animation = bccad.animations.first { it.name == "L_face" }
    private val leadSing0: Animation = bccad.animations.first { it.name == "L_face_sing00" }
    private val leadSing1: Animation = bccad.animations.first { it.name == "L_face_sing01" }

    private val microphone: Animation = bccad.animations.first { it.name == "mike" }
    private val textBox: Sprite = bccad.sprites[221]

    private val beatBeats: List<Int> = listOf(1, 3, 5, 7, 9, 11, 12, 13, 14, 15)
    private val countInBeats: List<Int> = listOf(12, 13, 14, 15)
    private val yahooBeats: List<Int> = listOf(21, 29, 37, 45, 69, 77, 85, 93, 121, 133, 137, 141, 145, 157, 173, 189, 211, 215, 219)
    private val yyyBeats: List<Int> = listOf(51, 99, 164, 199, 203)
    private val spinItBeats: List<Int> = listOf(60, 108, 148, 180)
    private val danceBeats: List<Int> = listOf(16..LAST_SHAKE).flatten().let {
        val list = it.toMutableList()

        yahooBeats.forEach {
            list.remove(it)
            list.remove(it + 1)
            list.remove(it + 2)
            list.remove(it + 3)
        }
        yyyBeats.forEach {
            list.remove(it)
            list.remove(it + 1)
            list.remove(it + 2)
            list.remove(it + 3)
        }
        spinItBeats.forEach {
            list.remove(it)
            list.remove(it + 1)
            list.remove(it + 2)
            list.remove(it + 3)
        }

        list
    }
    private val dancerDanceBeats: List<Int> = listOf(16..LAST_SHAKE).flatten().let {
        val list = it.toMutableList()

        yahooBeats.forEach {
            list.remove(it + 2)
            list.remove(it + 3)
        }
        yyyBeats.forEach {
            list.remove(it + 2)
            list.remove(it + 3)
        }
        spinItBeats.forEach {
            list.remove(it + 2)
            list.remove(it + 3)
        }

        list
    }
    private val lastBeatBeats: List<Int> = listOf(DURATION.roundToInt() + 2)

    private val D_READY = DanceState(1, dancersReady, vocalistReady, leadReady)
    private val D_BEAT = DanceState(28, dancersBeat, vocalistBeat, leadBeat)
    private val D_DANCE_L = DanceState(25, dancersDanceL, vocalistDanceL, leadDanceL)
    private val D_DANCE_R = DanceState(25, dancersDanceR, vocalistDanceR, leadDanceR)
    private val D_SLOW_L = DanceState(38, dancersSlowL, vocalistSlowL, leadSlowL)
    private val D_SLOW_R = DanceState(38, dancersSlowR, vocalistSlowR, leadSlowR)
    private val D_CHARGE_L = DanceState(34, dancersChargeL, vocalistChargeL, leadChargeL)
    private val D_CHARGE_R = DanceState(34, dancersChargeR, vocalistChargeR, leadChargeR)
    private val D_TURN_L = DanceState(91, dancersTurnL, vocalistTurnL, leadTurnL)
    private val D_TURN_R = DanceState(91, dancersTurnR, vocalistTurnR, leadTurnR)
    private val DV_TURN_L = DanceState(47, dancersTurnL, vocalistTurnL, leadTurnL)
    private val DV_TURN_R = DanceState(47, dancersTurnR, vocalistTurnR, leadTurnR)
    private val D_FACE = DanceState(1, dancersFace, vocalistFace, leadFace)
    private val D_SING_0 = DanceState(28, dancersSing0, vocalistSing0, leadSing0)
    private val D_SING_1 = DanceState(28, dancersSing1, vocalistSing1, leadSing1)

    private val creditsText = Credits.list.drop(1).joinToString(separator = "") {
        "[#${Color(Color.YELLOW).fromHsv((Credits.list.indexOf(it) - 1f) / Credits.list.size * 360f, 0.75f, 1f)}]${it.text}[]\n${it.persons}\n\n"
    } + Localization["licenseInfo"]
    private var creditsTextHeight: Float = -1f

    private var seconds: Float = -0.509f
    private val beat: Float
        get() = TempoUtils.secondsToBeats(seconds, TEMPO)
    private var lastBeat: Float = beat
    private var isLeftBeat = true
    private var isLeftBeatDancers = true
    private var lightingOnFront = true
    private var kururin = false

    private var dancersState = TimedDanceState(0, D_READY)
    private var vocalistState = TimedDanceState(0, D_READY)
    private var leadState = TimedDanceState(0, D_READY)
    private var dancersFaceState = TimedDanceState(0, D_FACE)
    private var vocalistFaceState = TimedDanceState(0, D_FACE)
    private var leadFaceState = TimedDanceState(0, D_FACE)
    private var currentFrame: Int = 0

    private var frameUsedSax: Int = 0

    override val hidesVersionText: Boolean
        get() = beat >= 6f
    private var skipFrame = true

    override fun render(delta: Float) {
        super.render(delta)

        val batch = main.batch
        val oldProjMatrix = batch.projectionMatrix
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.setColor(1f, 1f, 1f, 1f)

        val stageX: Float = if (beat < DURATION - 7f) Interpolation.exp5.apply(((beat - 6) / 2f).coerceIn(0f, 1f)) * -70f else (1f - Interpolation.exp5.apply(((beat - (DURATION - 7f)) / 2f).coerceIn(0f, 1f))) * -70f

        // Stage
        batch.draw(gradientRegion, 0f, 0f, camera.viewportWidth, camera.viewportHeight)
        batch.draw(checkeredRegion, camera.viewportWidth / 2 - checkeredRegion.regionWidth / 2, 0f)
        batch.draw(stageRegion, camera.viewportWidth / 2 - stageRegion.regionWidth / 2 + stageX, -10f)

        fun freshInList(list: List<Int>, beatOffset: Float = 0f): Boolean = (lastBeat - beatOffset).toInt() < (beat - beatOffset).toInt() && ((beat - beatOffset).toInt()) in list

        // Determine animations
        // Bouncing to beat
        if (freshInList(beatBeats)) {
            val newDanceState = TimedDanceState(currentFrame, D_BEAT)
            dancersState = newDanceState
            vocalistState = newDanceState
            leadState = newDanceState

            // Count in
            if (freshInList(countInBeats)) {
                leadFaceState = TimedDanceState(currentFrame, D_SING_1)
            }
        }
        // Normal dancing
        var didDanceBeat = false
        var didDanceBeatDancers = false
        if (freshInList(danceBeats)) {
            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeat) D_DANCE_L else D_DANCE_R)
            vocalistState = newDanceState
            leadState = newDanceState
            didDanceBeat = true
            lightingOnFront = true
        }
        if (freshInList(dancerDanceBeats)) {
            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeatDancers) D_DANCE_L else D_DANCE_R)
            dancersState = newDanceState
            didDanceBeatDancers = true
        }
        // Yahoo part 1
        if (freshInList(yahooBeats)) {
            val newFaceState = TimedDanceState(currentFrame, D_SING_1)
            leadFaceState = newFaceState

            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeat) D_DANCE_L else D_DANCE_R)
            vocalistState = newDanceState
            leadState = newDanceState
            didDanceBeat = true
            lightingOnFront = true
        } else if (freshInList(yahooBeats, beatOffset = 2f)) {
            val newFaceState = TimedDanceState(currentFrame, D_SING_1)
            dancersFaceState = newFaceState

            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeatDancers) D_DANCE_L else D_DANCE_R)
            dancersState = newDanceState
            didDanceBeatDancers = true
            lightingOnFront = false
        }
        // Yahoo part 2
        if (freshInList(yahooBeats, 0.5f)) {
            val newFaceState = TimedDanceState(currentFrame + 30, D_SING_0)
            leadFaceState = newFaceState

            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeat) D_SLOW_L else D_SLOW_R)
            vocalistState = newDanceState
            leadState = newDanceState
            didDanceBeat = true
            lightingOnFront = true
        } else if (freshInList(yahooBeats, beatOffset = 2.5f)) {
            val newFaceState = TimedDanceState(currentFrame + 30, D_SING_0)
            dancersFaceState = newFaceState

            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeatDancers) D_SLOW_L else D_SLOW_R)
            dancersState = newDanceState
            didDanceBeatDancers = true
            lightingOnFront = false
        }
        if (freshInList(yyyBeats) || freshInList(yyyBeats, 0.5f) || freshInList(yyyBeats, 1.0f)) { // yeah yeah yeah
            val newFaceState = TimedDanceState(currentFrame + 20, D_SING_0)
            leadFaceState = newFaceState

            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeat) D_DANCE_L else D_DANCE_R)
            vocalistState = newDanceState
            leadState = newDanceState
            didDanceBeat = true
            lightingOnFront = true
        } else if (freshInList(yyyBeats, 2f) || freshInList(yyyBeats, 2.5f) || freshInList(yyyBeats, 3f)) { // dancers
            val newFaceState = TimedDanceState(currentFrame + 20, D_SING_0)
            dancersFaceState = newFaceState

            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeatDancers) D_DANCE_L else D_DANCE_R)
            dancersState = newDanceState
            didDanceBeatDancers = true
            lightingOnFront = false
        }
        if (freshInList(spinItBeats)) { // spin it,
            val newFaceState = TimedDanceState(currentFrame + 5, D_SING_0)
            leadFaceState = newFaceState

            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeat) D_CHARGE_L else D_CHARGE_R)
            vocalistState = newDanceState
            leadState = newDanceState
            didDanceBeat = true
            lightingOnFront = true
            kururin = true
        } else if (freshInList(spinItBeats, 2f)) { // dancers
            val newFaceState = TimedDanceState(currentFrame + 5, D_SING_0)
            dancersFaceState = newFaceState

            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeatDancers) D_CHARGE_L else D_CHARGE_R)
            dancersState = newDanceState
            didDanceBeatDancers = true
            lightingOnFront = false
            kururin = false
        }
        if (freshInList(spinItBeats, 1f)) { // boys
            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeat) D_TURN_L else D_TURN_R)
            vocalistState = TimedDanceState(currentFrame, if (isLeftBeat) DV_TURN_L else DV_TURN_R, 45)
            leadState = newDanceState
            didDanceBeat = true
            lightingOnFront = true
            kururin = true
        } else if (freshInList(spinItBeats, 3f)) { // dancers
            val newDanceState = TimedDanceState(currentFrame, if (isLeftBeatDancers) D_TURN_L else D_TURN_R)
            dancersState = newDanceState
            didDanceBeatDancers = true
            lightingOnFront = false
            kururin = false
        }
        if (freshInList(lastBeatBeats)) {
            leadFaceState = TimedDanceState(currentFrame, D_SING_1, 30)
        }
        if (didDanceBeat) {
            isLeftBeat = !isLeftBeat
        }
        if (didDanceBeatDancers) {
            isLeftBeatDancers = !isLeftBeatDancers
        }

        // Dancers
        repeat(4) { i ->
            val x = camera.viewportWidth / 2 + 48 * (i - 1.5f)
            val y = 115f
            val currentAnimation: Animation = dancersState.danceState.dancers
            val frame = (currentFrame - dancersState.startFrame).coerceIn(0, dancersState.danceState.durationFrames - 1)
            val currentStep = currentAnimation.render(batch, sheet, bccad.sprites, frame, x + OFFSET + stageX, y + OFFSET)
            if (currentStep != null && dancersState.danceState != D_TURN_L && dancersState.danceState != D_TURN_R) {
                val sprite = bccad.sprites[currentStep.spriteNum.toInt()]
                val part = sprite.parts.last()
                dancersFaceState.danceState.dancers.render(batch, sheet, bccad.sprites, (currentFrame - dancersFaceState.startFrame).coerceIn(0, dancersFaceState.danceState.durationFrames), x + OFFSET + part.w / 2f + (part.relX + OFFSET) + stageX, y + OFFSET - (part.relY + OFFSET) - (part.h / 2f))
            }
        }

        if (!lightingOnFront) {
            // Set up blending for lights
            batch.setColor(0f, 0f, 0f, 0.5f)
            batch.fillRect(0f, 0f, camera.viewportWidth, camera.viewportHeight)
            batch.setColor(1f, 1f, 1f, 1f)
            batch.flush()
            batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA)
            val lightsRegion = fourLightsRegion
            batch.draw(lightsRegion, camera.viewportWidth / 2 - lightsRegion.regionWidth / 2 + stageX, camera.viewportHeight - lightsRegion.regionHeight + 28f)
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        }

        fun frontArea() {
            run {
                val x = camera.viewportWidth / 2 - 40f
                val y = 48f
                val currentAnimation = vocalistState.danceState.vocalist
                val frame = (currentFrame - vocalistState.startFrame).coerceIn(0, vocalistState.danceState.durationFrames - 1)
                val currentStep = currentAnimation.render(batch, sheet, bccad.sprites, frame, OFFSET + x + stageX, OFFSET + y)
                if (currentStep != null && vocalistState.danceState != DV_TURN_L && vocalistState.danceState != DV_TURN_R) {
                    val sprite = bccad.sprites[currentStep.spriteNum.toInt()]
                    val part = sprite.parts.last()
                    vocalistFaceState.danceState.vocalist.render(batch, sheet, bccad.sprites, (currentFrame - vocalistFaceState.startFrame).coerceIn(0, vocalistFaceState.danceState.durationFrames), x + OFFSET + part.w / 2f + (part.relX + OFFSET) + stageX, y + OFFSET - (part.relY + OFFSET) - (part.h / 2f))
                }
            }
            run {
                val x = camera.viewportWidth / 2 + 40f
                val y = 48f
                val currentAnimation = vocalistState.danceState.lead
                val frame = (currentFrame - leadState.startFrame).coerceIn(0, leadState.danceState.durationFrames - 1)
                val currentStep = currentAnimation.render(batch, sheet, bccad.sprites, frame, OFFSET + x + stageX, OFFSET + y)
                if (currentStep != null && leadState.danceState != D_TURN_L && leadState.danceState != D_TURN_R) {
                    val sprite = bccad.sprites[currentStep.spriteNum.toInt()]
                    val part = sprite.parts.last()
                    leadFaceState.danceState.lead.render(batch, sheet, bccad.sprites, (currentFrame - leadFaceState.startFrame).coerceIn(0, leadFaceState.danceState.durationFrames), x + OFFSET + part.w / 2f + (part.relX + OFFSET) + stageX, y + OFFSET - (part.relY + OFFSET) - (part.h / 2f))
                }
            }

            microphone.render(batch, sheet, bccad.sprites, 0, OFFSET + camera.viewportWidth / 2 - 32f + stageX, OFFSET + 32f)
        }

        frontArea()

        batch.end()
        frameBuffer.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE)
        frontArea()
        batch.flush()
        batch.end()
        frameBuffer.end()
        batch.begin()
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        if (lightingOnFront) {
            // Set up blending for lights
            batch.setColor(0f, 0f, 0f, 0.5f)
            batch.fillRect(0f, 0f, camera.viewportWidth, camera.viewportHeight)
            batch.setColor(1f, 1f, 1f, 1f)
            batch.flush()
            batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA)
            val lightsRegion = twoLightsRegion
            batch.draw(lightsRegion, camera.viewportWidth / 2 - lightsRegion.regionWidth / 2 + stageX, camera.viewportHeight - lightsRegion.regionHeight + 28f)
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        } else {
            batch.setColor(0f, 0f, 0f, 0.5f)
            batch.draw(frameBuffer.colorBufferTexture, 0f, 0f, frameBuffer.width.toFloat(), frameBuffer.height.toFloat(), 0, 0, frameBuffer.width, frameBuffer.height, false, true)
            batch.setColor(1f, 1f, 1f, 1f)
        }

        val font = main.defaultFont
        font.scaleFont()
        font.setColor(0f, 0f, 0f, 1f)

        // kururin
        if (kururin || beat > DURATION + 2f) {
            val newFont = main.defaultFontLarge
            newFont.scaleFont()
            newFont.setColor(0f, 0f, 0f, 1f)
            val x = camera.viewportWidth / 2 + stageX
            val y = 48f
            textBox.render(batch, sheet, x + OFFSET, y + OFFSET)

            newFont.scaleMul(0.5f)
            newFont.drawCompressed(batch, if (kururin) "くるりん！" else "Thank you!\n[ESC]", x - 100f, y + newFont.capHeight / 2 + (if (kururin) 0f else newFont.capHeight / 2), 200f, Align.center)
            newFont.scaleMul(1 / 0.5f)

            newFont.setColor(1f, 1f, 1f, 1f)
            newFont.unscaleFont()
        }

        // Credits text
        run {
            val font = main.defaultBorderedFont
            font.setColor(1f, 1f, 1f, 1f)
            font.scaleFont()

            font.scaleMul(0.75f)

            val targetWidth = camera.viewportWidth * 0.25f
            val x = camera.viewportWidth * 0.7f
            val y = MathUtils.lerp(0f, (creditsTextHeight + (camera.viewportHeight - font.capHeight)), (beat - 7f) / (DURATION - 15f))

            if (creditsTextHeight < 0) {
                creditsTextHeight = font.draw(batch, creditsText, x, 0f, targetWidth, Align.left, true).height
            }

            val logo = AssetRegistry.get<Texture>("logo_512")
            batch.setColor(1f, 1f, 1f, ((y + 1f) / 2f).coerceIn(0f, 1f))
            batch.draw(logo, x, (y - 64f).coerceAtLeast(0f) + camera.viewportHeight * 0.35f, targetWidth, targetWidth)
            batch.setColor(1f, 1f, 1f, 1f)

            font.draw(batch, creditsText, x, y - font.capHeight, targetWidth, Align.left, true)

            // controls
            font.setColor(1f, 1f, 1f, (if (frameUsedSax > 0) (1f - ((currentFrame - frameUsedSax - 30) / 30f)) else (if (beat > DURATION - 5f) (1f - (beat - (DURATION - 5f))) else 1f)).coerceIn(0f, 1f))
            font.draw(batch, Localization["credits.saxophone"], 2f, font.lineHeight)
            font.setColor(1f, 1f, 1f, 1f)

            font.scaleMul(1 / 0.75f)

            font.unscaleFont()
        }

        if (beat >= DURATION) {
            lightingOnFront = true
        }

        font.setColor(1f, 1f, 1f, 1f)
        font.unscaleFont()

        batch.end()

        batch.projectionMatrix = main.defaultCamera.combined

        currentFrame++
        if (currentFrame - dancersState.startFrame >= dancersState.danceState.durationFrames + dancersState.linger) {
            dancersState = TimedDanceState(currentFrame, D_READY)
        }
        if (currentFrame - vocalistState.startFrame >= vocalistState.danceState.durationFrames + vocalistState.linger) {
            vocalistState = TimedDanceState(currentFrame, D_READY)
        }
        if (currentFrame - leadState.startFrame >= leadState.danceState.durationFrames + leadState.linger) {
            leadState = TimedDanceState(currentFrame, D_READY)
        }
        if (currentFrame - dancersFaceState.startFrame >= dancersFaceState.danceState.durationFrames + dancersFaceState.linger) {
            dancersFaceState = TimedDanceState(currentFrame, D_FACE)
        }
        if (currentFrame - vocalistFaceState.startFrame >= vocalistFaceState.danceState.durationFrames + vocalistFaceState.linger) {
            vocalistFaceState = TimedDanceState(currentFrame, D_FACE)
        }
        if (currentFrame - leadFaceState.startFrame >= leadFaceState.danceState.durationFrames + leadFaceState.linger) {
            leadFaceState = TimedDanceState(currentFrame, D_FACE)
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (skipFrame) {
            skipFrame = false
        } else {
            lastBeat = beat
            seconds += Gdx.graphics.deltaTime
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            main.screen = ScreenRegistry["info"]
        }

        if (Toolboks.debugMode) {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                seconds -= Gdx.graphics.deltaTime * 10
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                seconds += Gdx.graphics.deltaTime * 10
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            vocalistFaceState = TimedDanceState(currentFrame, if (MathUtils.randomBoolean()) D_SING_1 else D_SING_0)
            if (frameUsedSax <= 0)
                frameUsedSax = currentFrame
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            if (currentFrame - vocalistFaceState.startFrame > 12) {
                vocalistFaceState = TimedDanceState(currentFrame, if (MathUtils.randomBoolean()) D_SING_1 else D_SING_0)
            }
        }

    }

    override fun show() {
        super.show()
        DiscordHelper.updatePresence(PresenceState.ViewingCredits)
        sheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        bgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        music.play()
        skipFrame = true
    }

    override fun hide() {
        super.hide()
        music.stop()
        frameBuffer.dispose()
    }

    private fun BitmapFont.scaleFont() {
        this.setUseIntegerPositions(false)
        this.data.setScale(camera.viewportWidth / main.defaultCamera.viewportWidth,
                           camera.viewportHeight / main.defaultCamera.viewportHeight)
    }

    private fun BitmapFont.unscaleFont() {
        this.setUseIntegerPositions(true)
        this.data.setScale(1f)
    }

    override fun getDebugString(): String? {
        return "seconds: $seconds\nbeat: $beat"
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
        frameBuffer.dispose()
    }
}