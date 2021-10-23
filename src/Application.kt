package com.gabi

import com.gabi.authentication.JwtConfig
import com.gabi.entities.LoginBody
import com.gabi.entities.ToDo
import com.gabi.entities.ToDoDraft
import com.gabi.repository.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val jwtConfig = JwtConfig(System.getenv("KTOR_TODOLIST_JWT_SECRET")?:"Not Found")

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(CallLogging)
    install(ContentNegotiation){
        gson {
            setPrettyPrinting()
        }
    }
    install(Authentication){
        jwt {
            jwtConfig.configureKtorFeature(this)
        }
    }

    routing{

        val repository: ToDoRepository = MySQLTodoRepository()
        val userRepository: UserRepository = InMemoryUserRepository()

        get("/"){
            call.respondText("Hello!")
        }

        post("/login"){
            val loginBody = call.receive<LoginBody>()

            val user = userRepository.getUser(loginBody.username, loginBody.password)

            if(user == null){
                call.respond(HttpStatusCode.Unauthorized, "Invalid Credentials")
                return@post
            }
            val token = jwtConfig.generateToken(JwtConfig.JwtUser(user.userId, user.username))
            call.respond(token)
        }

        authenticate {
            get("/me"){
                val user = call.authentication.principal as JwtConfig.JwtUser
                call.respond(user)
            }

            get("/todos"){
                call.respond(repository.getAllToDos())
            }

            get("/todos/{id}"){
                val id = call.parameters["id"]?.toIntOrNull()

                if(id==null){
                    call.respond(HttpStatusCode.BadRequest,
                        "id parameter has to be a number"
                    )
                    return@get
                }

                val todo = repository.getToDo(id)

                if(todo == null){
                    call.respond(HttpStatusCode.NotFound,
                        "found no todo for provided id $id"
                    )
                } else{
                    call.respond(todo)
                }
            }

            post("/todos"){

                val todoDraft = call.receive<ToDoDraft>()

                val todo = repository.addToDo(todoDraft)
                call.respond(todo)

            }

            put("/todos/{id}"){

                val todoDraft = call.receive<ToDoDraft>()
                val todoId = call.parameters["id"]?.toIntOrNull()

                if(todoId == null){
                    call.respond(HttpStatusCode.BadRequest,
                        "id parameter has to be a number!")
                    return@put
                }

                val updated = repository.updateToDo(todoId, todoDraft)
                if(updated){
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound,
                        "found no todo with the id $todoId")
                }
            }

            delete("/todos/{id}"){

                val todoId = call.parameters["id"]?.toIntOrNull()

                if(todoId == null){
                    call.respond(HttpStatusCode.BadRequest,
                        "id parameter has to be a number!")
                    return@delete
                }

                val removed = repository.removeToDo(todoId)
                if(removed){
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound,
                        "found no todo with the id $todoId")
                }


            }
        }
    }

}

