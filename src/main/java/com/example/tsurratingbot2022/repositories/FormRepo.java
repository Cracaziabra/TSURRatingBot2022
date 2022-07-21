package com.example.tsurratingbot2022.repositories;

import com.example.tsurratingbot2022.RatingForm;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface FormRepo extends CrudRepository<RatingForm, Long> {

    List<RatingForm> findAllByCompanyAndScoreDateBetween(String company, Date start, Date finish);

}
