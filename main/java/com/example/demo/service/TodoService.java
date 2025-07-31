package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.TodoEntity;
import com.example.demo.repository.TodoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TodoService {

	private final TodoRepository repository;

	public List<TodoEntity> create(final TodoEntity entity) {
		validate(entity);

		repository.save(entity);

		log.info("Entity Id : {} is saved.", entity.getId());

		return repository.findByUserId(entity.getUserId());
	}

	private void validate(final TodoEntity entity) {
		if (entity == null) {
			log.warn("Entity cannot be null.");
			throw new RuntimeException("Entity cannot be null.");
		}

		if (entity.getUserId() == null) {
			log.warn("Unknown user.");
			throw new RuntimeException("Unknown user.");
		}
	}

	public List<TodoEntity> retrieve(final Long userId) {
		return repository.findByUserId(userId);
	}

	public List<TodoEntity> update(final TodoEntity entity) {
		validate(entity);

		final Optional<TodoEntity> original = repository.findById(entity.getId());

		original.ifPresent(todo -> {
			todo.setTitle(entity.getTitle());
			todo.setDone(entity.isDone());

			repository.save(todo);
		});

		return retrieve(entity.getUserId());
	}

	public List<TodoEntity> delete(final TodoEntity entity) {
		validate(entity);

		try {
			repository.delete(entity);
		} catch (Exception e) {
			log.error("error deleting entity ", entity.getId(), e);

			throw new RuntimeException("error deleting entity " + entity.getId());
		}
		
		return retrieve(entity.getUserId());
	}

}
