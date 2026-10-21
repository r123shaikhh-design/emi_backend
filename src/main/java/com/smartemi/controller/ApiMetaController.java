package com.smartemi.controller;

import com.smartemi.dto.LoanTypeInfo;
import com.smartemi.dto.MetaResponse;
import com.smartemi.entity.LoanType;
import com.smartemi.service.EmiService;
import java.util.Arrays;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public reference data for the React UI (loan types, sample values, tips, disclaimer). */
@RestController
@RequestMapping("/api/meta")
public class ApiMetaController {
    @GetMapping
    public MetaResponse meta() {
        return new MetaResponse(Arrays.stream(LoanType.values()).map(LoanTypeInfo::from).toList(), EmiService.DISCLAIMER);
    }
}
