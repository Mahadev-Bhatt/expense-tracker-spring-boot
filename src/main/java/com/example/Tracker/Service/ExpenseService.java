package com.example.Tracker.Service;

import com.example.Tracker.Models.Expense;
import com.example.Tracker.Repositories.ExpenseRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor; // Keep if you use it explicitly, but @AllArgsConstructor usually replaces the need for a default in services
import org.springframework.beans.factory.annotation.Autowired; // Keep for field injection, or remove for constructor injection
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;       // Assuming Expense.date is java.util.Date
import java.util.List;
import java.util.Optional;

@Service
// @AllArgsConstructor is usually sufficient for constructor injection if no @Autowired on fields
// @NoArgsConstructor is less common for services unless specifically needed for reflection frameworks
public class ExpenseService {

    private final ExpenseRepo expenseRepo; // Use final for clarity with constructor injection

    // Option 1: Constructor Injection (Recommended) - No @Autowired needed here with @AllArgsConstructor
    // If you remove @NoArgsConstructor and Lombok generates an all-args constructor, Spring uses it.
    // However, if you use @Autowired on fields, Lombok's @AllArgsConstructor will still work.
    @Autowired // This annotation is optional here if using Lombok's @AllArgsConstructor or explicit constructor
    public ExpenseService(ExpenseRepo expenseRepo) {
        this.expenseRepo = expenseRepo;
    }

    // Option 2: Field Injection (Your current setup, which works)
    // @Autowired
    // private ExpenseRepo expenseRepo;


    /**
     * Saves a new expense or updates an existing one.
     * @param expense The Expense object to save.
     * @return The saved Expense object.
     */
    public Expense saveExpense(Expense expense) {
        return expenseRepo.save(expense);
    }

    /**
     * Retrieves all expenses from the database.
     * @return A list of all Expense objects.
     */
    public List<Expense> getAllExpenses() {
        return expenseRepo.findAll();
    }

    /**
     * Retrieves an expense by its unique ID.
     * @param id The ID of the expense to retrieve.
     * @return An Optional containing the Expense if found, or empty if not.
     */
    public Optional<Expense> getExpenseById(Long id) {
        return expenseRepo.findById(id);
    }

    /**
     * Deletes an expense by its unique ID.
     * @param id The ID of the expense to delete.
     */
    public void deleteExpense(Long id) {
        expenseRepo.deleteById(id);
    }

    /**
     * Retrieves a list of expenses within a specific month and year.
     * @param month The month (1-12).
     * @param year The year.
     * @return A list of expenses within the specified month.
     */
    public List<Expense> getExpensesByMonthAndYear(int month, int year) {
        // Define the start and end dates for the given month and year
        // We use LocalDate for calculation as it's cleaner for date arithmetic
        LocalDate startLocalDate = LocalDate.of(year, month, 1);
        LocalDate endLocalDate = startLocalDate.plusMonths(1).minusDays(1); // Last day of the month

        // Convert LocalDate to java.util.Date for compatibility with your JPA entity/repository
        // Assuming your database column and Expense entity 'date' field are java.util.Date
        Date startUtilDate = Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endUtilDate = Date.from(endLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return expenseRepo.findByDateBetween(startUtilDate, endUtilDate);
    }
}

