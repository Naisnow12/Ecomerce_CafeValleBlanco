package com.valleblanco.backend.service;

import com.valleblanco.backend.model.Auditoria;
import com.valleblanco.backend.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    @Transactional
    public void registrar(String emailUsuario, String rolUsuario, String accion, String modulo, String idRegistro, String detalles, String resultado) {
        try {
            Auditoria auditoria = Auditoria.builder()
                    .emailUsuario(emailUsuario != null ? emailUsuario : "ANONIMO")
                    .rolUsuario(rolUsuario != null ? rolUsuario : "DESCONOCIDO")
                    .accion(accion)
                    .modulo(modulo)
                    .idRegistro(idRegistro)
                    .detalles(detalles)
                    .resultado(resultado != null ? resultado : "EXITO")
                    .creadoEn(LocalDateTime.now())
                    .build();
            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            log.error("Error al registrar auditoría: {}", e.getMessage());
        }
    }

    public List<Auditoria> listarTodas() {
        return auditoriaRepository.findAllByOrderByCreadoEnDesc();
    }
}
