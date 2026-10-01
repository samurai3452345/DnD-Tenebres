package com.java_dragons.dnd_tenebres.domain.economy.controller;
import com.java_dragons.dnd_tenebres.domain.economy.dto.WalletTransactionResponse;
import com.java_dragons.dnd_tenebres.domain.economy.repository.WalletTransactionRepository;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/wallet") @RequiredArgsConstructor
public class WalletController {
 private final WalletTransactionRepository repository;
 @GetMapping("/history") public ResponseEntity<Page<WalletTransactionResponse>> history(@CurrentPlayerId Long playerId,
   @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="25") int size) {
  var result=repository.findByPlayerIdOrderByIdDesc(playerId,PageRequest.of(Math.max(0,page),Math.max(1,Math.min(100,size))))
    .map(t->new WalletTransactionResponse(t.getId(),t.getAmount(),t.getBalanceBefore(),t.getBalanceAfter(),t.getReason().name(),t.getReferenceType(),t.getReferenceId(),t.getCreatedAt()));
  return ResponseEntity.ok(result);
 }
}
