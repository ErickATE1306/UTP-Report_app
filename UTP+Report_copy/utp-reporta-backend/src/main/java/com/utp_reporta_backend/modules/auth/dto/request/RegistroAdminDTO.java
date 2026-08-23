package com.utp_reporta_backend.modules.auth.dto.request;


import lombok.Data;
//DTO para encapsular los datos necesarios para registrar un administrador.
@Data
public class RegistroAdminDTO {
	
	private String nombreCompleto;
	private String username;
    private String correo;
    private String password;
    private String telefono;
    private Long sedeId; 	
	

}
