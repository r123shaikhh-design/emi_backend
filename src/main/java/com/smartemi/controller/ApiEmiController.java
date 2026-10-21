package com.smartemi.controller;

import com.smartemi.dto.*;
import com.smartemi.service.EmiService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emi")
public class ApiEmiController {
    private final EmiService emiService;

    public ApiEmiController(EmiService emiService) { this.emiService = emiService; }

    @PostMapping("/calculate")
    public EmiSummary calculate(@Valid @RequestBody EmiRequest req) {
        return emiService.calculate(req).summary();
    }

    @PostMapping("/schedule")
    public List<ScheduleRow> schedule(@Valid @RequestBody EmiRequest req) {
        return emiService.calculate(req).schedule();
    }

    /** One call for the React calculator page: summary, step-by-step explanation values and the full schedule. */
    @PostMapping("/full")
    public EmiFullResponse full(@Valid @RequestBody EmiRequest req) {
        EmiResult r = emiService.calculate(req);
        return new EmiFullResponse(r.summary(), emiService.explain(req, r), r.schedule());
    }

    @PostMapping("/compare")
    public CompareResponse compare(@Valid @RequestBody CompareRequest req) {
        return emiService.compare(req.getLoans());
    }
}
