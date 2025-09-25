package com.example.springwebapi.repositories;

import com.example.springwebapi.entities.Person;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "persons", collectionResourceRel = "persons")
public interface PersonRepository extends PagingAndSortingRepository<com.example.springwebapi.entities.Person, Long>, org.springframework.data.repository.CrudRepository<com.example.springwebapi.entities.Person, Long> {
    com.example.springwebapi.entities.Person findByUsername(String username);

    List<Person> findByFirstName(String firstName);

    List<com.example.springwebapi.entities.Person> findByLastName(String lastName);
}
