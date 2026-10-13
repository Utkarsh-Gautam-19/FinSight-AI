package com.finsight.ai.config;

import com.finsight.ai.entity.*;
import com.finsight.ai.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private RecurringExpenseRepository recurringExpenseRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataSeeder: Initializing sample test data...");
        User demoUser = userRepository.findByFirebaseUid("demo-firebase-uid")
            .orElseGet(() -> {
                User u = new User("demo-firebase-uid", "demo@finsight.ai", "John", "Doe");
                u.setCurrency("ZAR");
                u.setDarkMode(false);
                return userRepository.save(u);
            });

        User devUser = userRepository.findByFirebaseUid("dev-user-default")
            .orElseGet(() -> {
                User u = new User("dev-user-default", "dev@finsight.ai", "John", "Doe");
                u.setCurrency("ZAR");
                u.setDarkMode(false);
                return userRepository.save(u);
            });

        seedDataForUser(demoUser);
        seedDataForUser(devUser);

        // Seed for any other user in DB if they have 0 expenses
        for (User u : userRepository.findAll()) {
            if (expenseRepository.findByUserOrderByDateDesc(u).isEmpty()) {
                seedDataForUser(u);
            }
        }
    }

    public void seedDataForUser(User user) {
        if (!expenseRepository.findByUserOrderByDateDesc(user).isEmpty()) {
            return;
        }

        System.out.println("Seeding sample expenses and budgets for user: " + user.getEmail());

        // Create sample expenses for current month
        List<Expense> expenses = Arrays.asList(
            new Expense("Grocery shopping", new BigDecimal("485.50"), ExpenseCategory.GROCERIES, LocalDate.now().minusDays(1), user),
            new Expense("Gas station", new BigDecimal("245.20"), ExpenseCategory.TRANSPORTATION, LocalDate.now().minusDays(2), user),
            new Expense("Coffee shop", new BigDecimal("62.75"), ExpenseCategory.FOOD_DINING, LocalDate.now().minusDays(3), user),
            new Expense("Netflix subscription", new BigDecimal("159.99"), ExpenseCategory.ENTERTAINMENT, LocalDate.now().minusDays(4), user),
            new Expense("Electricity bill", new BigDecimal("820.00"), ExpenseCategory.BILLS_UTILITIES, LocalDate.now().minusDays(5), user),
            new Expense("Lunch at restaurant", new BigDecimal("228.50"), ExpenseCategory.FOOD_DINING, LocalDate.now().minusDays(6), user),
            new Expense("Book purchase", new BigDecimal("119.99"), ExpenseCategory.EDUCATION, LocalDate.now().minusDays(7), user),
            new Expense("Gym membership", new BigDecimal("350.00"), ExpenseCategory.HEALTHCARE, LocalDate.now().minusDays(8), user),
            new Expense("Online shopping", new BigDecimal("475.30"), ExpenseCategory.SHOPPING, LocalDate.now().minusDays(9), user),
            new Expense("Uber ride", new BigDecimal("118.45"), ExpenseCategory.TRANSPORTATION, LocalDate.now().minusDays(10), user),
            new Expense("Movie tickets", new BigDecimal("184.00"), ExpenseCategory.ENTERTAINMENT, LocalDate.now().minusDays(11), user),
            new Expense("Haircut", new BigDecimal("235.00"), ExpenseCategory.PERSONAL_CARE, LocalDate.now().minusDays(12), user),
            new Expense("Phone bill", new BigDecimal("465.00"), ExpenseCategory.BILLS_UTILITIES, LocalDate.now().minusDays(13), user),
            new Expense("Grocery shopping", new BigDecimal("592.15"), ExpenseCategory.GROCERIES, LocalDate.now().minusDays(14), user),
            new Expense("Gift for friend", new BigDecimal("240.00"), ExpenseCategory.GIFTS_DONATIONS, LocalDate.now().minusDays(15), user)
        );

        expenseRepository.saveAll(expenses);

        // Create sample budgets for current month
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        List<Budget> budgets = Arrays.asList(
            new Budget(ExpenseCategory.GROCERIES, new BigDecimal("2000.00"), currentMonth, currentYear, user),
            new Budget(ExpenseCategory.FOOD_DINING, new BigDecimal("1500.00"), currentMonth, currentYear, user),
            new Budget(ExpenseCategory.TRANSPORTATION, new BigDecimal("1000.00"), currentMonth, currentYear, user),
            new Budget(ExpenseCategory.ENTERTAINMENT, new BigDecimal("800.00"), currentMonth, currentYear, user),
            new Budget(ExpenseCategory.BILLS_UTILITIES, new BigDecimal("2500.00"), currentMonth, currentYear, user),
            new Budget(ExpenseCategory.SHOPPING, new BigDecimal("1200.00"), currentMonth, currentYear, user),
            new Budget(ExpenseCategory.HEALTHCARE, new BigDecimal("800.00"), currentMonth, currentYear, user),
            new Budget(ExpenseCategory.PERSONAL_CARE, new BigDecimal("600.00"), currentMonth, currentYear, user)
        );

        // Update currentSpent for each budget based on expenses
        for (Budget b : budgets) {
            BigDecimal totalSpent = expenses.stream()
                .filter(e -> e.getCategory() == b.getCategory())
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            b.setCurrentSpent(totalSpent);
        }

        budgetRepository.saveAll(budgets);

        // Create sample recurring expenses
        List<RecurringExpense> recurringExpenses = Arrays.asList(
            new RecurringExpense("Netflix Subscription", new BigDecimal("159.99"), ExpenseCategory.ENTERTAINMENT, 
                               RecurrenceFrequency.MONTHLY, LocalDate.now().withDayOfMonth(1), user),
            new RecurringExpense("Gym Membership", new BigDecimal("350.00"), ExpenseCategory.HEALTHCARE, 
                               RecurrenceFrequency.MONTHLY, LocalDate.now().withDayOfMonth(15), user),
            new RecurringExpense("Phone Bill", new BigDecimal("465.00"), ExpenseCategory.BILLS_UTILITIES, 
                               RecurrenceFrequency.MONTHLY, LocalDate.now().withDayOfMonth(5), user),
            new RecurringExpense("Coffee Subscription", new BigDecimal("124.99"), ExpenseCategory.FOOD_DINING, 
                               RecurrenceFrequency.MONTHLY, LocalDate.now().withDayOfMonth(10), user)
        );

        recurringExpenseRepository.saveAll(recurringExpenses);

        System.out.println("Sample data seeded successfully for user: " + user.getEmail());
    }
}
