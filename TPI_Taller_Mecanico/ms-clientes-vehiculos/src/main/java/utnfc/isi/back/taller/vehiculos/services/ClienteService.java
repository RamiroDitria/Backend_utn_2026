package utnfc.isi.back.taller.vehiculos.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import utnfc.isi.back.taller.commons.exceptions.ServiceException;
import utnfc.isi.back.taller.vehiculos.dtos.ClienteRequest;
import utnfc.isi.back.taller.vehiculos.models.Cliente;
import utnfc.isi.back.taller.vehiculos.repositories.ClienteRepository;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente crear(ClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setNombre(request.nombre());
        cliente.setApellido(request.apellido());
        cliente.setTelefono(request.telefono().replace("+", ""));
        cliente.setEmail(request.email());
        return clienteRepository.save(cliente);
    }

    public Cliente obtener(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> ServiceException.noEncontrado("CLIENTE_NO_ENCONTRADO", "No existe el cliente " + id));
    }

    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }
}
