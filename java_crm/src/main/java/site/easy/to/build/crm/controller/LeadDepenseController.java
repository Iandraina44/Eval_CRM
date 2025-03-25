package site.easy.to.build.crm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import site.easy.to.build.crm.entity.LeadDepenseDetails;
import site.easy.to.build.crm.service.details.LeadDepenseService;

import java.util.List;

@RestController
@RequestMapping("/api/leads-depenses")
public class LeadDepenseController {

    private final LeadDepenseService leadDepenseService;

    public LeadDepenseController(LeadDepenseService leadDepenseService) {
        this.leadDepenseService = leadDepenseService;
    }

    @GetMapping
    public ResponseEntity<List<LeadDepenseDetails>> getAllLeadsDepenses() {
        List<LeadDepenseDetails> leads = leadDepenseService.getAllLeadDepenses();
        return ResponseEntity.ok(leads); // 200 OK
    }


}
