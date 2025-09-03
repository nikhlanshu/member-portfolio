package org.orioz.memberportfolio.dtos.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.orioz.memberportfolio.models.Member;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MembershipUpdateRequest {

    @NotNull
    private Member.MembershipDuration duration;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private Double amount;
}

