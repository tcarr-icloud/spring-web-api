package com.example.springwebapi.repositories;

import com.example.springwebapi.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, String>, CrudRepository<User, String> {
}
