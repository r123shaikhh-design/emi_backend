package com.smartemi.dto;

import java.util.List;

public record MetaResponse(List<LoanTypeInfo> loanTypes, String disclaimer) { }
