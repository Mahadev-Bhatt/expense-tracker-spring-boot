package com.example.Tracker.AllControllers;

import com.example.Tracker.Models.Expense;
import com.example.Tracker.Service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal; // Added for logged-in user info
import java.time.LocalDate; // Make sure this is imported if you use it
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/expenses") // Base path for all expense related endpoints
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    // Assuming you have a constructor for ExpenseService if not using @Autowired on field
    // public ExpenseController(ExpenseService expenseService) {
    //     this.expenseService = expenseService;
    // }


    // This is the method that handles GET requests to /expenses/view
    // This is the one your AuthController and SecurityConfig redirect to after login/registration.
    @GetMapping("/view")
    public String viewExpenses(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("loggedInUserEmail", principal.getName());
            // You will eventually want to filter expenses by the logged-in user.
            // For now, it's getting all expenses, which might not be ideal for a dashboard.
            model.addAttribute("expenses", expenseService.getAllExpenses());
        } else {
            // This should ideally not be hit if authentication is working correctly.
            return "redirect:/login";
        }
        // *** CRITICAL CHANGE: Return the correct HTML file name ***
        return "views"; // This will now correctly look for src/main/resources/templates/views.html
    }

    // You also have a @GetMapping("/expenses") mapping.
    // If you want /expenses to also show the main list, it should return "views" too.
    @GetMapping("") // This maps to just /expenses
    public String getAllExpenses(Model model) {
        model.addAttribute("expenses", expenseService.getAllExpenses());
        return "views"; // Also return views.html
    }


    // --- Rest of your ExpenseController methods (update, delete, add forms, monthly summary) ---
    // Ensure all other methods that return a view also return the correct template name
    // (e.g., "updateExpenseForm", "addExpenseForm", "monthlySummary")
    // and that those HTML files exist in src/main/resources/templates/ with those exact names.


    // Example: Adjusted redirects to consistently go to /expenses/view (your new dashboard)
    @PostMapping("/update")
    public String updateExpense(Expense expense) {
        Optional<Expense> existingOpt = expenseService.getExpenseById(expense.getId());
        if (existingOpt.isPresent()) {
            Expense existing = existingOpt.get();
            existing.setName(expense.getName());
            existing.setAmount(expense.getAmount());
            existing.setCategory(expense.getCategory());
            existing.setDate(expense.getDate()); // Assuming date is handled correctly
            expenseService.saveExpense(existing);
        }
        return "redirect:/expenses/view"; // Redirect to the dashboard
    }

    @PostMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id) {
        Optional<Expense> optionalExpense = expenseService.getExpenseById(id);
        if (optionalExpense.isPresent()) {
            expenseService.deleteExpense(id);
        }
        return "redirect:/expenses/view"; // Redirect to the dashboard
    }

    @GetMapping("/add")
    public String showAddExpenseForm(Model model) {
        model.addAttribute("expense", new Expense());
        return "addExpenseForm"; // Your add form template
    }

    @PostMapping("/add")
    public String addExpenseWithUser(@ModelAttribute Expense expense, Principal principal, RedirectAttributes redirectAttributes) {
        // You should associate the expense with the logged-in user here.
        // This requires injecting UserRepository and fetching the User by email.
        try {
            expenseService.saveExpense(expense);
            redirectAttributes.addFlashAttribute("successMessage", "Expense added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding expense: " + e.getMessage());
        }
        return "redirect:/expenses/view"; // Redirect to the dashboard
    }

    @GetMapping("/monthly-summary")
    public String getMonthlyExpenses(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            Model model) {

        LocalDate now = LocalDate.now();
        if (month == null) {
            month = now.getMonthValue();
        }
        if (year == null) {
            year = now.getYear();
        }

        List<Expense> monthlyExpenses = expenseService.getExpensesByMonthAndYear(month, year);
        double totalMonthlySpending = monthlyExpenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        model.addAttribute("expenses", monthlyExpenses);
        model.addAttribute("currentMonth", month);
        model.addAttribute("currentYear", year);
        model.addAttribute("totalSpending", totalMonthlySpending);
        model.addAttribute("viewTitle", "Monthly Spending Summary for " + now.getMonth().name() + " " + year);

        model.addAttribute("availableMonths", List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        model.addAttribute("availableYears", List.of(now.getYear() - 1, now.getYear(), now.getYear() + 1));

        return "monthlySummary"; // Your monthly summary template
    }
}
