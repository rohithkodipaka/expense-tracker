package com.rohithk.expensetracker.controller;


import com.rohithk.expensetracker.entity.Expense;
import com.rohithk.expensetracker.repository.ExpenseRepository;
import com.rohithk.expensetracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;
    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense) {
        try {
            Expense saved = expenseService.createExpense(expense);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ---------------- READ ALL ----------------
    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();
        return ResponseEntity.ok(expenses);
    }

    // ---------------- READ BY ID ----------------
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable UUID id) {
        return expenseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------- UPDATE ----------------
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable UUID id,
                                                 @RequestBody Expense updatedExpense) {
        return expenseRepository.findById(id)
                .map(expense -> {
                    expense.setAmount(updatedExpense.getAmount());
                    expense.setCategory(updatedExpense.getCategory());
                    expense.setCurrency(updatedExpense.getCurrency());
                    expense.setDescription(updatedExpense.getDescription());
                    expense.setOccurredAt(updatedExpense.getOccurredAt());
                    expenseRepository.save(expense);
                    return ResponseEntity.ok(expense);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------- DELETE ----------------
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteExpense(@PathVariable UUID id) {
//        return expenseRepository.findById(id)
//                .map(expense -> {
//                    expenseRepository.delete(expense);
//                    return ResponseEntity.noContent().build();
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
}
