package com.example.tsurratingbot2022.repositories;

import com.example.tsurratingbot2022.Criteria;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CriteriaRepo extends CrudRepository<Criteria, Long> {

    List<Criteria> findAllByOrderById();

    Criteria findCriteriaById(Long id);
}
