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
import utnfc.isi.back.taller.ordenes.dtos.OrdenResponse;
import utnfc.isi.back.taller.ordenes.dtos.RepuestoRequest;
import utnfc.isi.back.taller.ordenes.services.RepuestoService;

@RestController
@RequestMapping("/api/ordenes/{ordenId}/repuestos")
@RequiredArgsConstructor
public class RepuestoController {

    private final RepuestoService repuestoService;

    @Operation(summary = "F05 / F09 - Registrar repuesto necesario")
    @PostMapping
    @PreAuthorize("hasRole('MECANICO')")
    public ResponseEntity<OrdenResponse> agregar(@PathVariable Long ordenId, @Valid @RequestBody RepuestoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OrdenResponse.from(repuestoService.agregar(ordenId, request)));
    }

    @Operation(summary = "F08 - Registrar pedido de los repuestos aceptados")
    @PostMapping("/pedido")
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<OrdenResponse> registrarPedido(@PathVariable Long ordenId) {
        return ResponseEntity.ok(OrdenResponse.from(repuestoService.registrarPedido(ordenId)));
    }

    @Operation(summary = "F08 - Registrar llegada de un repuesto")
    @PostMapping("/{repuestoId}/recepcion")
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<OrdenResponse> registrarRecepcion(@PathVariable Long ordenId, @PathVariable Long repuestoId) {
        return ResponseEntity.ok(OrdenResponse.from(repuestoService.registrarRecepcion(ordenId, repuestoId)));
    }
}
