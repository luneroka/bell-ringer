package com.bell_ringer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bell_ringer.models.Attempt;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

}
