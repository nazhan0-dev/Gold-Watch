package com.goldwatch.goldtracker.repository;

import com.goldwatch.goldtracker.domain.GoldPrice;
import com.goldwatch.goldtracker.domain.GoldType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoldPriceRepository extends JpaRepository<GoldPrice, Long> {

    Optional<GoldPrice> findFirstByTypeOrderByAnnouncedAtDescIdDesc(GoldType type);

    Optional<GoldPrice> findFirstByTypeAndAnnouncedAtBeforeOrderByAnnouncedAtDescIdDesc(GoldType type, Instant before);

    List<GoldPrice> findByAnnouncedAtGreaterThanEqualAndAnnouncedAtLessThanOrderByAnnouncedAtAscIdAsc(
            Instant fromInclusive, Instant toExclusive);
}
