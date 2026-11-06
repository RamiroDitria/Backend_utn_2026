package utnfc.isi.back.taller.ordenes.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import utnfc.isi.back.taller.ordenes.dtos.DecisionRequest;
import utnfc.isi.back.taller.ordenes.dtos.OrdenResponse;
import utnfc.isi.back.taller.ordenes.dtos.PresupuestoRequest;
import utnfc.isi.back.taller.ordenes.services.PresupuestoService;

@RestController
@RequestMapping("/api/ordenes/{ordenId}/presupuestos")
@RequiredArgsConstructor
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    @Operation(summary = "F06 - Generar presupuesto (envía WhatsApp al cliente)")
    @PostMapping
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<OrdenResponse> generar(@PathVariable Long ordenId, @Valid @RequestBody PresupuestoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OrdenResponse.from(presupuestoService.generar(ordenId, request)));
    }

    @Operation(summary = "F07 - Registrar la decisión del cliente")
    @PostMapping("/{presupuestoId}/decision")
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<OrdenResponse> registrarDecision(@PathVariable Long ordenId, @PathVariable Long presupuestoId,
                                                           @Valid @RequestBody DecisionRequest request) {
        return ResponseEntity.ok(OrdenResponse.from(presupuestoService.registrarDecision(ordenId, presupuestoId, request)));
    }
}
