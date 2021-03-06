package com.backend.persistence.service;

import java.util.List;

import com.backend.persistence.entity.Todo;

public interface TodoService {

	List<Todo> findTodoForEmployee();

	void save(Todo todo);

	void createTodoForEmployee(Todo todo);

	Todo findTodoById(Long id);

	void toggleTodoStatus(Long id);

	void deleteTodo(Long id);

}
