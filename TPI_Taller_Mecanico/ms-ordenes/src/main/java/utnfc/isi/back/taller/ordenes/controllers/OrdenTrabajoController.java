package utnfc.isi.back.taller.ordenes.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import utnfc.isi.back.taller.ordenes.dtos.AbrirOrdenRequest;
import utnfc.isi.back.taller.ordenes.dtos.DesperfectoRequest;
import utnfc.isi.back.taller.ordenes.dtos.OrdenResponse;
import utnfc.isi.back.taller.ordenes.dtos.VinRequest;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoOrden;
import utnfc.isi.back.taller.ordenes.services.OrdenTrabajoService;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenTrabajoController {

    private final OrdenTrabajoService ordenService;

    @Operation(summary = "F02 - Abrir orden de trabajo y asignar mecánico")
    @PostMapping
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<OrdenResponse> abrir(@Valid @RequestBody AbrirOrdenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OrdenResponse.from(ordenService.abrir(request)));
    }

    @Operation(summary = "Listar órdenes (el mecánico ve solo las suyas)")
    @GetMapping
    @PreAuthorize("hasAnyRole('JEFE_TALLER','MECANICO')")
    public ResponseEntity<List<OrdenResponse>> listar(@RequestParam(required = false) EstadoOrden estado) {
        return ResponseEntity.ok(ordenService.listar(estado).stream().map(OrdenResponse::from).toList());
    }

    @Operation(summary = "Consultar orden")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE_TALLER','MECANICO')")
    public ResponseEntity<OrdenResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(OrdenResponse.from(ordenService.obtenerParaConsulta(id)));
    }

    @Operation(summary = "F03 - Registrar VIN del vehículo")
    @PostMapping("/{id}/vin")
    @PreAuthorize("hasRole('MECANICO')")
    public ResponseEntity<OrdenResponse> registrarVin(@PathVariable Long id, @Valid @RequestBody VinRequest request) {
        return ResponseEntity.ok(OrdenResponse.from(ordenService.registrarVin(id, request)));
    }

    @Operation(summary = "F04 / F09 - Registrar desperfecto (adicional si la orden está en reparación)")
    @PostMapping("/{id}/desperfectos")
    @PreAuthorize("hasRole('MECANICO')")
    public ResponseEntity<OrdenResponse> agregarDesperfecto(@PathVariable Long id,
                                                            @Valid @RequestBody DesperfectoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrdenResponse.from(ordenService.agregarDesperfecto(id, request)));
    }

    @Operation(summary = "F10 - Finalizar trabajo")
    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('MECANICO')")
    public ResponseEntity<OrdenResponse> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(OrdenResponse.from(ordenService.finalizar(id)));
    }

    @Operation(summary = "F11 - Registrar entrega del vehículo")
    @PostMapping("/{id}/entrega")
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<OrdenResponse> entregar(@PathVariable Long id) {
        return ResponseEntity.ok(OrdenResponse.from(ordenService.entregar(id)));
    }
}
