package org.orioz.memberportfolio.auth.properties;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.temporal.ChronoUnit;
@AllArgsConstructor
@Data
public class ExpiryData {
    private Integer duration;
    private ChronoUnit unit;
}