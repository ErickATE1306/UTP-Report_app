package com.utp_reporta_backend.modules.usuario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utp_reporta_backend.modules.usuario.model.enums.ERol;
import com.utp_reporta_backend.modules.usuario.model.Rol;
//Repositorio para manejar las operaciones CRUD de los roles.
public interface RolRepository extends JpaRepository<Rol, Long>{//Buscar un rol por su nombre.
	Optional<Rol> findByNombre(ERol nombre);

}


