package com.utp_reporta_backend.modules.sede.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp_reporta_backend.modules.sede.dto.request.SedeRequest;
import com.utp_reporta_backend.modules.sede.dto.response.SedeResponse;
import com.utp_reporta_backend.modules.sede.service.SedeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
//Controlador para manejar las operaciones CRUD de las sedes.
@RestController
@RequestMapping("/api/sedes")
@RequiredArgsConstructor
//Controlador para manejar las operaciones CRUD de las sedes.
public class SedeController {
	private final SedeService sedeService;

	@GetMapping
    // Listar todas las sedes
    public ResponseEntity<List<SedeResponse>> listarSedes() {
        return ResponseEntity.ok(sedeService.obtenerTodasLasSedes());
    }
    // Obtener una sede por su ID
    @GetMapping("/{id}")
    public ResponseEntity<SedeResponse> obtenerSedePorId(@PathVariable Long id) {
        return sedeService.obtenerSedePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    // Crear una nueva sede
    @PostMapping
    public ResponseEntity<SedeResponse> crearSede(@Valid @RequestBody SedeRequest request) {
        SedeResponse creada = sedeService.crearSede(request);
        return ResponseEntity.ok(creada);
    }
    // Actualizar una sede existente
    @PutMapping("/{id}")
    public ResponseEntity<SedeResponse> actualizarSede(@PathVariable Long id, @Valid @RequestBody SedeRequest request) {
        return sedeService.actualizarSede(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    // Eliminar una sede por su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSede(@PathVariable Long id) {
        sedeService.eliminarSede(id);
        return ResponseEntity.noContent().build();
    }
}


