package com.utp_reporta_backend.modules.auth.dto.request;

import com.utp_reporta_backend.modules.usuario.model.enums.TipoUsuario;

import lombok.Data;
//DTO para encapsular los datos necesarios para registrar un usuario.
@Data
public class RegistroUsuarioDTO {
	private String nombreCompleto;
	private String username;
    private String correo;
    private String password;
    private String telefono;
    private TipoUsuario tipoUsuario;
    private Long sedeId;     
}


