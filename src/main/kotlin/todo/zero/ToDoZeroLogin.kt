package todo.zero

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Parent
import javafx.scene.control.ButtonBar
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.scene.text.FontWeight
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import tornadofx.*
import java.util.*
import kotlin.concurrent.thread


class ToDoZeroLogin : View() {
    val model = ViewModel()
    val loginNome = model.bind { SimpleStringProperty() }
    val loginNomeEmpresa = model.bind { SimpleStringProperty() }
    val loginEmail = model.bind { SimpleStringProperty() }
    val controleDeLogin: ControleDeLogin by inject()

    override val root = form {
        title = "Login"

        fieldset("ToDo Zero") {
            label("Login - Funcionario") { style { alignment = Pos.CENTER; fontWeight = FontWeight.BOLD } }

            field("Nome") {
                textfield(loginNome).required()
            }

            field("Nome da empresa") {
                textfield(loginNomeEmpresa).required()
            }

            field("Email") {
                textfield(loginEmail).required()
            }

            style {
                spacing = 5.px
                alignment = Pos.CENTER
            }
        }

        hbox(alignment = Pos.BASELINE_RIGHT) {
            button("Login") {
                enableWhen(model.valid)

                action { runAsyncWithProgress { controleDeLogin.login(loginNome.value, loginNomeEmpresa.value, loginEmail.value) } }
            }

            button("Cadastrar Empresa") {
                action { replaceWith<CadastroEmpresa>(sizeToScene = true) }

            }

            style {
                paddingTop = 10.0
                spacing = 200.px
            }
        }
    }

    override fun onDock() {
        loginNome.value = ""
        loginNomeEmpresa.value = ""
        loginEmail.value = ""
        model.clearDecorators()
    }
}

class CadastroEmpresa : View() {

    var nomeEmpresa = SimpleStringProperty()
    var empresaDonoNome = SimpleStringProperty()
    var empresaDonoEmail = SimpleStringProperty()
    var funcionarioNome = SimpleStringProperty()
    var funcionarioEmail = SimpleStringProperty()
    val nomeEmpresaFuncionarios = mutableListOf<Map<String, String>>()


    override val root = form {
        fieldset("ToDo Zero") {
            label("Cadastro de Empresa") { style { alignment = Pos.CENTER; fontWeight = FontWeight.BOLD } }

            field("Nome da empresa") {
                textfield(nomeEmpresa)
            }

            field("Nome do dono da empresa") {
                textfield(empresaDonoNome)
            }

            field("Email do dono da empresa") {
                textfield(empresaDonoEmail)
            }


            fieldset("Cadastro dos funcionarios") {

                field("Nome do funcionario") {
                    textfield(funcionarioNome)
                }

                field("Email do funcionario") {
                    textfield(funcionarioEmail)
                }

                button("Cadastrar funcionario").action {
                    if (funcionarioNome.value.isNotEmpty() && funcionarioEmail.value.isNotEmpty()) {
                        nomeEmpresaFuncionarios.add(mapOf(funcionarioNome.value to funcionarioEmail.value))

                        information("Funcionario \"${funcionarioNome.value}\" adicionado com sucesso.")

                        funcionarioEmail.value = ""
                        funcionarioNome.value = ""
                    } else {
                        error("Nada para adicionar!")
                    }
                }

                style { spacing = 5.px; alignment = Pos.CENTER }
            }

            style { spacing = 5.px; alignment = Pos.CENTER }
        }

        hbox(alignment = Pos.BASELINE_RIGHT){
            button("Cadastrar").action {

                if (nomeEmpresaFuncionarios.isEmpty() || nomeEmpresa.value.isEmpty()) {
                    error("Preencha os campos!")
                    return@action
                }

                confirmation(
                    header = "Confirmar o cadastro da empresa",
                    content = "Clique OK para confirmar o cadastro da empresa",
                    actionFn = {btnType ->
                        if (btnType.buttonData == ButtonBar.ButtonData.OK_DONE) {
                            runAsyncWithOverlay {
                                //Thread.sleep(5000)
                                // TODO: Cadastrar empresa na database

                                transaction(Database.connect(AppDB.Database.dB)) {
                                    addLogger(StdOutSqlLogger)

                                    SchemaUtils.create(AppDB.empresa, AppDB.funcionario, AppDB.todo)

                                    AppDB.empresa.insert {
                                        it[nome] = nomeEmpresa.value
                                        it[dono_nome] = empresaDonoNome.value
                                        it[dono_email] = empresaDonoEmail.value
                                    }
                                }

                                nomeEmpresaFuncionarios.forEach { map -> map.forEach { (nome, email) ->
                                    //println("$nome é dono do email $email e pertence à empresa ${nomeEmpresa.value}")

                                    transaction(Database.connect(AppDB.Database.dB)) {
                                        addLogger(StdOutSqlLogger)

                                        SchemaUtils.create(AppDB.empresa, AppDB.funcionario, AppDB.todo)

                                        AppDB.funcionario.insert {
                                            it[nome_completo] = nome
                                            it[funcionario_empresa] = nomeEmpresa.value
                                            it[funcionario_email] = email
                                        }
                                    }
                                } }
                            }.setOnSucceeded {
                                information("Empresa cadastrada com sucesso no banco de dados, os funcionarios já podem logar no aplicativo.")
                            }
                        } else {
                            nomeEmpresa.value = ""
                            nomeEmpresaFuncionarios.clear()
                        }
                    },
                    title = "Cofirmação de Cadastro"
                )
            }

            button("Voltar") { action { replaceWith<ToDoZeroLogin>(sizeToScene = true) } }

            style { spacing = 320.px }
        }
    }

    override fun onDock() {
        nomeEmpresa.value = ""
        empresaDonoNome.value = ""
        empresaDonoEmail.value = ""
        funcionarioNome.value = ""
        funcionarioEmail.value = ""
        nomeEmpresaFuncionarios.clear()
    }
}

class Aplicativo : View() {
    val controleDeLogin: ControleDeLogin by inject()
    val contents = ItemFragment().contents
    var continuarAtualizando = true

    override val root = borderpane {
        title = "ToDo Zero"
        prefWidth = 500.0
        prefHeight = 800.0

        top = borderpane {
            right = button(graphic = Styles.addIcon()) {
                addClass(Styles.addIcon)

                action {

                    //TODO: APPLY THIS METHOD IN PLUS ICON TO ADD NEW TASK controleDeLogin.todoTarefas.add(Item("F", true))
                    val addNewTodo = AddNewTodo()
                    addNewTodo.openWindow()

                    addNewTodo.whenUndocked {

                        val todo = addNewTodo.todoContent() ?: return@whenUndocked

                        transaction(Database.connect(AppDB.Database.dB)) {
                            addLogger(StdOutSqlLogger)

                            AppDB.todo.insert {
                                it[descricao] = todo
                                it[situacao] = false
                                it[empresa] = controleDeLogin.todoUser.userEmpresa
                            }.resultedValues?.forEach {
                                controleDeLogin.todoTarefas.add(Item(it[AppDB.todo.descricao], it[AppDB.todo.situacao], it[AppDB.todo.id]))
                            }

                        }

                    }

                    contents.commit(contents.text, contents.completed)
                }
            }

            center = label("ToDo Zero") { addClass(Styles.appTitle)/*style { alignment = Pos.CENTER; fontWeight = FontWeight.BOLD; }*/ }

            left = button(graphic = Styles.sessionInfoIcon()) {
                addClass(Styles.sessionInfoIcon)

                action {
                    val funcionarioLista = FXCollections.observableArrayList<Funcionario>()

                    transaction(Database.connect(AppDB.Database.dB)) {
                        addLogger(StdOutSqlLogger)

                        AppDB.funcionario.select {
                            AppDB.funcionario.funcionario_empresa eq controleDeLogin.todoUser.userEmpresa
                        }.forEach {
                            funcionarioLista.add(
                                Funcionario(it[AppDB.funcionario.id], it[AppDB.funcionario.nome_completo])
                            )
                        }
                    }

                    val listaFuncionarios = ListaFuncionarios(funcionarioLista)

                    listaFuncionarios.openWindow()
                }
            }
        }

        center = listview(controleDeLogin.todoTarefas) {
            setPrefSize(500.0, 700.0)

            cellFragment(ItemFragment::class)

            this.refresh()

        }

        bottom = hbox {
            button(graphic = Styles.signOutIcon()) {
                addClass(Styles.signOutIcon)
                action {
                    controleDeLogin.logout()

                }
            }

            style { alignment = Pos.CENTER_RIGHT }
        }
    }

    fun corountineLogger(str: String) = println("[${Thread.currentThread().name}] $str")

    override fun onDock() {
        GlobalScope.launch {
            updateCurrentView()
        }
    }

    override fun onUndock() {
        continuarAtualizando = false
    }

    private suspend fun updateCurrentView() {
        do {
            delay(4000)
            corountineLogger("ATUALIZANDO A LISTA")

            transaction(Database.connect(AppDB.Database.dB)) {
                AppDB.todo.select {
                    AppDB.todo.empresa eq controleDeLogin.todoUser.userEmpresa
                }.forEach {

                    val jaExiste = controleDeLogin.todoTarefas.firstOrNull { todoItem ->
                        todoItem.todo_id == it[AppDB.todo.id]
                    }

                    if (jaExiste == null) {
                        runLater {
                            controleDeLogin.todoTarefas.add(Item(it[AppDB.todo.descricao], it[AppDB.todo.situacao], it[AppDB.todo.id]))
                         }
                    } else {
                        runLater {
                            controleDeLogin.todoTarefas.find { todoItem ->
                                todoItem.todo_id == it[AppDB.todo.id]
                            }.apply {
                                this?.completed = it[AppDB.todo.situacao]

                                contents.commit(contents.text, contents.completed)
                            }
                        }
                    }

                    //controleDeLogin.todoTarefas.add(Item(it[AppDB.todo.descricao], it[AppDB.todo.situacao], it[AppDB.todo.id]))
                    //corountineLogger("ATUALIZANDO VIEW")
                }

                val todoIds = mutableListOf<Int>()

                AppDB.todo.select {
                    AppDB.todo.empresa eq controleDeLogin.todoUser.userEmpresa
                }.forEach {
                    todoIds.add(it[AppDB.todo.id])
                }

                runLater {
                    controleDeLogin.todoTarefas = controleDeLogin.todoTarefas.apply {
//                        this.forEach {
//                            if (it.todo_id !in todoIds) {
//                                this.remove(it)
//                            }
//                        }
                        this.remove(controleDeLogin.todoTarefas.find {
                            it.todo_id !in todoIds
                        })
                    }
                }
            }
        } while (continuarAtualizando)
    }
}

class Funcionario(id: Int, nome: String) {
    var id by property<Int>()
    fun idProperty() = getProperty(Funcionario::id)

    var nome by property<String>()
    fun nameProperty() = getProperty(Funcionario::nome)

    init {
        this.id = id
        this.nome = nome
    }
}

class ListaFuncionarios(list: ObservableList<Funcionario>) : View() {
    val controleDeLogin: ControleDeLogin by inject()

    override val root = borderpane {
        title = "Minhas informações"
        prefWidth = 325.0

        top = borderpane {
            top = borderpane {
                left = label("Seu nome:") { style { padding = box(top = 5.px, right = 1.px, bottom = 5.px, left = 1.px) } }
                center = label(controleDeLogin.todoUser.userNome) { style { alignment = Pos.CENTER_RIGHT } }
            }

            center = borderpane {
                left = label("Sua empresa:") { style { padding = box(top = 5.px, right = 1.px, bottom = 5.px, left = 1.px) } }
                center = label(controleDeLogin.todoUser.userEmpresa) { style { alignment = Pos.CENTER_RIGHT; padding = box(top = 5.px, right = 1.px, bottom = 5.px, left = (-20).px)} }
            }

            bottom = borderpane {
                left = label("Seu email:") { style { padding = box(top = 5.px, right = 1.px, bottom = 5.px, left = 1.px) } }
                center = label(controleDeLogin.todoUser.userCargo) { style { alignment = Pos.CENTER_RIGHT } }
            }
        }

        center = label("Funcionarios da sua empresa") { style { padding = box(20.px) }}

        bottom = tableview(list) {
            column("ID", Funcionario::idProperty)
            column("NOME", Funcionario::nameProperty)
        }
    }
}

class AddNewTodo : View() {
    val todo = SimpleStringProperty()

    override val root = borderpane {
        title = "Adicionar nova tarefa"

        prefWidth = 400.0

        top = borderpane {
            center = vbox(4) {
                label("Adcionar nova tarefa:") { style { alignment = Pos.CENTER }}
                textfield(todo)
            }
        }


        bottom = borderpane {
            center = button("Adicionar") {
                action {
                    close()
                }
            }
        }
    }

    fun todoContent(): String? = todo.value
}

class Item(str: String, completed: Boolean, id: Int) {

    val tarefa_id = SimpleIntegerProperty(id)
    var todo_id by tarefa_id

    val strProperty = SimpleStringProperty(str)
    var str by strProperty

    val completedProperty = SimpleBooleanProperty(completed)
    var completed by completedProperty
}

class ItemModel(property: ObjectProperty<Item>) : ItemViewModel<Item>(itemProperty = property) {
    val text = bind(autocommit = true) {
        item?.strProperty }
    val completed = bind(autocommit = true) { item?.completedProperty }
    val id = bind(autocommit = false) { item?.tarefa_id }
}

class ItemFragment : ListCellFragment<Item>() {
    val contents = ItemModel(itemProperty)
    val controleDeLogin: ControleDeLogin by inject()

    override val root = borderpane {
        addClass(Styles.itemRoot)

        left = hbox(25) {

            runLater {
                button(graphic = Styles.deleteTodoIcon()) {
                    onHover {
                        if (it) {
                            parent.cursor = Cursor.HAND
                        } else {
                            parent.cursor = Cursor.DEFAULT
                        }
                    }

                    action {
                        transaction(Database.connect(AppDB.Database.dB)) {
                            addLogger(StdOutSqlLogger)

                            AppDB.todo.deleteWhere {
                                AppDB.todo.id eq contents.id.value.toInt()
                            }

                            controleDeLogin.todoTarefas.apply {
                                this.remove(controleDeLogin.todoTarefas.find {
                                    it.todo_id == contents.id.value.toInt()
                                })
                            }
                        }
                    }
                }

                button(graphic = if (contents.completed.value) Styles.checkCompletedTodoIcon() else Styles.checkUncompletedTodoIcon()) {
                    onHover {
                        if (it) {
                            parent.cursor = Cursor.HAND
                        } else {
                            parent.cursor = Cursor.DEFAULT
                        }
                    }

                    contents.completed.onChange {
                        graphic = if (it!!) Styles.checkCompletedTodoIcon() else Styles.checkUncompletedTodoIcon()
                    }

                    action {
                        contents.completed.value = !contents.completed.value

                        graphic = if (contents.completed.value) Styles.checkCompletedTodoIcon() else Styles.checkUncompletedTodoIcon()

                        runAsyncWithProgress {
                            controleDeLogin.updateDB(contents.id.value.toInt(), contents.completed.value)
                        }

                        contents.commit(contents.text, contents.completed)
                    }
                }

                label(contents.text) {
                    toggleClass(Styles.strikeThrough, contents.completed)
                }
            }
        }

//        right = hbox(25) {
//            button(graphic = Styles.deleteTodoIcon()) {
//                onHover {
//                    if (it) {
//                        parent.cursor = Cursor.HAND
//                    } else {
//                        parent.cursor = Cursor.DEFAULT
//                    }
//                }
//
//                action {
//                    println("Delete todo")
//                    //TODO: DELETE TODO
//                }
//            }
//        }
        //--------------------------------------------------------------------------OLD STYLE--------------------------------------------------------
//        addClass(Styles.itemRoot)
//
//        left = hbox {
//            label(contents.text) {
//                toggleClass(Styles.strikeThrough, contents.completed)
//            }
//        }
//
//        right = hbox(25) {
//            button(graphic = Styles.deleteTodoIcon()) {
//                onHover {
//                    if (it) {
//                        parent.cursor = Cursor.HAND
//                    }else{
//                        parent.cursor = Cursor.DEFAULT
//                    }
//                }
//
//                action {
//
//                }
//            }
//
//            runLater {
//                button(graphic = if (contents.completed.value) Styles.checkCompletedTodoIcon() else Styles.checkUncompletedTodoIcon()) {
//                    onHover {
//                        if (it) {
//                            parent.cursor = Cursor.HAND
//                        } else {
//                            parent.cursor = Cursor.DEFAULT
//                        }
//                    }
//
//                    action {
//                        contents.completed.value = !contents.completed.value
//
//                        graphic = if (contents.completed.value) Styles.checkCompletedTodoIcon() else Styles.checkUncompletedTodoIcon()
//
//                        runAsyncWithProgress {
//                            controleDeLogin.updateDB(contents.id.value.toInt(), contents.completed.value)
//                        }
//
//                        contents.commit(contents.text, contents.completed)
//                    }
//                }
//            }
//        }

    }
}