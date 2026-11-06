package utnfc.isi.back.taller.notificaciones.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import utnfc.isi.back.taller.notificaciones.dtos.NotificacionRequest;
import utnfc.isi.back.taller.notificaciones.dtos.NotificacionResponse;
import utnfc.isi.back.taller.notificaciones.services.NotificacionService;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    /** Lo invoca ms-ordenes con el token del usuario que hizo la operación. */
    @Operation(summary = "F12 - Enviar notificación por WhatsApp")
    @PostMapping
    @PreAuthorize("hasAnyRole('JEFE_TALLER','MECANICO')")
    public ResponseEntity<NotificacionResponse> crear(@Valid @RequestBody NotificacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificacionResponse.from(notificacionService.crear(request)));
    }

    @Operation(summary = "Historial de notificaciones de una orden")
    @GetMapping
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<List<NotificacionResponse>> listar(@RequestParam Long ordenId) {
        return ResponseEntity.ok(notificacionService.listarPorOrden(ordenId).stream()
                .map(NotificacionResponse::from).toList());
    }
}
