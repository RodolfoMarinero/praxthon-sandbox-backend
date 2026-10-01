package com.amisimecompila.speisandbox.operacion.application;

import com.amisimecompila.speisandbox.idempotencia.infrastructure.persistence.entity.IdempotenciaEntity;
import com.amisimecompila.speisandbox.idempotencia.infrastructure.persistence.repository.IdempotenciaRepository;
import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudOperacion;
import com.amisimecompila.speisandbox.operacion.api.dto.response.ErrorResponse;
import com.amisimecompila.speisandbox.operacion.api.dto.response.OperacionResponse;
import com.amisimecompila.speisandbox.operacion.api.dto.response.PaginaOperacionesResponse;
import com.amisimecompila.speisandbox.operacion.domain.CodigoErrorPrx;
import com.amisimecompila.speisandbox.operacion.domain.Escenario;
import com.amisimecompila.speisandbox.operacion.domain.EstadoOperacion;
import com.amisimecompila.speisandbox.operacion.domain.ResolutorEscenario;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity.OperacionEntity;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity.TransicionOperacionEntity;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.mapper.OperacionPersistenceMapper;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.repository.OperacionRepository;
import com.amisimecompila.speisandbox.shared.util.OperacionIdGenerator;
import com.amisimecompila.speisandbox.shared.util.PayloadHasher;
import com.amisimecompila.speisandbox.validacion.OperacionValidator;
import com.amisimecompila.speisandbox.validacion.ResultadoValidacion;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperacionService {
    private final OperacionRepository repository;
    private final IdempotenciaRepository idempotenciaRepository;
    private final OperacionValidator validator;
    private final ResolutorEscenario resolutor;
    private final OperacionPersistenceMapper persistenceMapper;
    private final OperacionResponseMapper responseMapper;
    private final PayloadHasher hasher;
    private final OperacionIdGenerator idGenerator;
    private final ApplicationEventPublisher publisher;

    public OperacionService(
            OperacionRepository repository,
            IdempotenciaRepository idempotenciaRepository,
            OperacionValidator validator,
            ResolutorEscenario resolutor,
            OperacionPersistenceMapper persistenceMapper,
            OperacionResponseMapper responseMapper,
            PayloadHasher hasher,
            OperacionIdGenerator idGenerator,
            ApplicationEventPublisher publisher
    ) {
        this.repository = repository;
        this.idempotenciaRepository = idempotenciaRepository;
        this.validator = validator;
        this.resolutor = resolutor;
        this.persistenceMapper = persistenceMapper;
        this.responseMapper = responseMapper;
        this.hasher = hasher;
        this.idGenerator = idGenerator;
        this.publisher = publisher;
    }

    @Transactional
    public ResultadoRegistro registrar(
            SolicitudOperacion solicitud,
            UUID clave,
            Escenario escenarioForzado
    ) {
        ResultadoValidacion validacion = validator.validar(solicitud);
        if (!validacion.esValido()) {
            throw new ValidacionOperacionException(
                    solicitud == null
                            ? null
                            : solicitud.referenciaSeguimiento(),
                    validacion.errores()
            );
        }

        String hash = hasher.calcular(solicitud);
        if (clave != null) {
            var existente = idempotenciaRepository.findByClave(
                    clave.toString()
            );
            if (existente.isPresent()) {
                if (!existente.get().getPayloadHash().equals(hash)) {
                    throw new ConflictoIdempotenciaException();
                }
                return new ResultadoRegistro(
                        responseMapper.toResponse(
                                existente.get().getOperacion()
                        ),
                        false
                );
            }
        }

        if (repository.existsByReferenciaSeguimiento(
                solicitud.referenciaSeguimiento()
        )) {
            throw new ValidacionOperacionException(
                    solicitud.referenciaSeguimiento(),
                    List.of(new ErrorResponse(
                            CodigoErrorPrx.PRX_010,
                            "referenciaSeguimiento",
                            "La referencia ya fue registrada"
                    ))
            );
        }

        Escenario escenario = resolutor.resolver(
                solicitud,
                escenarioForzado
        );
        OperacionEntity entity = persistenceMapper.toEntity(
                solicitud,
                idGenerator.generar(),
                hash,
                escenario
        );
        entity.agregarTransicion(new TransicionOperacionEntity(
                EstadoOperacion.RECIBIDO,
                null
        ));
        repository.save(entity);

        if (clave != null) {
            idempotenciaRepository.save(new IdempotenciaEntity(
                    clave.toString(),
                    hash,
                    entity
            ));
        }

        OperacionResponse inicial = responseMapper.toResponse(entity);
        publisher.publishEvent(new OperacionRegistradaEvent(
                entity.getId(),
                escenario
        ));
        return new ResultadoRegistro(inicial, true);
    }

    @Transactional(readOnly = true)
    public OperacionResponse consultar(String id) {
        return repository.findOneWithTransicionesById(id)
                .map(responseMapper::toResponse)
                .orElseThrow(() -> new OperacionNoEncontradaException(id));
    }
    @Transactional(readOnly = true)
    public PaginaOperacionesResponse listar(int pagina, int tamano) {
        Page<OperacionEntity> result = repository.findAll(
                PageRequest.of(
                        pagina,
                        tamano,
                        Sort.by(Sort.Direction.DESC, "fechaRegistro")
                )
        );
        return new PaginaOperacionesResponse(
                result.map(responseMapper::toResponse).getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
