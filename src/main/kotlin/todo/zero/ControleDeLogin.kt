package todo.zero

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javafx.scene.control.ButtonBar
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import todo.zero.AppDB.funcionario.autoIncrement
import tornadofx.*
import javax.sql.DataSource

class ControleDeLogin : Controller() {
    var todoUser: TodoUser = TodoUser()
    var todoTarefas: SortedFilteredList<Item> = SortedFilteredList()

    private var nome: String = ""
    private var empresa: String = ""
    private var cargo: String = ""

    fun login(nomeUsuario: String, nomeEmpresa: String, nomeCargo: String) {
        var acceptOrDenyLogin: Boolean = false
        val tarefasLista = SortedFilteredList<Item>()

        transaction(Database.connect(AppDB.Database.dB)) {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(AppDB.empresa, AppDB.funcionario, AppDB.todo)

            val checarFuncionario = AppDB.funcionario.select {
                AppDB.funcionario.nome_completo eq nomeUsuario and (
                        AppDB.funcionario.funcionario_email eq nomeCargo
                        ) and (
                        AppDB.funcionario.funcionario_empresa eq nomeEmpresa
                        )
            }.count()

            val checarLoginDono = AppDB.empresa.select {
                AppDB.empresa.dono_nome eq nomeUsuario and (AppDB.empresa.dono_email eq nomeCargo) and (AppDB.empresa.nome eq nomeEmpresa)
            }.count()

            val validarInformacao: Long.() -> Boolean = { this > 0 }

            when {
                validarInformacao(checarFuncionario) -> {
                    nome = nomeUsuario
                    empresa = nomeEmpresa
                    cargo = nomeCargo

                    AppDB.todo.select {
                        AppDB.todo.empresa eq empresa
                    }.orderBy(AppDB.todo.id).forEach {
                        tarefasLista.add(Item(it[AppDB.todo.descricao], it[AppDB.todo.situacao], it[AppDB.todo.id]))
                    }

                    acceptOrDenyLogin = true

                }

                validarInformacao(checarLoginDono) -> {
                    println("Logar dono")
                }

                else -> {
                    println("Informações erradas ou não cadastradas")
                }
            }
        }

        runLater {

            if (acceptOrDenyLogin) {

                todoTarefas = tarefasLista
                todoUser = TodoUser(nome, empresa, cargo)

                find(ToDoZeroLogin::class).replaceWith(Aplicativo::class, sizeToScene = true, centerOnScreen = true)
                //primaryStage.uiComponent<UIComponent>()?.replaceWith(Aplicativo::class, sizeToScene = true, centerOnScreen = true)
            } else {
                find(ToDoZeroLogin::class).root.vbox {
                    error("Informações erradas")
                }
            }

        }
    }

    fun updateDB(tarefaId: Int, novaSituacao: Boolean) {
        //Thread.sleep(2000)
        //println("\n\nAtualizando estado do ID $tarefaId para $situacao\n\n")
        transaction(Database.connect(AppDB.Database.dB)) {
            addLogger(StdOutSqlLogger)

            AppDB.todo.update({ AppDB.todo.id eq tarefaId }) {
                it[situacao] = novaSituacao
            }
        }
    }

    fun logout() {
//primaryStage.uiComponent<UIComponent>()?.replaceWith(ToDoZeroLogin::class, sizeToScene = true, centerOnScreen = true)
//        confirmation(
//            header = "Deseja sair do aplicativo",
//            actionFn = {btnType ->
//                if (btnType.buttonData == ButtonBar.ButtonData.OK_DONE) {
//                    primaryStage.close()
//                } else {
//                    return@confirmation
//                }
//            },
//            title = "Sair"
//        )

        primaryStage.close()
    }

}

class AppDB {

    object Database {
        var dB: DataSource = connect()

        fun connect(): DataSource {
            val config = HikariConfig()
//val todoDB = Database.connect("jdbc:postgresql://motty.db.elephantsql.com:5432/dwtiygpg", driver = "org.postgresql.Driver", user = "dwtiygpg", password = "debN90xKVGTkYAPiTgw2XV7KuPpcF31w")
//            config.jdbcUrl = "jdbc:postgresql://motty.db.elephantsql.com:5432/dwtiygpg"
//            config.username = "dwtiygpg"
//            config.password = "debN90xKVGTkYAPiTgw2XV7KuPpcF31w"
//            config.driverClassName = "org.postgresql.Driver"
            config.jdbcUrl = "jdbc:postgresql://todozeroapp.postgres.database.azure.com:5432/todozerodb"
            config.username = "todozero@todozeroapp"
            config.password = "zxaTf7HkjTcSmTG"
            config.driverClassName = "org.postgresql.Driver"
            return HikariDataSource(config)
        }
    }



    object empresa : Table("empresa") {
        val nome = varchar("nome", 20)
        val dono_nome = varchar("dono_nome", 30)
        val dono_email = varchar("dono_email", 50)

        override val primaryKey = PrimaryKey(nome)
    }

    object funcionario : Table("funcionario") {
        val id = integer("id").autoIncrement()
        val nome_completo = varchar("nome_completo", 30)
        val funcionario_email = varchar("email", 50)
        val funcionario_empresa = (varchar("empresa", 20) references AppDB.empresa.nome)

        override val primaryKey = PrimaryKey(id)
    }

    object  todo : Table("todo") {
        val id = integer("id").autoIncrement()
        val descricao = varchar("descricao", 200200)
        val situacao = bool("situacao")
        val empresa = (varchar("empresa", 20) references AppDB.empresa.nome)

        override val primaryKey = PrimaryKey(id)
    }
}

data class TodoUser(
    var userNome: String = "",
    var userEmpresa: String = "",
    var userCargo: String = ""
)