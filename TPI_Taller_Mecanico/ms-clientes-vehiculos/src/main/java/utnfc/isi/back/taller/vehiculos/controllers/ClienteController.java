package utnfc.isi.back.taller.vehiculos.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import utnfc.isi.back.taller.vehiculos.dtos.ClienteRequest;
import utnfc.isi.back.taller.vehiculos.dtos.ClienteResponse;
import utnfc.isi.back.taller.vehiculos.services.ClienteService;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(summary = "F01 - Registrar cliente")
    @PostMapping
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteResponse.from(clienteService.crear(request)));
    }

    @Operation(summary = "Listar clientes")
    @GetMapping
    @PreAuthorize("hasAnyRole('JEFE_TALLER','MECANICO')")
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(clienteService.listar().stream().map(ClienteResponse::from).toList());
    }

    @Operation(summary = "Obtener cliente por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('JEFE_TALLER','MECANICO')")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ClienteResponse.from(clienteService.obtener(id)));
    }
}
