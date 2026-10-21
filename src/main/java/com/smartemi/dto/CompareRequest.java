package com.smartemi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class CompareRequest {
    @NotNull @Size(min = 2, max = 3, message = "Provide 2 or 3 loans to compare")
    @Valid
    private List<EmiRequest> loans = new ArrayList<>();

    public List<EmiRequest> getLoans() { return loans; }
    public void setLoans(List<EmiRequest> loans) { this.loans = loans; }
}
