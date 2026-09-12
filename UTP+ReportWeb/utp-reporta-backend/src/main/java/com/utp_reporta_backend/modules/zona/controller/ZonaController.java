package com.utp_reporta_backend.modules.zona.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.utp_reporta_backend.modules.zona.dto.request.ZonaRequest;
import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
import com.utp_reporta_backend.modules.zona.service.ZonaService;

import lombok.RequiredArgsConstructor;
//Controlador para manejar las operaciones CRUD de las zonas.
@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
//Controlador para manejar las operaciones CRUD de las zonas.
public class ZonaController {
	private final ZonaService zonaService;
	@GetMapping
    public ResponseEntity<List<ZonaResponse>> listarZonas(@RequestParam(name = "includeInactive", required = false, defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(zonaService.obtenerTodasLasZonas(includeInactive));
    }

    @GetMapping("/sede/{sedeId}")
    public ResponseEntity<List<ZonaResponse>> listarZonasPorSede(@PathVariable Long sedeId,
            @RequestParam(name = "includeInactive", required = false, defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(zonaService.obtenerZonasPorSedeId(sedeId, includeInactive));
    }

    @PostMapping
    public ResponseEntity<ZonaResponse> crearZona(
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam Long sedeId) {
        ZonaResponse createdZona = zonaService.crearZona(new ZonaRequest(nombre, descripcion, foto, sedeId));
        return new ResponseEntity<>(createdZona, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZonaResponse> updateZona(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam Long sedeId) {
        ZonaResponse updatedZona = zonaService.updateZona(id, new ZonaRequest(nombre, descripcion, foto, sedeId));
        return ResponseEntity.ok(updatedZona);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZona(@PathVariable Long id) {
        zonaService.setActivoZona(id, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activo")
    public ResponseEntity<ZonaResponse> setActivoZona(@PathVariable Long id, @RequestParam boolean activo) {
        return ResponseEntity.ok(zonaService.setActivoZona(id, activo));
    }
}


