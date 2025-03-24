package site.easy.to.build.crm.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.StatistiqueBudget;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.statistique.StatistiqueBudgetService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BudgetRestController {

    private final BudgetService budgetService;
    private final StatistiqueBudgetService statistiqueBudgetService;

    @Autowired
    public BudgetRestController(BudgetService budgetService, StatistiqueBudgetService statistiqueBudgetService) {
        this.budgetService = budgetService;
        this.statistiqueBudgetService = statistiqueBudgetService;
    }

    @GetMapping("/budgets")
    public List<Budget> getAllBudgets() {
        return budgetService.findAll();
    }

    @GetMapping("/statistiques")
    public List<StatistiqueBudget> getAllStatistiques() {
        return statistiqueBudgetService.getAllStatistiques();
    }


}
