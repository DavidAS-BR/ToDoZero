package todo.zero

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tornadofx.*

class ToDoZeroLoginApp : App(ToDoZeroLogin::class, Styles::class)

class Styles : Stylesheet() {
    companion object {
        val itemRoot by cssclass()
        val deleteTodoIcon by cssclass()
        val strikeThrough by cssclass()
        val addIcon by cssclass()
        val sessionInfoIcon by cssclass()
        val signOutIcon by cssclass()
        val appTitle by cssclass()
        val checkCompletedTodoIcon by cssclass()
        val checkUncompletedTodoIcon by cssclass()

        fun deleteTodoIcon() = FontAwesomeIconView(FontAwesomeIcon.TRASH_ALT).apply {
            glyphSize = 25
            addClass(deleteTodoIcon)
        }

        fun addIcon() = FontAwesomeIconView(FontAwesomeIcon.PENCIL_SQUARE_ALT).apply {
            glyphSize = 50
            addClass(addIcon)
        }

        fun sessionInfoIcon() = FontAwesomeIconView(FontAwesomeIcon.VCARD_ALT).apply {
            glyphSize = 50
            addClass(sessionInfoIcon)
        }

        fun signOutIcon() = FontAwesomeIconView(FontAwesomeIcon.SIGN_OUT).apply {
            glyphSize = 50
            addClass(signOutIcon)
        }

        fun checkCompletedTodoIcon() = FontAwesomeIconView(FontAwesomeIcon.CHECK_SQUARE).apply {
            glyphSize = 24
            addClass(checkCompletedTodoIcon)
        }

        fun checkUncompletedTodoIcon() = FontAwesomeIconView(FontAwesomeIcon.SQUARE_ALT).apply {
            glyphSize = 25
            addClass(checkUncompletedTodoIcon)
        }
    }

    init {
        label {

        }

        appTitle {
            //padding = box(10.px)
            fontSize = 40.px
            //fill = c("#f0f0f0")
            alignment = Pos.CENTER
            fontWeight = FontWeight.BOLD
        }

        signOutIcon {
            borderColor += box(Color.TRANSPARENT)
            borderWidth += box(0.px)
            backgroundColor += Color.TRANSPARENT
            backgroundRadius += box(0.px)


            fill = c("#ff4d4d")

            and(hover) {
                fill = c("#ff1a1a")
            }
        }

        addIcon {
            borderColor += box(Color.TRANSPARENT)
            borderWidth += box(0.px)
            backgroundColor += Color.TRANSPARENT
            backgroundRadius += box(0.px)


            fill = c("#38ba5b")

            and(hover) {
                fill = c("#125c26")
            }
        }

        sessionInfoIcon {
            borderColor += box(Color.TRANSPARENT)
            borderWidth += box(0.px)
            backgroundColor += Color.TRANSPARENT
            backgroundRadius += box(0.px)


            fill = c("#c9c032")

            and(hover) {
                fill = c("#ada51a")
            }
        }

        deleteTodoIcon {
            fill = c("#cc9a9a")

            and(hover) {
                fill = c("#af5b5e")
            }
        }

        strikeThrough {
            text {
                strikethrough = true
            }
        }

        itemRoot {
            padding = box(8.px)
            button {
                backgroundColor += c("transparent")
                padding = box(-2.px)
            }
            alignment = Pos.CENTER_LEFT
        }

    }
}

fun main(args: Array<String>) {

    Application.launch(ToDoZeroLoginApp::class.java, *args)
}