package com.example.Tracker.Repositories;  // Check the spelling here

import com.example.Tracker.Models.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ExpenseRepo extends JpaRepository<Expense, Long> {
    List<Expense> findByDateBetween(Date startDate, Date endDate);
}
