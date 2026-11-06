package utnfc.isi.back.taller.vehiculos.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import utnfc.isi.back.taller.vehiculos.dtos.VehiculoRequest;
import utnfc.isi.back.taller.vehiculos.dtos.VehiculoResponse;
import utnfc.isi.back.taller.vehiculos.dtos.VehiculoUpdateRequest;
import utnfc.isi.back.taller.vehiculos.dtos.VinRequest;
import utnfc.isi.back.taller.vehiculos.services.VehiculoService;

@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @Operation(summary = "F01 - Registrar vehículo")
    @PostMapping
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<VehiculoResponse> crear(@Valid @RequestBody VehiculoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(VehiculoResponse.from(vehiculoService.crear(request)));
    }

    @Operation(summary = "Buscar vehículo por patente")
    @GetMapping
    @PreAuthorize("hasAnyRole('JEFE_TALLER','MECANICO')")
    public ResponseEntity<VehiculoResponse> buscarPorPatente(@RequestParam String patente) {
        return vehiculoService.buscarPorPatente(patente)
                .map(VehiculoResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtener vehículo por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE_TALLER','MECANICO')")
    public ResponseEntity<VehiculoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(VehiculoResponse.from(vehiculoService.obtener(id)));
    }

    @Operation(summary = "Corregir marca, modelo o año")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<VehiculoResponse> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody VehiculoUpdateRequest request) {
        return ResponseEntity.ok(VehiculoResponse.from(vehiculoService.actualizar(id, request)));
    }

    /** Uso interno: solo lo invoca ms-ordenes, con el token del mecánico, cuando registra el VIN en la orden. */
    @Operation(summary = "F03 - Registrar VIN (consulta vPIC)")
    @PutMapping("/{id}/vin")
    @PreAuthorize("hasRole('MECANICO')")
    public ResponseEntity<VehiculoResponse> registrarVin(@PathVariable Long id, @Valid @RequestBody VinRequest request) {
        return ResponseEntity.ok(VehiculoResponse.from(vehiculoService.registrarVin(id, request)));
    }
}
