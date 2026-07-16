package com.java_dragons.dnd_tenebres.domain.item.repository;

import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ItemTemplateRepository extends JpaRepository<ItemTemplate, Long> {
    Optional<ItemTemplate> findByName(String name);
}
