package site.easy.to.build.crm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.service.depense.DepenseService;

@RestController
@RequestMapping("/api/depenses")
public class RestDepenseController {
    
    private final DepenseService depenseService;

    public RestDepenseController(DepenseService depenseService) {
        this.depenseService = depenseService;
    }

    @GetMapping("/update/{id}/{valeur}")
    public ResponseEntity<Depense> updateDepense(@PathVariable Integer id, @PathVariable double valeur) {
        Depense depense = depenseService.getDepenseById(id).orElse(null);
        System.out.println("valeur"+valeur);
        System.out.println("id"+id);
        if (depense != null) {
            depense.setValeurDepense(valeur);
            depense = depenseService.saveDepense(depense);
            return ResponseEntity.ok(depense);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    

}
