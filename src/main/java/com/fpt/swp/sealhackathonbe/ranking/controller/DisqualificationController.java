//package com.fpt.swp.sealhackathonbe.ranking.controller;
//
//import com.fpt.swp.sealhackathonbe.ranking.dto.DisqualificationDTO;
//import com.fpt.swp.sealhackathonbe.ranking.dto.DisqualificationRequestDTO;
//import com.fpt.swp.sealhackathonbe.ranking.service.DisqualificationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.TokenHashUtil.UUID;
//import java.TokenHashUtil.Map;
//
//@RestController
//@RequestMapping("/api/v1/admin/disqualifications")
//public class DisqualificationController {
//
//    private final DisqualificationService disqualificationService;
//
//    @Autowired
//    public DisqualificationController(DisqualificationService disqualificationService) {
//        this.disqualificationService = disqualificationService;
//    }
//
//    @PostMapping
//    public ResponseEntity<DisqualificationDTO> disqualify(
//            @RequestBody DisqualificationRequestDTO requestDTO,
//            @RequestParam(required = false) UUID adminId) {
//
//        // Mock ID if not provided, or extract from SecurityContext later
//        UUID id = adminId != null ? adminId : UUID.randomUUID();
//
//        DisqualificationDTO dto = disqualificationService.disqualify(requestDTO, id);
//        return ResponseEntity.ok(dto);
//    }
//
//    @PutMapping("/{id}/reverse")
//    public ResponseEntity<DisqualificationDTO> reverseDisqualification(
//            @PathVariable("id") UUID disqualificationId,
//            @RequestBody Map<String, String> requestBody,
//            @RequestParam(required = false) UUID adminId) {
//
//        String reversalReason = requestBody.get("reversalReason");
//        UUID id = adminId != null ? adminId : UUID.randomUUID();
//
//        DisqualificationDTO dto = disqualificationService.reverseDisqualification(disqualificationId, reversalReason, id);
//        return ResponseEntity.ok(dto);
//    }
//}
