package com.utp_reporta_backend.modules.usuario.service.impl;

import com.utp_reporta_backend.modules.usuario.dto.request.UsuarioUpdateRequest;
import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import com.utp_reporta_backend.modules.usuario.service.UsuarioService;
import com.utp_reporta_backend.modules.usuario.service.command.UsuarioCommandService;
import com.utp_reporta_backend.modules.usuario.service.query.UsuarioQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioQueryService queryService;
    private final UsuarioCommandService commandService;
    public List<UsuarioDTO> getAllUsuarios(){return queryService.getAllUsuarios();}
    public List<UsuarioDTO> getUsuariosByRolUsuario(){return queryService.getUsuariosByRolUsuario();}
    public List<UsuarioDTO> getUsuariosByRolAdmin(){return queryService.getUsuariosByRolAdmin();}
    public List<UsuarioDTO> getUsuariosByRolSeguridad(){return queryService.getUsuariosByRolSeguridad();}
    public List<UsuarioDTO> getFilteredSeguridadUsers(Long zonaId,Long sedeId){return queryService.getFilteredSeguridadUsers(zonaId,sedeId);}
    public List<UsuarioDTO> getUsuariosByTipoDocente(){return queryService.getUsuariosByTipoDocente();}
    public List<UsuarioDTO> getUsuariosByTipoAlumno(){return queryService.getUsuariosByTipoAlumno();}
    public UsuarioDTO findByCodigo(String codigo){return queryService.findByCodigo(codigo);}
    public List<UsuarioDTO> getUsuariosByEnabledStatus(boolean enabled){return queryService.getUsuariosByEnabledStatus(enabled);}
    public UsuarioDTO updateUsuarioEnabledStatus(Long id,boolean enabled){return commandService.updateUsuarioEnabledStatus(id,enabled);}
    public UsuarioDTO updateUsuario(Long id,UsuarioUpdateRequest request,String password){return commandService.updateUsuario(id,request,password);}
    public List<UsuarioDTO> getAllUsersExcludingSuperAdmin(){return queryService.getAllUsersExcludingSuperAdmin();}
    public Boolean isTelefonoUnique(String telefono,Long id){return queryService.isTelefonoUnique(telefono,id);}
    public UsuarioDTO getCurrentProfile(String principal){return queryService.getCurrentProfile(principal);}
}
