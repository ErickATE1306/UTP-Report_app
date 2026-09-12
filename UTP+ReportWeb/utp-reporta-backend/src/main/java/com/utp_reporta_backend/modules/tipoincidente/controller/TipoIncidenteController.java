package com.utp_reporta_backend.modules.tipoincidente.controller;

import com.utp_reporta_backend.modules.tipoincidente.dto.request.TipoIncidenteRequest;
import com.utp_reporta_backend.modules.tipoincidente.dto.response.TipoIncidenteResponse;
import jakarta.validation.Valid;
import com.utp_reporta_backend.modules.tipoincidente.service.TipoIncidenteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tipoincidentes")
@RequiredArgsConstructor
public class TipoIncidenteController {
    private final TipoIncidenteService tipoIncidenteService;

    @GetMapping
    public ResponseEntity<List<TipoIncidenteResponse>> getAllTipoIncidentes() {
        List<TipoIncidenteResponse> tipoIncidentes = tipoIncidenteService.getAllTipoIncidentes();
        return new ResponseEntity<>(tipoIncidentes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoIncidenteResponse> getTipoIncidenteById(@PathVariable Long id) {
        TipoIncidenteResponse tipoIncidente = tipoIncidenteService.getTipoIncidenteById(id);
        if (tipoIncidente != null) {
            return new ResponseEntity<>(tipoIncidente, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<TipoIncidenteResponse> createTipoIncidente(@Valid @RequestBody TipoIncidenteRequest request) {
        TipoIncidenteResponse createdTipoIncidente = tipoIncidenteService.createTipoIncidente(request);
        return new ResponseEntity<>(createdTipoIncidente, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoIncidenteResponse> updateTipoIncidente(@PathVariable Long id, @Valid @RequestBody TipoIncidenteRequest request) {
        TipoIncidenteResponse updatedTipoIncidente = tipoIncidenteService.updateTipoIncidente(id, request);
        if (updatedTipoIncidente != null) {
            return new ResponseEntity<>(updatedTipoIncidente, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTipoIncidente(@PathVariable Long id) {
        tipoIncidenteService.deleteTipoIncidente(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}


