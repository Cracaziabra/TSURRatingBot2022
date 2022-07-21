package com.example.tsurratingbot2022.repositories;

import com.example.tsurratingbot2022.Company;
import com.example.tsurratingbot2022.CompanyType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepo extends CrudRepository<Company, Long> {

    List<Company> findByCompanyType(CompanyType companyType);

    List<Company> findAllByOrderById();

    Company getCompanyById(Long id);
}
