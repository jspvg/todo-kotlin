package com.gabi.repository

import com.gabi.entities.ToDo
import com.gabi.entities.ToDoDraft

class InMemoryToDoRepository : ToDoRepository {

    private val todos= mutableListOf<ToDo>()

    override fun getAllToDos(): List<ToDo> {
        return todos
    }

    override fun getToDo(id: Int): ToDo? {
        return todos.firstOrNull{ it.id == id }
    }

    override fun addToDo(draft: ToDoDraft): ToDo {

        val todo = ToDo(
            id= todos.size + 1,
            title = draft.title,
            done = draft.done
        )
        todos.add(todo)
        return todo

    }

    override fun removeToDo(id: Int): Boolean {

        return todos.removeIf{ it.id == id}

    }

    override fun updateToDo(id: Int, draft: ToDoDraft): Boolean {

        val todo = todos.firstOrNull{ it.id == id}
            ?: return false

        todo.title = draft.title
        todo.done = draft.done
        return true
    }
}