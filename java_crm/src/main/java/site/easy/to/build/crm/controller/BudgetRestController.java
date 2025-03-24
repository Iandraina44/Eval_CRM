package site.easy.to.build.crm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.StatistiqueBudget;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.statistique.StatistiqueBudgetService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permet les appels CORS depuis n'importe quel domaine
public class BudgetRestController {

    private final BudgetService budgetService;
    private final StatistiqueBudgetService statistiqueBudgetService;

    public BudgetRestController(BudgetService budgetService, StatistiqueBudgetService statistiqueBudgetService) {
        this.budgetService = budgetService;
        this.statistiqueBudgetService = statistiqueBudgetService;
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<Budget>> getAllBudgets() {
        List<Budget> budgets = budgetService.findAll();
        return ResponseEntity.ok(budgets);
    }


    @GetMapping("/statistiques")
    public ResponseEntity<List<StatistiqueBudget>> getAllStatistiques() {
        List<StatistiqueBudget> statistiques = statistiqueBudgetService.getAllStatistiques();
        return ResponseEntity.ok(statistiques);
    }

   
}
