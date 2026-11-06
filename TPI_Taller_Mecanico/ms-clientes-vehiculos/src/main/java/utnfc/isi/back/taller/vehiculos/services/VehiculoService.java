package utnfc.isi.back.taller.vehiculos.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import utnfc.isi.back.taller.commons.exceptions.ServiceException;
import utnfc.isi.back.taller.commons.validations.PatenteValidator;
import utnfc.isi.back.taller.commons.validations.VinValidator;
import utnfc.isi.back.taller.vehiculos.clients.DatosVin;
import utnfc.isi.back.taller.vehiculos.clients.VpicClient;
import utnfc.isi.back.taller.vehiculos.dtos.VehiculoRequest;
import utnfc.isi.back.taller.vehiculos.dtos.VehiculoUpdateRequest;
import utnfc.isi.back.taller.vehiculos.dtos.VinRequest;
import utnfc.isi.back.taller.vehiculos.models.Vehiculo;
import utnfc.isi.back.taller.vehiculos.repositories.VehiculoRepository;

@Service
@RequiredArgsConstructor
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ClienteService clienteService;
    private final VpicClient vpicClient;

    /** F01 - Registrar vehículo. RN-01: formato de patente y patente no repetida. */
    @Transactional
    public Vehiculo crear(VehiculoRequest request) {
        String patente = PatenteValidator.normalizar(request.patente());
        if (!PatenteValidator.esValida(patente)) {
            throw ServiceException.reglaNegocio("RN-01", "La patente debe tener formato AAA123 o AA123AA");
        }
        if (vehiculoRepository.existsByPatente(patente)) {
            throw ServiceException.conflicto("RN-01", "La patente " + patente + " ya está registrada");
        }
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPatente(patente);
        vehiculo.setMarca(request.marca());
        vehiculo.setModelo(request.modelo());
        vehiculo.setAnio(request.anio());
        vehiculo.setCliente(clienteService.obtener(request.clienteId()));
        return vehiculoRepository.save(vehiculo);
    }

    public Vehiculo obtener(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> ServiceException.noEncontrado("VEHICULO_NO_ENCONTRADO", "No existe el vehículo " + id));
    }

    public Optional<Vehiculo> buscarPorPatente(String patente) {
        return vehiculoRepository.findByPatente(PatenteValidator.normalizar(patente));
    }

    @Transactional
    public Vehiculo actualizar(Long id, VehiculoUpdateRequest request) {
        Vehiculo vehiculo = obtener(id);
        vehiculo.setMarca(request.marca());
        vehiculo.setModelo(request.modelo());
        vehiculo.setAnio(request.anio());
        vehiculo.recalcularConsistencia();
        return vehiculo;
    }

    /**
     * F03 - Registrar VIN. RN-04: VIN de 17 caracteres sin I, O ni Q.
     * Si vPIC lo decodifica, se guardan sus datos y se compara la marca.
     * Si vPIC falla o no lo reconoce, se usan los datos manuales y quedan como no verificados.
     */
    @Transactional
    public Vehiculo registrarVin(Long id, VinRequest request) {
        String vin = VinValidator.normalizar(request.vin());
        if (!VinValidator.esValido(vin)) {
            throw ServiceException.reglaNegocio("RN-04", "El VIN debe tener 17 caracteres y no puede contener I, O ni Q");
        }
        Vehiculo vehiculo = obtener(id);
        vehiculo.setVin(vin);

        Optional<DatosVin> datos = vpicClient.decodificar(vin);
        if (datos.isPresent()) {
            vehiculo.setMarcaVpic(datos.get().marca());
            vehiculo.setMotor(datos.get().motor());
            vehiculo.setCombustible(datos.get().combustible());
            vehiculo.setDatosVerificados(true);
        } else {
            vehiculo.setMarcaVpic(null);
            vehiculo.setMotor(request.motor());
            vehiculo.setCombustible(request.combustible());
            vehiculo.setDatosVerificados(false);
        }
        vehiculo.recalcularConsistencia();
        return vehiculo;
    }
}
