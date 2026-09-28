package com.java_dragons.dnd_tenebres.domain.item.repository;

import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;

@Repository
public interface ItemTemplateRepository extends JpaRepository<ItemTemplate, Long> {
    Optional<ItemTemplate> findByName(String name);
    List<ItemTemplate> findByTypeIn(List<ItemType> types);
}
