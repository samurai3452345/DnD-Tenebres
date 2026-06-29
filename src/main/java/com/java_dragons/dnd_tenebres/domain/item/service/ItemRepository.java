package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.domain.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

interface ItemRepository extends JpaRepository<Item, Long> {

}
