package com.utp_reporta_backend.modules.auth.dto.request;

import lombok.Data;
//DTO para encapsular los datos de inicio de sesión.
@Data
public class LoginDTO {
	private String usernameOrCorreo;
    private String password;
}
