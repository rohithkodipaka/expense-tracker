package com.rohithk.expensetracker.dto.response;

public record JwtResponse(String accessToken, String accessType, String email, String[] roles) {
}
