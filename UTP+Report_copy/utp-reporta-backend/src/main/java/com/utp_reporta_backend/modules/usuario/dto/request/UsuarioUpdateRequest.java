package com.utp_reporta_backend.modules.usuario.dto.request;

import com.utp_reporta_backend.modules.usuario.model.enums.TipoUsuario;
import lombok.Data;
import java.util.List;

@Data
public class UsuarioUpdateRequest {
    private String nombreCompleto;
    private String correo;
    private String telefono;
    private TipoUsuario tipoUsuario;
    private String sedeNombre;
    private List<String> zonasNombres;
}
