package com.java_dragons.dnd_tenebres.domain.item.repository;
import com.java_dragons.dnd_tenebres.domain.item.entity.MerchantOffer;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface MerchantOfferRepository extends JpaRepository<MerchantOffer, Long> {
    List<MerchantOffer> findByLocationIdAndEnabledTrueOrderById(String locationId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MerchantOffer> findByIdAndLocationIdAndEnabledTrue(Long id, String locationId);
}
