package com.goldwatch.goldtracker.repository;

import com.goldwatch.goldtracker.domain.SpotPrice;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpotPriceRepository extends JpaRepository<SpotPrice, Long> {

    Optional<SpotPrice> findFirstByOrderByRecordedAtDescIdDesc();

    Optional<SpotPrice> findFirstByRecordedAtBeforeOrderByRecordedAtDescIdDesc(Instant before);

    Optional<SpotPrice> findFirstByUsdThbIsNotNullOrderByRecordedAtDescIdDesc();

    Optional<SpotPrice> findFirstByUsdThbIsNotNullAndRecordedAtBeforeOrderByRecordedAtDescIdDesc(Instant before);
}
